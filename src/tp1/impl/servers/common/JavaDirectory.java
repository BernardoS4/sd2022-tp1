package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.redirect;
import static tp1.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp1.api.service.java.Result.ErrorCode.FORBIDDEN;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import static tp1.impl.clients.Clients.FilesClients;
import static tp1.impl.clients.Clients.UsersClients;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
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
import util.Token;

public class JavaDirectory implements Directory {

	static final long USER_CACHE_EXPIRATION = 3000;

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
	final Map<String, FileUrls> fileUrls = new ConcurrentHashMap<>();

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {

		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		//FALTA FAZER COM 2 ESCRITAS
		//NA PRIMEIRA ESCRITA NAO QUERO COM $ MAS NA SEGUNDA QUERO URI1$URI2
		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		synchronized (uf) {
			var fileId = fileId(filename, userId);
			var file = files.get(fileId);
			var info = file != null ? file.info() : new FileInfo();
			int countWrites = 0;
			Set<URI> uriSet = new HashSet<>();
			String ulr1 = null;
			String url2;
			for (var uri : orderCandidateFileServers(file)) {
				var result = FilesClients.get(uri).writeFile(fileId, data, Token.get());
				if (result.isOK()) {
					
					info.setOwner(userId);
					info.setFilename(filename);
					if(countWrites == 0) {
					
						ulr1 = String.format("%s/files/%s", uri, fileId);
						info.setFileURL(ulr1);
						Log.info("FIRST TIME>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>FILE URL: " + info.getFileURL());
					}
					
					//vai fazer a segunda escrita
					else if(countWrites == 1) {
						
						Log.info("SECOND TIME>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>FILE URL: " + info.getFileURL());
						url2 = String.format("%s/files/%s", uri,fileId);
						info.setFileURL(url2);
						fileUrls.put(fileId, new FileUrls(ulr1 ,url2));
						Log.info("SECOND TIME>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>FILE URL2: " + url2);
					}					
					uriSet.add(uri);
					files.put(fileId, file = new ExtendedFileInfo(uriSet, fileId, info));
					if (uf.owned().add(fileId))
						for (URI tmp : file.uri) {
							getFileCounts(tmp, true).numFiles().incrementAndGet();
						}
					countWrites++;
					Log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Counter: " + countWrites);
					if (countWrites == 2)
						break;
				}else
					Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
			}
			if (countWrites > 0)
				return ok(file.info);

			return error(BAD_REQUEST);
		}
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
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

			executor.execute(() -> {
				this.removeSharesOfFile(info);
				for (URI uri : file.uri)
					FilesClients.get(uri).deleteFile(fileId, password);
			});

			for (URI uri : file.uri)
				getFileCounts(uri, false).numFiles().decrementAndGet();
		}
		return ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
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
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
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
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
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

		Result<byte[]> result = null;
		
		result = redirect(file.info().getFileURL());
		
		if (!result.isOK()) {
			//swap uris -> info.getFileURL() = swap uri
			FileUrls fUrls = fileUrls.get(fileId);
			String urlToRetry = fUrls.url2;
			Log.info("-------------------------------------------------FAILED URL " + fUrls.url1);
			Log.info("--------------------------------------------------URL TO RETRY: " + urlToRetry);
			file.info().setFileURL(urlToRetry);
		}	
		
		return result;
		
		//return redirect( file.info().getFileURL() );
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
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

		if (file != null) {

			result.addAll(file.uri());
		}

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

	static record ExtendedFileInfo(Set<URI> uri, String fileId, FileInfo info) {
	}
	
	static record FileUrls(String url1, String url2) {
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