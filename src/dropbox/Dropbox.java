package dropbox;

final public class Dropbox {
	
	private static final String ROOT = "/main/";
	
	private Dropbox() {}

    public static void write(String fileId, byte[] data) {
        try {
            UploadFile createFile = new UploadFile();
            createFile.execute(ROOT + fileId, data);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static byte[] read(String fileId) {
        try {
            DownloadFile getFile = new DownloadFile();
            return getFile.execute(ROOT + fileId);
        } catch (Exception x) {
            x.printStackTrace();
            return new byte[0];
        }
    }

    public static boolean delete(String fileId) {
        try {
            DeleteDirectory deleteFile = new DeleteDirectory();
            deleteFile.execute(ROOT + fileId);
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
