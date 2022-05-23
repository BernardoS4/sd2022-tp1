package leaderElection;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.*;

public class LeaderElection {

	private String root = "/directory";
	private ZooKeeper zooKeeper;
	private String currentLeader;

	public LeaderElection() {

		currentLeader = "";
	}

	
	public String getCurrentLeader() {
		return this.currentLeader;
	}

	public void setCurrentLeader(String currentLeader) {
		this.currentLeader = currentLeader;
	}

	public void electLeader() throws KeeperException, InterruptedException {

		List<String> children = zooKeeper.getChildren(root, false);
		Collections.sort(children);

		String currentNode = children.get(0).replace(root + "/", "");
		setCurrentLeader(currentNode);
		
		System.out.println("Leader is " + getCurrentLeader());
		zooKeeper.exists(root + "/" + getCurrentLeader(), (Watcher) this);
	}

	public void reelectLeader(WatchedEvent watchedEvent) throws KeeperException, InterruptedException {

		String affectedNode = watchedEvent.getPath().replace(root + "/", "");

		System.out.println("Node " + affectedNode + " crashed");

		if (!getCurrentLeader().equalsIgnoreCase(affectedNode)) {
			System.out.println("No change in leader, some member nodes got partitioned or crashed");
			// e preciso apagar?
			zooKeeper.delete(affectedNode, -1);
			return;
		}

		List<String> children = zooKeeper.getChildren(root, false);
		Collections.sort(children);

		if (children.isEmpty()) {
			System.out.println("Re-election not possible. Add nodes please.");
			return;
		}

		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}
		
		setCurrentLeader(children.get(0).replace(root + "/", ""));

		System.out.println("Successful re-election. Elected " + getCurrentLeader());

		zooKeeper.exists(root + "/" + getCurrentLeader(), (Watcher) this);
	}
}
