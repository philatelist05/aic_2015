package at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSASignAndVerify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HeartBeatWorker implements Runnable {

    static final Logger logger = LogManager.getLogger(HeartBeatWorker.class.getName());

    private DirectoryService.Client client;
    private ChainNodeInformation nodeInformation;
    private long sleepInterval;
    private PrivateKey privateKey;
    private int nodeID = -1;

    private LocalDateTime lastSuccessfulHeartBeat;
    private DateTimeFormatter dateTimeFormatter;

    public HeartBeatWorker(DirectoryService.Client client, ChainNodeInformation nodeInformation, long sleepInterval, PrivateKey privateKey) {
        this.sleepInterval = sleepInterval;
        this.nodeInformation = nodeInformation;
        this.client = client;
        this.privateKey = privateKey;
        this.lastSuccessfulHeartBeat = LocalDateTime.now();
        this.dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    @Override
    public void run() {
        //TODO find a way to restart client when directory went down..
        while (true) {
            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                logger.warn("Interrupted");
                break;
            }

            if (Thread.interrupted()) {
                logger.warn("Interrupted");
                break;
            }

            LocalDateTime currentEndTime = LocalDateTime.now();

            ConnectionWorkerFactory factory = ConnectionWorkerFactory.getInstance();

            long circuitCountSnapshot = 0;
            long chainCountSnapshot = 0;
            long targetCountSnapshot = 0;

            // Factory is null if setCellWorkerFactory has not been called beforehand as it is the case with the HeartBeatWorkerTest.
            if (factory != null) {
                ConnectionWorkerFactory.Statistics stats = factory.collectUsageStatistics();

                if (stats != null) {
                    circuitCountSnapshot = stats.circuitCount;
                    chainCountSnapshot = stats.chainCount;
                    targetCountSnapshot = stats.targetCount;
                }
            }

            long relayMsgCountSnapshot = UsageStatistics.currentRelayMsgCount.get();
            long createMsgCountSnapshot = UsageStatistics.currentCreateMsgCount.get();

            NodeUsage usage = new NodeUsage(
                    lastSuccessfulHeartBeat.format(dateTimeFormatter),
                    currentEndTime.format(dateTimeFormatter),
                    relayMsgCountSnapshot,
                    createMsgCountSnapshot,
                    circuitCountSnapshot,
                    chainCountSnapshot,
                    targetCountSnapshot);
            usage.setSignature(Base64.toBase64String(RSASignAndVerify.signData(
                    usage.toString().getBytes(Charset.forName("UTF-8")), privateKey)));

            //logger.debug("Trying to send heartbeat");
            boolean ret;
            try {
                ret = client.heartbeat(nodeID, usage);
            } catch (TException e) {
                logger.warn("Encountered TException while trying to send heartbeat: " + e.getMessage());
                continue;
            }

            if (ret) {
                //logger.debug("Heartbeat with ID {} successful!", nodeID);
                lastSuccessfulHeartBeat = currentEndTime;
                UsageStatistics.currentRelayMsgCount.accumulateAndGet(relayMsgCountSnapshot, (current, update) -> current - update);
                UsageStatistics.currentCreateMsgCount.accumulateAndGet(createMsgCountSnapshot, (current, update) -> current - update);
            } else {
                logger.warn("Heartbeat unsuccessful, need to register first");
                try {
                    nodeID = client.registerNode(nodeInformation);
                } catch (TException e) {
                    logger.warn("Encountered TException while trying to register node: " + e.getMessage());
                    continue;
                }
                if (nodeID >= 0) {
                    logger.info("Registered with ID {} successfully!", nodeID);
                } else {
                    logger.warn("Registration failed, trying again in {}ms", sleepInterval);
                }
            }
        }
    }
}
