package tp1.api.service.java;

import token.GenerateToken;

public interface Files {
	public static String SERVICE_NAME = "files";
	
	Result<byte[]> getFile(String fileId, GenerateToken token);

	Result<Void> deleteFile(String fileId, GenerateToken token);
	
	Result<Void> writeFile(String fileId, byte[] data, GenerateToken token);

	Result<Void> deleteUserFiles(String userId, GenerateToken token);
	
}
