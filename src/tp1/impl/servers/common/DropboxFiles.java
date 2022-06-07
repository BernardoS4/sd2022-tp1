package tp1.impl.servers.common;

import static kafka.ReplicationManager.KAFKA_BROKERS;
import static kafka.ReplicationManager.REPLAY_FROM_BEGINNING;
import static kafka.ReplicationManager.TOPIC;
import static tp1.api.service.java.Result.error;
import static tp1.api.service.java.Result.ok;
import static tp1.api.service.java.Result.ErrorCode.NOT_FOUND;
import java.util.List;
import dropbox.Dropbox;
import kafka.KafkaSubscriber;
import kafka.process.operations.ProcessFilesOperation;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;


public class DropboxFiles implements Files {

	static final String DELIMITER = "$$$";
	private KafkaSubscriber ks;

	public DropboxFiles() {
		ks = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), REPLAY_FROM_BEGINNING);
		ks.start(false, new ProcessFilesOperation(this));
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		byte[] data = Dropbox.read(fileId);
		return data != null ? ok(data) : error(NOT_FOUND);
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		boolean res = Dropbox.delete(fileId);
		return res ? ok() : error(NOT_FOUND);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace(DELIMITER, "/");
		Dropbox.write(fileId, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId) {
		Dropbox.delete(userId);
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}
}
