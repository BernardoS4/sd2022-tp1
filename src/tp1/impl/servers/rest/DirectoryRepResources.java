package tp1.impl.servers.rest;

import util.IP;
import java.util.List;
import util.Operation;
import tp1.api.FileInfo;
import zookeeper.Zookeeper;
import token.GenerateToken;
import java.util.logging.Logger;
import jakarta.inject.Singleton;
import tp1.api.service.java.Result;
import tp1.api.service.java.Directory;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.java.Result.ErrorCode;
import tp1.impl.servers.common.JavaDirectory;
import static tp1.impl.clients.Clients.FilesClients;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;

@Singleton
public class DirectoryRepResources extends RestResource implements RestDirectory {
	private static Logger Log = Logger.getLogger(DirectoryRepResources.class.getName());

	private static final String REST = "/rest/";

	final Directory impl;
	private String primaryURI = "";
	private Zookeeper zk;
	private String serverURI = "";

	public DirectoryRepResources() {
		impl = new JavaDirectory();
		startZookeeper();
	}

	private void startZookeeper() {
		serverURI = String.format(DirectoryRepRestServer.SERVER_BASE_URI, IP.hostAddress(),
				DirectoryRepRestServer.PORT);
		byte[] svrURIinBytes = serverURI.getBytes();
		zk = Zookeeper.getInstance();
		zk.createEphemerals(svrURIinBytes);
	}

	@Override
	public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password) {
		Log.info(String.format(
				"REST writeFile: version = %d, filename = %s, data.length = %d, userId = %s, password = %s \n", version,
				filename, data.length, userId, password));

		primaryURI = zk.getPrimaryURI();
		Log.info("PATHHHHHHH       " + primaryURI);
		Log.info("SERVER            " + serverURI);
		if (primaryURI.equalsIgnoreCase(serverURI)) {
			Log.info("ENTREIIIIII");
			return super.resultOrThrow(impl.writeFile(filename, data, userId, password, version), ++version);
		} else {
			primaryURI = primaryURI.replace("directory", "dir");
			primaryURI = String.format("%s/%s/%s?password=%s", primaryURI, userId, filename, password);
			return super.resultOrThrow(Result.redirect(primaryURI), ++version);
		}
	}

	@Override
	public void writeFileSec(Long version, String filename, String userId, ExtendedFileInfo file) {
		Log.info(
				String.format("REST writeFile: version = %d, filename = %s, userId = %s\n", version, filename, userId));

		super.resultOrThrow(impl.writeFileSec(filename, userId, file, version), version);
	}

	@Override
	public void deleteFile(Long version, String filename, String userId, String password) {
		Log.info(String.format("REST deleteFile: version = %d, filename = %s, userId = %s, password =%s\n", version,
				filename, userId, password));

		primaryURI = zk.getPrimaryURI();
		if (primaryURI.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.deleteFile(filename, userId, password, version), version);
		else {
			primaryURI = primaryURI.replace("directory", "dir");
			primaryURI = String.format("/%s/%s/%s?password=%s", primaryURI, userId, filename, password);
			super.resultOrThrow(Result.redirect(primaryURI), version);
		}
	}

	@Override
	public void deleteFileSec(Long version, String filename, String userId) {
		Log.info(String.format("REST deleteFile: version = %d, filename = %s, userId = %s\n", version, filename,
				userId));

		super.resultOrThrow(impl.deleteFileSec(filename, userId, version), version);
	}

	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format(
				"REST shareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n", version,
				filename, userId, userIdShare, password));

		primaryURI = zk.getPrimaryURI();
		if (primaryURI.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.shareFile(filename, userId, userIdShare, password, version), version);
		else
			primaryURI = primaryURI.replace("directory", "dir");
		primaryURI = String.format("/%s/%s/%s/share/%s?password=%s", primaryURI, userId, filename, userIdShare,
				password);
		super.resultOrThrow(Result.redirect(primaryURI), version);
	}

	@Override
	public void shareFileSec(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST shareFile: version = %d, filename = %s, userId = %s, userIdShare = %s\n", version,
				filename, userId, userIdShare));

		super.resultOrThrow(impl.shareFileSec(filename, userId, userIdShare, version), version);
	}

	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format(
				"REST unshareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n", version,
				filename, userId, userIdShare, password));

		primaryURI = zk.getPrimaryURI();
		if (primaryURI.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.unshareFile(filename, userId, userIdShare, password, version), version);
		else {
			primaryURI = primaryURI.replace("directory", "dir");
			primaryURI = String.format("/%s/%s/%s/share/%s?password=%s", primaryURI, userId, filename, userIdShare,
					password);
			super.resultOrThrow(Result.redirect(primaryURI), version);
		}
	}

	@Override
	public void unshareFileSec(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST unshareFile: version = %d, filename = %s, userId = %s, userIdShare = %s\n",
				version, filename, userId, userIdShare));

		super.resultOrThrow(impl.unshareFileSec(filename, userId, userIdShare, version), version);
	}

	@Override
	public byte[] getFile(Long version, String filename, String userId, String accUserId, String password) {
		Log.info(String.format("REST getFile: version = %d, filename = %s, userId = %s, accUserId = %s, password =%s\n",
				version, filename, userId, accUserId, password));

		var res = impl.getFile(filename, userId, accUserId, password, version);
		if (res.error() == ErrorCode.REDIRECT) {
			String location = res.errorValue();
			String fileId = JavaDirectory.fileId(filename, userId);
			String token = GenerateToken.buildToken(fileId);
			if (!location.contains(REST)) {
				res = FilesClients.get(location).getFile(fileId, token);
			} else
				res = Result.redirect(location + "?token=" + token);
		}
		return super.resultOrThrow(res, version);

	}

	@Override
	public List<FileInfo> lsFile(Long version, String userId, String password) {
		long T0 = System.currentTimeMillis();
		try {

			Log.info(String.format("REST lsFile: version = %d, userId = %s, password = %s\n", version, userId,
					password));

			return super.resultOrThrow(impl.lsFile(userId, password, version), version);
		} finally {
			System.err.println("TOOK:" + (System.currentTimeMillis() - T0));
		}
	}

	@Override
	public void deleteUserFiles(String userId, String password, String token) {
		Log.info(
				String.format("REST deleteUserFiles: user = %s, password = %s, token = %s\n", userId, password, token));

		super.resultOrThrow(impl.deleteUserFiles(userId, password, token), 0L);
	}

	@Override
	public Operation getOperation(Long version) {
		Log.info(String.format("REST getOperation: version = %s", version));

		return super.resultOrThrow(impl.getOperation(version), version);
	}

	private String pathToRedirect(String userId, String filename, String password) {
		primaryURI = primaryURI.replace("directory", "dir");
		return String.format("/%s/%s/%s/?password=%s", primaryURI, userId, filename, password);
	}

	private <T> T sendRedirect(String userId, String filename, String password, Long version) {
		primaryURI = pathToRedirect(userId, filename, password);
		return super.resultOrThrow(Result.redirect(primaryURI), version);
	}
}
