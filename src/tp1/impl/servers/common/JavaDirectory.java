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
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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

import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.java.Result.ErrorCode;
import util.Operation;
import zookeeper.Zookeeper;

public class JavaDirectory implements Directory {

	static final long USER_CACHE_EXPIRATION = 3000;
	static final int MAX_URLS = 2;
	private long version = -1L;

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
	private Map<Long, Operation> opVersion = new ConcurrentHashMap<>();


	@Override
	public Result<FileInfo> writeFile(Long version, String filename, byte[] data, String userId, String password) {

		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		synchronized (uf) {
			var fileId = fileId(filename, userId);
			var file = files.get(fileId);
			var info = file != null ? file.info() : new FileInfo();
			int countWrites = 0;
			List<URI> uris = new LinkedList<>();
			String fileURL;
			/*var token = new GenerateToken();
			token.buildToken(fileId);*/

			for (var uri : orderCandidateFileServers(file)) {
				var result = FilesClients.get(uri).writeFile(fileId, data, "");
				if (result.isOK()) {
					fileURL = String.format("%s/files/%s", uri, fileId);
					try {
						uris.add(new URI(fileURL));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					countWrites++;
					if (countWrites < 2) {
						info.setOwner(userId);
						info.setFilename(filename);
						info.setFileURL(fileURL);
					} else {
						files.put(fileId, file = new ExtendedFileInfo(uris, fileId, info));
						if (uf.owned().add(fileId)) 
							for (URI fileUri : file.uri()) 
								getFileCounts(fileUri, true).numFiles().incrementAndGet();
						break;
					}
				} else
					Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
			}
			
			// for(URI uri : DirectoryClients.all())
			// DirectoryClients.get(uri).writeFile(version, fileId, file);
			
			if (countWrites > 0) return ok(file.info);
			else return error(BAD_REQUEST);
		}
	}
	
	@Override
	public Result<Void> writeFile(Long version, String filename, String userId, ExtendedFileInfo file) {

		//guardar operaçao e incrementar
		var fileId = fileId(filename, userId);
		files.put(fileId, file);
		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		if (uf.owned().add(fileId)) 
			for (URI fileUri : file.uri()) 
				getFileCounts(fileUri, true).numFiles().incrementAndGet();
		return ok();
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId, String password) {
		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var info = files.remove(fileId);
			uf.owned().remove(fileId);
			/*var token = new GenerateToken();
			token.buildToken(fileId);*/
			
			executor.execute(() -> {
				this.removeSharesOfFile(info);
				for (URI uri : file.uri)
					FilesClients.get(uri).deleteFile(fileId, "");
			});

			for (URI uri : file.uri)
				getFileCounts(uri, false).numFiles().decrementAndGet();
		}

		// for(URI uri : DirectoryClients.all())
		// DirectoryClients.get(uri).writeFile(version, fileId, file);

		return ok();
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId) {
		var uf = userFiles.getOrDefault(userId, new UserFiles());
		var fileId = fileId(filename, userId);
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
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().add(fileId);
			file.info().getSharedWith().add(userIdShare);
		}

		return ok();
	}
	
	@Override
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare) {
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
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}

		return ok();
	}
	
	@Override
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare) {
		var fileId = fileId(filename, userId);
		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		var file = files.get(fileId);
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}
		return ok();
	}

	@Override
	public Result<byte[]> getFile(Long version, String filename, String userId, String accUserId, String password) {
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

		Result<byte[]> result = redirect(file.info().getFileURL());

		String url = file.uri.get(0).toString();
		String url2 = file.uri.get(1).toString();

		if (url.equalsIgnoreCase(file.info().getFileURL())) 
			file.info().setFileURL(url2);	
		else 
			file.info().setFileURL(url);	

		return result;

	}
	

	@Override
	public Result<List<FileInfo>> lsFile(Long version, String userId, String password) {
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
	
	@Override
	public Result<Void> lsFile(Long version, String userId) {
		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			Stream.concat(uf.owned().stream(), uf.shared().stream()).map(f -> files.get(f).info())
					.collect(Collectors.toSet());
		}
		return ok();
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
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
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
			result.addAll(file.uri());

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

	private void addOperation(Long version, Operation op) {
		opVersion.put(version, op);
	}

	// para obter a versao do primario -> faz pedido ao primario
	@Override
	public Result<Operation> getOperation(Long version) {
		return null;

	}

	public synchronized void updateVersion(long newVersion) throws Exception {
		Zookeeper zk = Zookeeper.getInstance();
		while (version < newVersion) {

			Operation op = DirectoryClients.get(zk.getPrimaryPath()).getOperation(++version).value();
			op.execute();
		}
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