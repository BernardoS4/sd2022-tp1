package kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import kafka.sync.SyncPoint;
import tp1.api.service.rest.RestDirectory;
import tp1.impl.servers.common.JavaRepDirectory;
import tp1.impl.servers.common.JavaRepDirectory.ExtendedFileInfo;
import util.JSON;

public class ProcessOperation implements RecordProcessor {

	private JavaRepDirectory rep;

	public ProcessOperation(JavaRepDirectory rep) {
		this.rep = rep;
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> r) {

		Map<String, String> opParams = JSON.decode(r.value());

		String filename = JSON.decode(opParams.get(RestDirectory.FILENAME));
		String userId = JSON.decode(opParams.get(RestDirectory.USER_ID));
		String userIdShare = JSON.decode(opParams.get(RestDirectory.USER_ID_SHARE));
		ExtendedFileInfo file = JSON.decode(opParams.get(RestDirectory.FILE));

		switch (r.key()) {
		case "writeFile":
			SyncPoint.getInstance().setResult(r.offset(), rep.writeFileSec(filename, userId, file));
			break;
		case "deleteFile":
			SyncPoint.getInstance().setResult(r.offset(), rep.deleteFileSec(filename, userId));
			break;
		case "shareFile":
			SyncPoint.getInstance().setResult(r.offset(), rep.shareFileSec(filename, userId, userIdShare));
			break;
		case "unshareFile":
			SyncPoint.getInstance().setResult(r.offset(), rep.unshareFileSec(filename, userId, userIdShare));
			break;
		default:
			break;
		}
	}

}
