package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.redirect;
import static tp1.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp1.api.service.java.Result.ErrorCode.FORBIDDEN;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import static tp1.impl.clients.Clients.FilesClients;
import static tp1.impl.clients.Clients.UsersClients;
import static tp1.impl.clients.Clients.DirectoryClients;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import kafka.KafkaSubscriber;
import kafka.ProcessFilesOperation;
import kafka.ProcessOperation;
import token.GenerateToken;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.java.Result.ErrorCode;
import tp1.impl.discovery.Discovery;


public class JavaDirectory implements Directory {

	static final long USER_CACHE_EXPIRATION = 3000;
	static final int MAX_URLS = 2;
	private KafkaSubscriber ks;

	final LoadingCache<UserInfo, Result<User>> users = CacheBuilder.newBuilder()
			.expireAfterWrite(Duration.ofMillis(USER_CACHE_EXPIRATION)).build(new CacheLoader<>() {
				@Override
				public Result<User> load(UserInfo info) throws Exception {
					var res = UsersClients.get().getUser(info.userId(), info.password());
					if (res.error() == ErrorCode.TIMEOUT)
						return error(BAD_REQUEST);
					else
						return res;
				}
			});

	final static Logger Log = Logger.getLogger(JavaDirectory.class.getName());
	final ExecutorService executor = Executors.newCachedThreadPool();

	final Map<String, ExtendedFileInfo> files = new ConcurrentHashMap<>();
	final Map<String, UserFiles> userFiles = new ConcurrentHashMap<>();
	final Map<URI, FileCounts> fileCounts = new ConcurrentHashMap<>();
	
	
	public JavaDirectory() {
		ks = KafkaSubscriber.createSubscriber(ReplicationManager.KAFKA_BROKERS, List.of(ReplicationManager.TOPIC), ReplicationManager.REPLAY_FROM_BEGINNING);
		ks.start(false, new ProcessOperation(this));
	}
	
	

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password, Long version) {

		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		var info = file != null ? file.info() : new FileInfo();
		int countWrites = 0;
		List<URI> uris = new ArrayList<>(MAX_URLS);

		for (var uri : orderCandidateFileServers(file)) {
			String token = GenerateToken.buildToken(fileId);
			var result = FilesClients.get(uri).writeFile(fileId, data, token);
			if (result.isOK()) {
				uris.add(uri);
				countWrites++;
				if (countWrites < 2) {
					info.setOwner(userId);
					info.setFilename(filename);
					info.setFileURL(String.format("%s/files/%s", uri, fileId));
				} else
					break;

			} else
				Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
		}
		file = new ExtendedFileInfo(uris, fileId, info);
		files.put(fileId, file);

		for (URI uri : DirectoryClients.all())
			DirectoryClients.get(uri).writeFileSec(filename, userId, file);

		if (countWrites > 0)
			return ok(file.info);
		else
			return error(BAD_REQUEST);
	}

	@Override
	public Result<Void> writeFileSec(String filename, String userId, ExtendedFileInfo file) {



		var fileId = fileId(filename, userId);

		if (file != null)
			files.put(fileId, file);
		else
			file = files.get(fileId);

		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		synchronized (uf) {
			if (uf.owned().add(fileId))
				for (URI fileUri : file.uri)
					getFileCounts(fileUri, true).numFiles().incrementAndGet();
		}
		return ok();
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password, Long version) {
		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		executor.execute(() -> {
			for (URI uri : file.uri) {
				String token = GenerateToken.buildToken(fileId);
				FilesClients.get(uri).deleteFile(fileId, token);
			}
		});

		for (URI uri : DirectoryClients.all())
			DirectoryClients.get(uri).deleteFileSec(filename, userId);

		return ok();
	}

	@Override
	public Result<Void> deleteFileSec(String filename, String userId) {


		var fileId = fileId(filename, userId);
		var uf = userFiles.getOrDefault(userId, new UserFiles());

		synchronized (uf) {
			var info = files.remove(fileId);
			uf.owned().remove(fileId);

			executor.execute(() -> {
				this.removeSharesOfFile(info);
			});

			for (URI uri : info.uri)
				getFileCounts(uri, false).numFiles().decrementAndGet();
		}
		return ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		for (URI uri : DirectoryClients.all())
			DirectoryClients.get(uri).shareFileSec(filename, userId, userIdShare);

		return ok();
	}

	@Override
	public Result<Void> shareFileSec(String filename, String userId, String userIdShare) {


		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().add(fileId);
			file.info().getSharedWith().add(userIdShare);
		}
		return ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		for (URI uri : DirectoryClients.all())
			DirectoryClients.get(uri).unshareFileSec(filename, userId, userIdShare);

		return ok();
	}

	@Override
	public Result<Void> unshareFileSec(String filename, String userId, String userIdShare) {

		
		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}
		return ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password, Long version) {

		if (badParam(filename))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(accUserId, password);
		if (!user.isOK())
			return error(user.error());

		if (!file.info().hasAccess(accUserId))
			return error(FORBIDDEN);

		Discovery d = Discovery.getInstance();
		URI uriToRedirect = d.getUriToRedirect(file.uri);
		var url = String.format("%s/files/%s", uriToRedirect, fileId);
		return redirect(url);
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password, Long version) {
		if (badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var infos = Stream.concat(uf.owned().stream(), uf.shared().stream()).map(f -> files.get(f).info())
					.collect(Collectors.toSet());

			return ok(new ArrayList<>(infos));
		}
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}

	private static boolean badParam(String str) {
		return str == null || str.length() == 0;
	}

	private Result<User> getUser(String userId, String password) {
		try {
			return users.get(new UserInfo(userId, password));
		} catch (Exception x) {
			x.printStackTrace();
			return error(ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password) {
		users.invalidate(new UserInfo(userId, password));

		var fileIds = userFiles.remove(userId);
		if (fileIds != null)
			for (var id : fileIds.owned()) {
				var file = files.remove(id);
				removeSharesOfFile(file);
				for (URI uri : file.uri)
					getFileCounts(uri, false).numFiles().decrementAndGet();
			}
		return ok();
	}

	private void removeSharesOfFile(ExtendedFileInfo file) {
		for (var userId : file.info().getSharedWith())
			userFiles.getOrDefault(userId, new UserFiles()).shared().remove(file.fileId());
	}

	private Queue<URI> orderCandidateFileServers(ExtendedFileInfo file) {
		int MAX_SIZE = 3;
		Queue<URI> result = new ArrayDeque<>();

		if (file != null)
			for (URI uri : file.uri)
				result.add(uri);

		FilesClients.all().stream().filter(u -> !result.contains(u)).map(u -> getFileCounts(u, false))
				.sorted(FileCounts::ascending).map(FileCounts::uri).limit(MAX_SIZE).forEach(result::add);

		while (result.size() < MAX_SIZE)
			result.add(result.peek());
		Log.info("Candidate files servers: " + result + "\n");
		return result;
	}

	private FileCounts getFileCounts(URI uri, boolean create) {
		if (create)
			return fileCounts.computeIfAbsent(uri, FileCounts::new);
		else
			return fileCounts.getOrDefault(uri, new FileCounts(uri));
	}


	public static record ExtendedFileInfo(List<URI> uri, String fileId, FileInfo info) {
	}

	static record UserFiles(Set<String> owned, Set<String> shared) {

		UserFiles() {
			this(ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
		}
	}

	static record FileCounts(URI uri, AtomicLong numFiles) {
		FileCounts(URI uri) {
			this(uri, new AtomicLong(0L));
		}

		static int ascending(FileCounts a, FileCounts b) {
			return Long.compare(a.numFiles().get(), b.numFiles().get());
		}
	}

	static record UserInfo(String userId, String password) {
	}
}