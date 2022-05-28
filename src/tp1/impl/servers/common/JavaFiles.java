package tp1.impl.servers.common;

import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.INTERNAL_ERROR;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import util.IO;

public class JavaFiles implements Files {

	static final String DELIMITER = "$$$";
	private static final String ROOT = "/tmp/";

	public JavaFiles() {
		new File(ROOT).mkdirs();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		byte[] data = IO.read(new File(ROOT + "main/" + fileId));
		return data != null ? ok(data) : error(NOT_FOUND);
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		boolean res = IO.delete(new File(ROOT + "main/" + fileId));
		return res ? ok() : error(NOT_FOUND);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		File file = new File(ROOT + "main/" + fileId);
		file.getParentFile().mkdirs();
		IO.write(file, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		File file = new File(ROOT + "main/" + userId);
		try {
			java.nio.file.Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}

	/*
	 * public String[] hashInfo(String token) {
	 * 
	 * //pos 0 -> fileId; pos 1-> Timestamp; pos 2-> hash String[] info =
	 * token.split(" "); String[] result = null; result[0] =
	 * DigestUtils.sha512Hex(info[0].concat(info[1].toString()).concat(key));
	 * result[1] = info[2]; result[2] = info[1]
	 * 
	 * return result; }
	 */

	public boolean isTokenValid(String timeStamp) {
		// quando criamos o token, definimos a validade dele como sendo a data de criacao + o tempo de validade
		String ts = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		return timeStamp.compareTo(ts) < 0;
	}

	public boolean checkConfidentiality(String newHash, String oldHash) {

		return newHash.equalsIgnoreCase(oldHash);
	}
}
