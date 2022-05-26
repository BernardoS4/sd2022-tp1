package tp1.api.service.java;

public interface DboxFiles {

public static String SERVICE_NAME = "files";
	
	Result<byte[]> getFile(String apiKey, String apiSecret, String fileId, String token);

	Result<Void> deleteFile(String apiKey, String apiSecret, String fileId, String token);
	
	Result<Void> writeFile(String apiKey, String apiSecret, String fileId, byte[] data, String token);

	Result<Void> deleteUserFiles(String apiKey, String apiSecret, String userId, String token);
}
