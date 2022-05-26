package util;

import dropbox.DeleteDirectory;
import dropbox.DownloadFile;
import dropbox.UploadFile;

final public class Dropbox {
	
	private Dropbox() {}

    public static void write(String apiKey, String apiSecret, String fileId, byte[] data) {
        try {
            UploadFile createFile = new UploadFile(apiKey, apiSecret);
            createFile.execute("/main/" + fileId, data);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static byte[] read(String apiKey, String apiSecret, String fileId) {
        try {
            DownloadFile getFile = new DownloadFile(apiKey, apiSecret);
            return getFile.execute("/main/" + fileId);
        } catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }

    public static boolean delete(String apiKey, String apiSecret, String fileId) {
        try {
            DeleteDirectory deleteFile = new DeleteDirectory(apiKey, apiSecret);
            deleteFile.execute("/main/" + fileId);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }
    
    public static boolean deleteAllFiles(String apiKey, String apiSecret) {
        try {
            DeleteDirectory deleteFile = new DeleteDirectory(apiKey, apiSecret);
            deleteFile.execute("/main");
            return true;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }

}
