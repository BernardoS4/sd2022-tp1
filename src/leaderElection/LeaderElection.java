package leaderElection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.*;

public class LeaderElection {

	private String root = "/directory";
	private ZooKeeper zooKeeper;
	private String currentLeader;
	private Map<String, Integer> nodeVersion;

	public LeaderElection() {

		currentLeader = "";
		nodeVersion = new HashMap<>();
	}

	public String getCurrentLeader() {
		return this.currentLeader;
	}

	public void setCurrentLeader(String currentLeader) {
		this.currentLeader = currentLeader;
	}

	public void electLeader(WatchedEvent watchedEvent) throws KeeperException, InterruptedException {

		// watchedEvent.getPath() -> contem o caminho do no que falhou
		// replace(root + "/", "") -> /directory/guid-n_i vai ficar guid-n_i
		String affectedNode = replaceSubString(watchedEvent.getPath());

		System.out.println("Node " + affectedNode + " crashed");

		// se o no que falhou nao for o current leader
		if (!getCurrentLeader().equalsIgnoreCase(affectedNode)) {
			System.out.println("No change in leader, some member nodes got partitioned or crashed");
			return;
		}
	
		List<String> children = zooKeeper.getChildren(root, (Watcher) this);
		Collections.sort(children);

		// print dos nomeados
		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}

		setCurrentLeader(replaceSubString(children.get(0)));

		System.out.println("Successful re-election. Elected " + getCurrentLeader());

		zooKeeper.exists(root + "/" + getCurrentLeader(), false);
	}

	public void firstElection() throws KeeperException, InterruptedException {

		List<String> children = zooKeeper.getChildren(root, (Watcher) this);
		Collections.sort(children);
		String leader = getCurrentLeader();

		String currentNode = replaceSubString(children.get(0));
		setCurrentLeader(currentNode);

		System.out.println("Leader is " + leader);
		zooKeeper.exists(root + "/" + leader, false);
	}

	private String replaceSubString(String path) {

		return path.replace(root + "/", "");
	}
}
