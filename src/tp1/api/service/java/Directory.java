package tp1.api.service.java;

import java.util.List;

import token.GenerateToken;
import tp1.api.FileInfo;
import tp1.api.service.java.Result.ErrorCode;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.Operation;


public interface Directory {

	static String SERVICE_NAME = "directory";
	
	Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password, Long version);
	
	Result<Void> writeFile(String filename, String userId, ExtendedFileInfo file, Long version);

	Result<Void> deleteFile(String filename, String userId, String password, Long version);
	
	Result<Void> deleteFile(String filename, String userId, Long version);

	Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version);
	
	Result<Void> shareFile(String filename, String userId, String userIdShare, Long version);

	Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version);
	
	Result<Void> unshareFile(String filename, String userId, String userIdShare, Long version);

	Result<byte[]> getFile(String filename,  String userId, String accUserId, String password, Long version);

	Result<List<FileInfo>> lsFile(String userId, String password, Long version);
		
	Result<Void> deleteUserFiles(String userId, String password, String token);
	
	default Result<Operation> getOperation(Long version) {
		return Result.error(ErrorCode.NOT_IMPLEMENTED); 
	}
}
