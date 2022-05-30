package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import util.Dropbox;
import token.GenerateToken;

public class DropboxFiles implements Files {

	static final String DELIMITER = "$$$";

	public DropboxFiles() {
	}

	@Override
	public Result<byte[]> getFile(String fileId, GenerateToken token) {
		fileId = fileId.replace(DELIMITER, "/");
		byte[] data = Dropbox.read(fileId);
		return data != null ? ok(data) : error(NOT_FOUND);
	}

	@Override
	public Result<Void> deleteFile(String fileId, GenerateToken token) {
		fileId = fileId.replace(DELIMITER, "/");
		boolean res = Dropbox.delete(fileId);
		return res ? ok() : error(NOT_FOUND);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, GenerateToken token) {
		fileId = fileId.replace(DELIMITER, "/");
		Dropbox.write(fileId, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, GenerateToken token) {
		Dropbox.delete(userId);
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}
}
