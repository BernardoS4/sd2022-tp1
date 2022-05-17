package util;

import dropbox.DeleteDirectory;
import dropbox.DownloadFile;
import dropbox.UploadFile;

final public class Dropbox {

	public static void write(String fileId, byte[] data) {
		try {
			var uf = new UploadFile();
			uf.execute("/" + fileId, data);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static byte[] read(String fileId) {
		try {
			var df = new DownloadFile();
			return df.execute("/" + fileId);
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public static boolean delete(String fileId) {
		try {
			var df = new DeleteDirectory();
			df.execute("/" + fileId);
			return true;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return false;
	}
}
