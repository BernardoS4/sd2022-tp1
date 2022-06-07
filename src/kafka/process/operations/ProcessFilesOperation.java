package kafka.process.operations;


import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import kafka.sync.SyncPoint;
import tp1.api.service.rest.RestDirectory;
import tp1.impl.servers.common.JavaFiles;
import util.JSON;

public class ProcessFilesOperation implements RecordProcessor {

	private JavaFiles jf;
	
	
	public ProcessFilesOperation(JavaFiles jf) {
		this.jf = jf;
	}
	
	
	@Override
	public void onReceive(ConsumerRecord<String, String> r) {
		Map<String, String> opParams = JSON.decode(r.value());
		String userId = JSON.decode(opParams.get(RestDirectory.USER_ID), String.class);
		if(r.key().equals(RestDirectory.DELETE_USER_FILES))
			SyncPoint.getInstance().setResult(r.offset(), jf.deleteUserFiles(userId));
	}
}
