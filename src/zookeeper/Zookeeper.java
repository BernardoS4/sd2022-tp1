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
	private static final int TIMEOUT = 5000;
	private static final int SLEEP = 1000;
	private static final String KAFKA = "kafka";
	private String root = "/directory";
	private String sufix = "/guid-n_";
	private AtomicReference<String> currentLeader = new AtomicReference<>();
	private AtomicReference<String> primaryPath = new AtomicReference<>();
	private static Zookeeper inst = null;

	private Zookeeper() {
		try {
			this.connect(KAFKA, TIMEOUT);
			createPersistent();
			watchEvents();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Zookeeper getInstance() {
		if (inst == null) {
			inst = new Zookeeper();
		}

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
			System.err.println(e);
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
		String serverPath = new String(serverURI);
		byte[] data = serverPath.getBytes();
		var newpath = createNode(root + sufix, data, CreateMode.EPHEMERAL_SEQUENTIAL);
		System.err.println(newpath);
	}

	public void watchEvents() {
		new Thread(() -> {
			for (;;) {
				getChildren(root, (e) -> {
					process(e);
				});
				Sleep.ms(SLEEP);
			}
		}).start();
	}

	public String getCurrentLeader() {
		return currentLeader.get();
	}

	public void setCurrentLeader(String newLeader) {
		currentLeader.set(newLeader);
	}

	public void electLeader(WatchedEvent watchedEvent) {

		List<String> children = getChildren(root);
		Collections.sort(children);
		String newLeader = children.get(0);
		String currentLeader = getCurrentLeader();
		
		if (currentLeader == null || !currentLeader.equalsIgnoreCase(newLeader)) {
			for (String nominee : children) {
				System.out.println("Nominee " + nominee);
			}
			setCurrentLeader(newLeader);
			primaryPath.set(createPath(newLeader));
		}
	}

	private String createPath(String path) {
		return String.format("%s/%s", root, path);
	}

	public String getPrimaryURI() {
		try {
			System.out.println(" olaaaaaaaa     " + primaryPath.get());
			return new String(client().getData(primaryPath.get(), false, null), StandardCharsets.UTF_8);
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
