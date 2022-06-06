package tp1.impl.servers.common;


import java.util.List;
import kafka.KafkaPublisher;
import kafka.KafkaSubscriber;
import kafka.ProcessOperation;
import kafka.sync.SyncPoint;

public class ReplicationManager {

	static final String TOPIC = "single_partition_topic";
	static final String KAFKA_BROKERS = "kafka:9092";
	static final String REPLAY_FROM_BEGINNING = "earliest";
	private KafkaPublisher kp;
	private KafkaSubscriber ks;

	public ReplicationManager(JavaRepDirectory rep) {
		start(rep);
	}

	private void start(JavaRepDirectory rep) {

		kp = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		ks = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), REPLAY_FROM_BEGINNING);
		ks.start(false, new ProcessOperation(rep));
	}

	public void publish(String operation, String params) {
		SyncPoint.getInstance().waitForResult(kp.publish(TOPIC, operation, params));
	}

}