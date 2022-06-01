package zookeeper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import jakarta.inject.Singleton;


@Singleton
public class Zookeeper implements Watcher {

	private ZooKeeper _client;
	private int timeout = 5000;
	private static final String KAFKA = "kafka";
	private String root = "/directory";
	private String sufix = "/guid-n_";
	private String currentLeader = "";
	private String primaryPath = "";
	private static Zookeeper inst = null;
	

	private Zookeeper() throws Exception {
		this.connect(KAFKA, timeout);
	}
	
	public static Zookeeper getInstance() throws Exception
    {
        if (inst == null)
        	inst = new Zookeeper();
 
        return inst;
    }

	public synchronized ZooKeeper client() {
		if (_client == null || !_client.getState().equals(ZooKeeper.States.CONNECTED)) {
			throw new IllegalStateException("ZooKeeper is not connected.");
		}
		return _client;
	}

	private void connect(String host, int timeout) throws IOException, InterruptedException {
		var connectedSignal = new CountDownLatch(1);
		_client = new ZooKeeper(host, timeout, (e) -> {
			System.err.println( e );
			if (e.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
				connectedSignal.countDown();
			}
		});
		connectedSignal.await();
	}

	public String createNode(String path, byte[] data, CreateMode mode) {
		try {
			return client().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		} catch (KeeperException.NodeExistsException x) {
			return path;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public List<String> getChildren(String path) {
		try {
			return client().getChildren(path, false);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}

	public List<String> getChildren(String path, Watcher watcher) {
		try {
			return client().getChildren(path, watcher);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	@Override
	public void process(WatchedEvent event) {
		
		switch (event.getType()) {
        case None:
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("Successfully connected to Zookeeper");
            } else {
                synchronized (_client) {
                    System.out.println("Disconnected from Zookeeper event");
                    _client.notifyAll();
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
        case NodeCreated:
        	try {
        		electLeader(event);
        	}
        	catch (Exception e) {
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
	
	public void createPersistent(byte[] serverURI) {
		createNode(root, serverURI, CreateMode.PERSISTENT);
	}
	
	public void createEphemerals(byte[] serverURI) {
		var newpath = createNode(root + sufix, serverURI, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.err.println(newpath);
	}
	
	public void watchEvents() {
		for(;;) {
			new Thread(() -> {
				getChildren(root, this::process);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}).start();
		}
	}
	
	public String getCurrentLeader() {
		return this.currentLeader;
	}

	public void setCurrentLeader(String currentLeader) {
		this.currentLeader = currentLeader;
	}

	public void electLeader(WatchedEvent watchedEvent) {

		List<String> children = getChildren(root, (Watcher) this);
		Collections.sort(children);
		
		if(currentLeader.equals("")) {
			setCurrentLeader(replaceSubString(children.get(0)));
			primaryPath = children.get(0);
			return;
		}
		
		// watchedEvent.getPath() -> contem o caminho do no que falhou
		// replace(root + "/", "") -> /directory/guid-n_i vai ficar guid-n_i
		String affectedNode = replaceSubString(watchedEvent.getPath());
		primaryPath = affectedNode;

		System.out.println("Node " + affectedNode + " crashed");

		// se o no que falhou nao for o current leader
		if (!getCurrentLeader().equalsIgnoreCase(affectedNode)) {
			System.out.println("No change in leader, some member nodes got partitioned or crashed");
			return;
		}

		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}

		setCurrentLeader(replaceSubString(children.get(0)));
		System.out.println("Successful re-election. Elected " + getCurrentLeader());
		// zooKeeper.exists(root + "/" + getCurrentLeader(), false);
	}

	private String replaceSubString(String path) {

		return path.replace(root + "/", "");
	}

	public String getPrimaryPath() {
		try {
			return new String(client().getData(primaryPath, false, null), StandardCharsets.UTF_8);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
