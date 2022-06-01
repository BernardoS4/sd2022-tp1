package tp1.api.service.java;

import java.util.List;

import token.GenerateToken;
import tp1.api.FileInfo;
import tp1.api.service.java.Result.ErrorCode;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.Operation;


public interface Directory {

	static String SERVICE_NAME = "directory";
	
	Result<FileInfo> writeFile(Long version, String filename, byte []data, String userId, String password);
	
	Result<Void> writeFile(Long version, String filename, String userId, ExtendedFileInfo file);

	Result<Void> deleteFile(Long version, String filename, String userId, String password);
	
	Result<Void> deleteFile(Long version, String filename, String userId);

	Result<Void> shareFile(Long version, String filename, String userId, String userIdShare, String password);
	
	Result<Void> shareFile(Long version, String filename, String userId, String userIdShare);

	Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare, String password);
	
	Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare);

	Result<byte[]> getFile(Long version, String filename,  String userId, String accUserId, String password);

	Result<List<FileInfo>> lsFile(Long version, String userId, String password);
	
	Result<Void> lsFile(Long version, String userId);
		
	Result<Void> deleteUserFiles(String userId, String password, String token);
	
	default Result<Operation> getOperation(Long version) {
		return Result.error(ErrorCode.NOT_IMPLEMENTED); 
	}
}
