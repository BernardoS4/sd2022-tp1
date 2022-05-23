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
		String leader = getCurrentLeader();

		String currentNode = children.get(0).replace(root + "/", "");
		setCurrentLeader(currentNode);
		
		System.out.println("Leader is " + leader);
		//sempre que lider e alterado e apenas ele, notificar Watcher
		zooKeeper.exists(root + "/" + leader, (Watcher) this);
	}

	//so fazer caso o no que falhar for o leader
	public void reelectLeader(WatchedEvent watchedEvent) throws KeeperException, InterruptedException {

		//watchedEvent.getPath() -> contem o caminho do no que falhou
		//replace(root + "/", "") -> /directory/guid-n_i vai ficar guid-n_i
		String affectedNode = watchedEvent.getPath().replace(root + "/", "");

		System.out.println("Node " + affectedNode + " crashed");

		//se o no que falhou nao for o current leader
		if (!getCurrentLeader().equalsIgnoreCase(affectedNode)) {
			System.out.println("No change in leader, some member nodes got partitioned or crashed");
			// e preciso apagar?
			zooKeeper.delete(affectedNode, -1);
			return;
		}

		List<String> children = zooKeeper.getChildren(root, false);
		Collections.sort(children);

		//se der true, nao existe nos para fazer reeleicao logo todos crasharam
		if (children.isEmpty()) {
			System.out.println("Re-election not possible. Add nodes please.");
			return;
		}

		//print dos nomeados
		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}
		
		setCurrentLeader(children.get(0).replace(root + "/", ""));

		System.out.println("Successful re-election. Elected " + getCurrentLeader());

		zooKeeper.exists(root + "/" + getCurrentLeader(), (Watcher) this);
	}
}
