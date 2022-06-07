package kafka;


import java.util.List;

import kafka.process.operations.ProcessOperation;
import kafka.sync.SyncPoint;
import tp1.impl.servers.common.JavaDirectory;

public class ReplicationManager {

	public static final String TOPIC = "single_partition_topic";
	public static final String KAFKA_BROKERS = "kafka:9092";
	public static final String REPLAY_FROM_BEGINNING = "earliest";
	private KafkaPublisher kp;
	private KafkaSubscriber ks;

	public ReplicationManager(JavaDirectory rep) {
		start(rep);
	}


	private void start(JavaDirectory rep) {

		kp = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		ks = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), REPLAY_FROM_BEGINNING);
		ks.start(false, new ProcessOperation(rep));
	}
	

	public void publish(String operation, String params) {
		SyncPoint.getInstance().waitForResult(kp.publish(TOPIC, operation, params));
	}

}