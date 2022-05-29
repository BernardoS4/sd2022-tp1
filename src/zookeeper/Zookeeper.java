package zookeeper;

import java.io.IOException;
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
import leaderElection.LeaderElection;


@Singleton
public class Zookeeper /*implements Watcher*/ {

	private ZooKeeper _client;
	private int timeout = 5000;

	public Zookeeper(String servers) throws Exception {
		this.connect(servers, timeout);
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
}
