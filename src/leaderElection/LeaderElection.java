package leaderElection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;

import util.Operation;
import zookeeper.Zookeeper;

public class LeaderElection implements Watcher {

	private String root = "/directory";
	private String sufix = "/guid-n_";
	private static Zookeeper zooKeeper;
	private String currentLeader;
	// chave numero de sequencia da oper
	private Map<Long, Operation> results;
	private long version = -1L;

	public LeaderElection(String server) throws Exception {

		currentLeader = "";
		results = new HashMap<>();
		if (zooKeeper == null) {
			zooKeeper = new Zookeeper(server);
		}
		buildNodes();
		electLeader();
		watchEvents();
	}
	
	
	private void buildNodes() {
		zooKeeper.createNode(root, new byte[0], CreateMode.PERSISTENT);
		var newpath = zooKeeper.createNode(root + sufix, new byte[0], CreateMode.EPHEMERAL_SEQUENTIAL);
		System.err.println(newpath);
	}
	
	private void watchEvents() {
		zooKeeper.getChildren(root, (e) -> {
			process(e);
		});
	}
	
	@Override
	public void process(WatchedEvent event) {
		
		switch (event.getType()) {
        case None:
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("Successfully connected to Zookeeper");
            } else {
                synchronized (zooKeeper) {
                    System.out.println("Disconnected from Zookeeper event");
                    zooKeeper.notifyAll();
                }
            }
            break;
        case NodeDeleted:
            try {
            	electLeader(event);
            } catch (Exception e) {
                e.printStackTrace();
            } 
            break;        
        case NodeDataChanged:
            System.out.println("Leader updated progress of task");
            break;
		default:
			break;
    }
		System.err.println(event);
	}

	public String getCurrentLeader() {
		return this.currentLeader;
	}

	public void setCurrentLeader(String currentLeader) {
		this.currentLeader = currentLeader;
	}

	public void electLeader(WatchedEvent watchedEvent) {

		List<String> children = zooKeeper.getChildren(root, this);
		Collections.sort(children);
		
		if(currentLeader.equals("")) {
			setCurrentLeader(replaceSubString(children.get(0)));
			return;
		}
		
		// watchedEvent.getPath() -> contem o caminho do no que falhou
		// replace(root + "/", "") -> /directory/guid-n_i vai ficar guid-n_i
		String affectedNode = replaceSubString(watchedEvent.getPath());

		System.out.println("Node " + affectedNode + " crashed");

		// se o no que falhou nao for o current leader
		if (!getCurrentLeader().equalsIgnoreCase(affectedNode)) {
			System.out.println("No change in leader, some member nodes got partitioned or crashed");
			return;
		}

		// print dos nomeados
		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}

		setCurrentLeader(replaceSubString(children.get(0)));
		System.out.println("Successful re-election. Elected " + getCurrentLeader());
		// zooKeeper.exists(root + "/" + getCurrentLeader(), false);
	}

	public void firstElection() {

		List<String> children = zooKeeper.getChildren(root, this);
		Collections.sort(children);
		String leader = getCurrentLeader();

		String currentNode = replaceSubString(children.get(0));
		setCurrentLeader(currentNode);

		System.out.println("Leader is " + leader);
		// zooKeeper.exists(root + "/" + leader, false);
	}

	private String replaceSubString(String path) {

		return path.replace(root + "/", "");
	}

	/**
	 * Waits for version to be at least equals to n
	 */
	public synchronized void waitForVersion(long n, int waitPeriod) {
		while (version < n) {
			try {
				this.wait(waitPeriod);
			} catch (InterruptedException e) {
			}
		}
	}
/*
	
	public synchronized T waitForResult(long n) {
		waitForVersion(n, Integer.MAX_VALUE);
		return results.remove(n);
	}

	
	public synchronized void setResult(long n, T result) {
		results.put(n, result);
		version = n;
		this.notifyAll();
	}

	
	public synchronized void setVersion(long n) {
		version = n;
		this.notifyAll();
	}

	public synchronized String toString() {
		return results.keySet().toString();
	}
	*/
}
