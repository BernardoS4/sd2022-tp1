package tp1.impl.servers.rest;

import java.util.List;
import tp1.api.FileInfo;

import token.GenerateToken;
import java.util.logging.Logger;
import jakarta.inject.Singleton;
import kafka.sync.SyncPoint;
import tp1.api.service.java.Result;
import tp1.api.service.java.Directory;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.java.Result.ErrorCode;
import tp1.impl.servers.common.JavaDirectory;
import static tp1.impl.clients.Clients.FilesClients;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;

@Singleton
public class DirectoryResources extends RestResource implements RestDirectory {
	private static Logger Log = Logger.getLogger(DirectoryResources.class.getName());

	private static final String REST = "/rest/";

	final Directory impl;

	public DirectoryResources() {
		impl = new JavaDirectory();
	}

	@Override
	public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password) {
		Log.info(String.format(
				"REST writeFile: version = %d, filename = %s, data.length = %d, userId = %s, password = %s \n", version,
				filename, data.length, userId, password));
		
			return super.resultOrThrow(impl.writeFile(filename, data, userId, password, version), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void writeFileSec(Long version, String filename, String userId, ExtendedFileInfo file) {
		Log.info(
				String.format("REST writeFileSec: version = %d, filename = %s, userId = %s\n", version, filename, userId));

		super.resultOrThrow(impl.writeFileSec(filename, userId, file), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void deleteFile(Long version, String filename, String userId, String password) {
		Log.info(String.format("REST deleteFile: version = %d, filename = %s, userId = %s, password =%s\n", version,
				filename, userId, password));

		
			super.resultOrThrow(impl.deleteFile(filename, userId, password, version), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void deleteFileSec(Long version, String filename, String userId) {
		Log.info(String.format("REST deleteFileSec: version = %d, filename = %s, userId = %s\n", version, filename,
				userId));

		super.resultOrThrow(impl.deleteFileSec(filename, userId), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format(
				"REST shareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n", version,
				filename, userId, userIdShare, password));

		
			super.resultOrThrow(impl.shareFile(filename, userId, userIdShare, password, version), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void shareFileSec(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST shareFileSec: version = %d, filename = %s, userId = %s, userIdShare = %s\n", version,
				filename, userId, userIdShare));

		super.resultOrThrow(impl.shareFileSec(filename, userId, userIdShare), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Log.info(String.format(
				"REST unshareFile: version = %d, filename = %s, userId = %s, userIdShare = %s, password =%s\n", version,
				filename, userId, userIdShare, password));
		
			super.resultOrThrow(impl.unshareFile(filename, userId, userIdShare, password, version), SyncPoint.getInstance().getVersion());
	}

	@Override
	public void unshareFileSec(Long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("REST unshareFileSec: version = %d, filename = %s, userId = %s, userIdShare = %s\n",
				version, filename, userId, userIdShare));

		super.resultOrThrow(impl.unshareFileSec(filename, userId, userIdShare), SyncPoint.getInstance().getVersion());
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
		return super.resultOrThrow(res, SyncPoint.getInstance().getVersion());

	}

	@Override
	public List<FileInfo> lsFile(Long version, String userId, String password) {
		long T0 = System.currentTimeMillis();
		try {

			Log.info(String.format("REST lsFile: version = %d, userId = %s, password = %s\n", version, userId,
					password));

			return super.resultOrThrow(impl.lsFile(userId, password, version), SyncPoint.getInstance().getVersion());
		} finally {
			System.err.println("TOOK:" + (System.currentTimeMillis() - T0));
		}
	}

	@Override
	public void deleteUserFiles(String userId, String password) {
		Log.info(
				String.format("REST deleteUserFiles: user = %s, password = %s\n", userId, password));

		super.resultOrThrow(impl.deleteUserFiles(userId, password), SyncPoint.getInstance().getVersion());
	}
	
}