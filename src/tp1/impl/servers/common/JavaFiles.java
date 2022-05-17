package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.INTERNAL_ERROR;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import util.Dropbox;

public class JavaFiles implements Files {

	static final String DELIMITER = "$$$";
	private static final String ROOT = "/tmp/";
	
	public JavaFiles() {

	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		byte[] data = Dropbox.read(fileId);
		return data != null ? ok( data) : error( NOT_FOUND );
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		
		boolean res = Dropbox.delete(fileId);	
		return res ? ok() : error( NOT_FOUND );
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		Dropbox.write(fileId, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		try {
			Dropbox.delete(userId);	
		
		} catch (Exception e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}
}
