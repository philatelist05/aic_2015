package at.ac.tuwien.aic.ws14.group2.onion.shared;

/**
 * Created by stefan on 30.11.14.
 */
public class Configuration {

    private final String nodeCommonHost;
    private final int nodeCommonPort;
    private final int localNodeServerPort;
    private final String localNodeListeningHost;
    private final int localNodeNumCellWorkers;
    private final long chainNodeHeartbeatInterval;
    private final int chainNodeNumCellWorkers;
    private final long directoryNodeHeartbeatTimeout;
    private final int directoryNodeMinThriftWorker;
    private final int directoryNodeMaxThriftWorker;
    private final String targetServiceHost;
    private final int targetServicePort;
    private final long targetWorkerTimeout;
    private final boolean nodeCommonLocalMode;
    private final boolean directoryAutoStart;
    private final String directoryAutoStartRegion;
    private final short directoryNumberOfNodes;


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
                         int targetServicePort,
                         long targetWorkerTimeout,
                         boolean nodeCommonLocalMode,
                         boolean directoryAutoStart,
                         String directoryAutoStartRegion, short directoryNumberOfNodes) {

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
        this.targetWorkerTimeout = targetWorkerTimeout;
        this.nodeCommonLocalMode = nodeCommonLocalMode;
        this.directoryAutoStart = directoryAutoStart;
        this.directoryAutoStartRegion = directoryAutoStartRegion;
        this.directoryNumberOfNodes = directoryNumberOfNodes;
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

    public long getTargetWorkerTimeout() {
        return targetWorkerTimeout;
    }

    public boolean isLocalMode() {
        return nodeCommonLocalMode;
    }

    public boolean isDirectoryAutoStart() {
        return directoryAutoStart;
    }

    public String getDirectoryAutoStartRegion() {
        return directoryAutoStartRegion;
    }

    public short getDirectoryNumberOfNodes() {
        return directoryNumberOfNodes;
    }

}
