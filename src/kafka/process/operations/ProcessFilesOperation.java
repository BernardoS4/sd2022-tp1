package kafka.process.operations;


import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import kafka.sync.SyncPoint;
import tp1.api.service.java.Files;
import util.JSON;
import static util.SystemConstants.USER_ID;
import static util.SystemConstants.DELETE_USER_FILES;;

public class ProcessFilesOperation implements RecordProcessor {

	private Files jf;
	
	
	public ProcessFilesOperation(Files jf) {
		this.jf = jf;
	}
	
	
	@Override
	public void onReceive(ConsumerRecord<String, String> r) {
		Map<String, String> opParams = JSON.decode(r.value());
		String userId = JSON.decode(opParams.get(USER_ID), String.class);
		if(r.key().equals(DELETE_USER_FILES))
			SyncPoint.getInstance().setResult(r.offset(), jf.deleteUserFiles(userId));
	}
}
