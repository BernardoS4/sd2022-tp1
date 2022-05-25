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
import org.apache.zookeeper.data.Stat;

import leaderElection.LeaderElection;



public class Zookeeper implements Watcher {

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

	public void initiate() throws Exception {

		String root = "/directory";
		
		//precisamos de verificar se a diretoria ja existe?
		/*Stat stat = new Stat();
		stat = _client.exists(root , false);
		if(stat == null) {
		
			var path = zk.createNode(root, new byte[0], CreateMode.PERSISTENT);
			System.err.println( path );
		}*/
		createNode(root, new byte[0], CreateMode.PERSISTENT);
		var newpath = createNode(root + "/guid-n_", new byte[0], CreateMode.EPHEMERAL_SEQUENTIAL);
		System.err.println( newpath );
		
		LeaderElection leaderElection = new LeaderElection();
		leaderElection.firstElection();
		
		getChildren(root, (e) -> {
			process(e)  ;
		});

		Thread.sleep(Integer.MAX_VALUE);
	}

	@Override
	public void process(WatchedEvent event) {
		
		LeaderElection leaderElection = new LeaderElection();
		
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
            	leaderElection.electLeader(event);
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
}
