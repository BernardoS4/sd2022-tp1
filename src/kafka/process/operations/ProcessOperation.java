package kafka.process.operations;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import kafka.sync.SyncPoint;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.JSON;
import static util.SystemConstants.*;


public class ProcessOperation implements RecordProcessor {

	private Directory rep;
	

	public ProcessOperation(Directory rep) {
		this.rep = rep;
	}
	

	@Override
	public void onReceive(ConsumerRecord<String, String> r) {

		Map<String, String> opParams = JSON.decode(r.value());

		String filename = JSON.decode(opParams.get(FILENAME), String.class);
		String userId = JSON.decode(opParams.get(USER_ID), String.class);
		String userIdShare = JSON.decode(opParams.get(USER_ID_SHARE), String.class);

		
		switch (r.key()) {
		case WRITE_FILE:
			URI[] uris = JSON.decode(opParams.get(URIS), URI[].class);
			String fileId = JSON.decode(opParams.get(FILE_ID), String.class);
			FileInfo info = JSON.decode(opParams.get(INFO), FileInfo.class);
			ExtendedFileInfo file = new ExtendedFileInfo(Arrays.asList(uris), fileId, info);
			SyncPoint.getInstance().setResult(r.offset(), rep.writeFileSec(filename, userId, file));
			break;
		case DELETE_FILE:
			SyncPoint.getInstance().setResult(r.offset(), rep.deleteFileSec(filename, userId));
			break;
		case SHARE_FILE:
			SyncPoint.getInstance().setResult(r.offset(), rep.shareFileSec(filename, userId, userIdShare));
			break;
		case UNSHARE_FILE:
			SyncPoint.getInstance().setResult(r.offset(), rep.unshareFileSec(filename, userId, userIdShare));
			break;
		case DELETE_USER_FILES:
			String password = JSON.decode(opParams.get(PASSWORD), String.class);
			SyncPoint.getInstance().setResult(r.offset(), rep.deleteUserFiles(userId, password));
			break;
		default:
			break;
		}
	}

}