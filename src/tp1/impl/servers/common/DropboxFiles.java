package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;

import tp1.api.service.java.DboxFiles;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import util.Dropbox;

public class DropboxFiles implements DboxFiles {

	static final String DELIMITER = "$$$";

	public DropboxFiles() {
	}

	@Override
	public Result<byte[]> getFile(String apiKey, String apiSecret, String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		byte[] data = Dropbox.read(apiKey, apiSecret, fileId);
		return data != null ? ok(data) : error(NOT_FOUND);
	}

	@Override
	public Result<Void> deleteFile(String apiKey, String apiSecret, String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		boolean res = Dropbox.delete(apiKey, apiSecret, fileId);
		return res ? ok() : error(NOT_FOUND);
	}

	@Override
	public Result<Void> writeFile(String apiKey, String apiSecret, String fileId, byte[] data, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		Dropbox.write(apiKey, apiSecret, fileId, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String apiKey, String apiSecret, String userId, String token) {
		Dropbox.delete(apiKey, apiSecret, userId);
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}
}
