package util;

import dropbox.DeleteDirectory;
import dropbox.DownloadFile;
import dropbox.UploadFile;

final public class Dropbox {
	
	private Dropbox() {}

    public static void write(String fileId, byte[] data) {
        try {
            UploadFile createFile = new UploadFile();
            createFile.execute("/main/" + fileId, data);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static byte[] read(String fileId) {
        try {
            DownloadFile getFile = new DownloadFile();
            return getFile.execute("/main/" + fileId);
        } catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }

    public static boolean delete(String fileId) {
        try {
            DeleteDirectory deleteFile = new DeleteDirectory();
            deleteFile.execute("/main/" + fileId);
            return true;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }
    
    public static boolean deleteAllFiles() {
        try {
            DeleteDirectory deleteFile = new DeleteDirectory();
            deleteFile.execute("/main");
            return true;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }

}
