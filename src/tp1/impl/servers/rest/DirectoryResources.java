package tp1.impl.servers.rest;

import static tp1.impl.clients.Clients.FilesClients;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.java.Result.ErrorCode;
import tp1.api.service.rest.RestDirectory;
import tp1.impl.servers.common.JavaDirectory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.IP;
import util.Operation;
import zookeeper.Zookeeper;
import token.GenerateToken;

@Singleton
public class DirectoryResources extends RestResource implements RestDirectory {
	private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

	private static final String REST = "/rest/";

	final Directory impl;
	private String primaryPath = "";
	private Zookeeper zk;
	private String serverURI = "";

	public DirectoryResources() {
		impl = new JavaDirectory();
		//startZookeeper();
	}
	
	private void startZookeeper() {
		serverURI = String.format(DirectoryRestServer.SERVER_BASE_URI, IP.hostAddress(), DirectoryRestServer.PORT);
		byte[] svrURIinBytes = serverURI.getBytes();
		/*zk = Zookeeper.getInstance();
		zk.createPersistent(svrURIinBytes);
		zk.createEphemerals(svrURIinBytes);
		zk.watchEvents();*/
	}
	@Override
	public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password) {
		Log.info(String.format("REST writeFile: version = %d, filename = %s, data.length = %d, userId = %s, password = %s \n",
				version, filename, data.length, userId, password));

		/*primaryPath = zk.getPrimaryPath();
		if(primaryPath.equalsIgnoreCase(serverURI))
			return super.resultOrThrow(impl.writeFile(version, filename, data, userId, password));
			//throw new WebApplicationException(Response.ok().header(HEADER_VERSION, version).entity(impl.writeFile(version, filename, data, userId, password)).build());
		else
			return super.resultOrThrow(Result.redirect(primaryPath));*/
		
		return super.resultOrThrow(impl.writeFile(filename, data, userId, password, version));
	}
	
	@Override
	public void writeFile(Long version, String filename, String userId, ExtendedFileInfo file) {
		Log.info(String.format("REST writeFile: version = %d, filename = %s, userId = %s\n", version, filename, userId));

		super.resultOrThrow(impl.writeFile(filename, userId, file, version));
	}

	@Override
	public void deleteFile(Long version, String filename, String userId, String password) {
		Log.info(String.format("REST deleteFile: version = %d, filename = %s, userId = %s, password =%s\n", version, filename, userId,
				password));

		/*primaryPath = zk.getPrimaryPath();
		if(primaryPath.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.deleteFile(version, filename, userId, password));
		else
			super.resultOrThrow(Result.redirect(primaryPath));*/
		super.resultOrThrow(impl.deleteFile(filename, userId, password, version));
	}
	
	@Override
	public void deleteFile(Long version, String filename, String userId) {
		Log.info(String.format("REST deleteFile: version = %d, filename = %s, userId = %s\n", version, filename, userId));

		super.resultOrThrow(impl.deleteFile(filename, userId, version));
	}

	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format("REST shareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n", version, filename,
				userId, userIdShare, password));

		/*primaryPath = zk.getPrimaryPath();
		if(primaryPath.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.shareFile(version, filename, userId, userIdShare, password));
		else
			super.resultOrThrow(Result.redirect(primaryPath));*/
		super.resultOrThrow(impl.shareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST shareFile: version = %d, filename = %s, userId = %s, userIdShare = %s\n", version, filename,
				userId, userIdShare));

		super.resultOrThrow(impl.shareFile(filename, userId, userIdShare, version));
	}

	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format("REST unshareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n",
				version, filename, userId, userIdShare, password));

		/*primaryPath = zk.getPrimaryPath();
		if(primaryPath.equalsIgnoreCase(serverURI))
			super.resultOrThrow(impl.unshareFile(version, filename, userId, userIdShare, password));
		else
			super.resultOrThrow(Result.redirect(primaryPath));*/
		super.resultOrThrow(impl.unshareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST unshareFile: version = %d, filename = %s, userId = %s, userIdShare = %s\n",
				version, filename, userId, userIdShare));

		super.resultOrThrow(impl.unshareFile(filename, userId, userIdShare, version));
	}

	@Override
	public byte[] getFile(Long version, String filename, String userId, String accUserId, String password) {
		Log.info(String.format("REST getFile: version = %d, filename = %s, userId = %s, accUserId = %s, password =%s\n", version, filename,
				userId, accUserId, password));

		var res = impl.getFile(filename, userId, accUserId, password, version);
		if (res.error() == ErrorCode.REDIRECT) {
			String location = res.errorValue();
			String fileId = JavaDirectory.fileId(filename, userId);
			String token = GenerateToken.buildToken(fileId);
			if (!location.contains(REST)) {
				res = FilesClients.get(location).getFile(fileId, token);
			}
			else
				res = Result.redirect(location + "?token=" + token);
		}
		return super.resultOrThrow(res);

	}

	@Override
	public List<FileInfo> lsFile(Long version, String userId, String password) {
		long T0 = System.currentTimeMillis();
		try {

			Log.info(String.format("REST lsFile: version = %d, userId = %s, password = %s\n", version, userId, password));

			return super.resultOrThrow(impl.lsFile(userId, password, version));
		} finally {
			System.err.println("TOOK:" + (System.currentTimeMillis() - T0));
		}
	}
	

	@Override
	public void deleteUserFiles(String userId, String password, String token) {
		Log.info(
				String.format("REST deleteUserFiles: user = %s, password = %s, token = %s\n", userId, password, token));

		super.resultOrThrow(impl.deleteUserFiles(userId, password, token));
	}

	@Override
	public Operation getOperation(Long version) {
		Log.info(String.format("REST getOperation: version = %s", version));

		return super.resultOrThrow(impl.getOperation(version));
	}
}
