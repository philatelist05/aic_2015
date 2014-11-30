package at.ac.tuwien.aic.ws14.group2.onion.common;

/**
 * Created by stefan on 30.11.14.
 */
public class Configuration {

	private String nodeCommonHost;
	private int nodeCommonPort;
	private int localNodeServerPort;
	private String localNodeListeningHost;
	private int localNodeNumCellWorkers;
	private long chainNodeHeartbeatInterval;
	private int chainNodeNumCellWorkers;
	private long directoryNodeHeartbeatTimeout;
	private int directoryNodeMinThriftWorker;
	private int directoryNodeMaxThriftWorker;
	private String targetServiceHost;
	private int targetServicePort;


	public Configuration(String nodeCommonHost,
	                     int nodeCommonPort,
	                     int localNodeServerPort,
	                     String localNodeListeningHost,
	                     int localNodeNumCellWorkers,
	                     long chainNodeHeartbeatInterval,
	                     int chainNodeNumCellWorkers,
	                     long directoryNodeHeartbeatTimeout,
	                     int directoryNodeMinThriftWorker,
	                     int directoryNodeMaxThriftWorker,
	                     String targetServiceHost,
	                     int targetServicePort) {

		this.nodeCommonHost = nodeCommonHost;
		this.nodeCommonPort = nodeCommonPort;
		this.localNodeServerPort = localNodeServerPort;
		this.localNodeListeningHost = localNodeListeningHost;
		this.localNodeNumCellWorkers = localNodeNumCellWorkers;
		this.chainNodeHeartbeatInterval = chainNodeHeartbeatInterval;
		this.chainNodeNumCellWorkers = chainNodeNumCellWorkers;
		this.directoryNodeHeartbeatTimeout = directoryNodeHeartbeatTimeout;
		this.directoryNodeMinThriftWorker = directoryNodeMinThriftWorker;
		this.directoryNodeMaxThriftWorker = directoryNodeMaxThriftWorker;
		this.targetServiceHost = targetServiceHost;
		this.targetServicePort = targetServicePort;
	}

	public String getNodeCommonHost() {
		return nodeCommonHost;
	}

	public int getNodeCommonPort() {
		return nodeCommonPort;
	}

	public int getLocalNodeServerPort() {
		return localNodeServerPort;
	}

	public String getLocalNodeListeningHost() {
		return localNodeListeningHost;
	}

	public int getLocalNodeNumCellWorkers() {
		return localNodeNumCellWorkers;
	}

	public long getChainNodeHeartbeatInterval() {
		return chainNodeHeartbeatInterval;
	}

	public int getChainNodeNumCellWorkers() {
		return chainNodeNumCellWorkers;
	}

	public long getDirectoryNodeHeartbeatTimeout() {
		return directoryNodeHeartbeatTimeout;
	}

	public int getDirectoryNodeMinThriftWorker() {
		return directoryNodeMinThriftWorker;
	}

	public int getDirectoryNodeMaxThriftWorker() {
		return directoryNodeMaxThriftWorker;
	}

	public String getTargetServiceHost() {
		return targetServiceHost;
	}

	public int getTargetServicePort() {
		return targetServicePort;
	}
}
