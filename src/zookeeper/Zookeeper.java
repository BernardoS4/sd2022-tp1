package zookeeper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
import util.Sleep;

import java.util.concurrent.atomic.AtomicReference;



@Singleton
public class Zookeeper implements Watcher {

	private ZooKeeper _client;
	private int timeout = 5000;
	private static final String KAFKA = "kafka";
	private String root = "/directory";
	private String sufix = "/guid-n_";
	private AtomicReference<String> currentLeader;
	private AtomicReference<String> primaryPath;
	private static Zookeeper inst = null;
	

	private Zookeeper() {
		try {
			this.connect(KAFKA, timeout);
			createPersistent();
			watchEvents();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Zookeeper getInstance()
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
        case NodeChildrenChanged:
        	electLeader(event);
            break;
		default:
			break;
		}
	}
	
	public void createPersistent() {
		createNode(root, new byte[0], CreateMode.PERSISTENT);
	}
	
	public void createEphemerals(byte[] serverURI) {
		String path = Arrays.toString(serverURI).concat("/dir");
		byte[] data = path.getBytes();
		var newpath = createNode(root + sufix, data, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.err.println(newpath);
	}
	
	public void watchEvents() {
		for(;;) {
			new Thread(() -> {
				getChildren(root, this::process);
				Sleep.ms(1000);
			}).start();
		}
	}
	
	public AtomicReference<String> getCurrentLeader() {
		return this.currentLeader;
	}

	public void setCurrentLeader(String currentLeader) {
		this.currentLeader.set(currentLeader);
	}

	public void electLeader(WatchedEvent watchedEvent) {

		List<String> children = getChildren(root, this);
		Collections.sort(children);
		
		if(getCurrentLeader().toString().equals("")) {
			setCurrentLeader(replaceSubString(children.get(0)));
			primaryPath.set(children.get(0));
			return;
		}

		String affectedNode = replaceSubString(watchedEvent.getPath());
		primaryPath.set(affectedNode);

		for (String nominee : children) {
			System.out.println("Nominee " + nominee);
		}
		setCurrentLeader(replaceSubString(children.get(0)));
	}

	private String replaceSubString(String path) {

		return path.replace(root + "/", "");
	}

	public String getPrimaryPath() {
		try {
			return new String(client().getData(primaryPath.toString(), false, null), StandardCharsets.UTF_8);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
