package at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSASignAndVerify;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.bouncycastle.util.encoders.Base64;

import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HeartBeatWorker implements Runnable {

    static final Logger logger = LogManager.getLogger(HeartBeatWorker.class.getName());

    private DirectoryService.Client client;
    private ChainNodeInformation nodeInformation;
    private long sleepInterval;
    private PrivateKey privateKey;

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

            LocalDateTime currentEndTime = LocalDateTime.now();
            long relayMsgCountSnapshot = UsageCollector.currentRelayMsgCount.get();
            long createMsgCountSnapshot = UsageCollector.currentCreateMsgCount.get();
            NodeUsage usage = new NodeUsage(lastSuccessfulHeartBeat.format(dateTimeFormatter), currentEndTime.format(dateTimeFormatter), relayMsgCountSnapshot, createMsgCountSnapshot);

            usage.setSignature(Base64.toBase64String(RSASignAndVerify.signData(usage.toString().getBytes(), privateKey)));


            logger.debug("Trying to send heartbeat");
            boolean ret;
            try {
                ret = client.heartbeat(nodeInformation, usage);
            } catch (TException e) {
                logger.warn("Encountered TException while trying to send heartbeat: " + e.getMessage());
                continue;
            }

            if (ret) {
                logger.info("Heartbeat successful!");
                lastSuccessfulHeartBeat = currentEndTime;
                UsageCollector.currentRelayMsgCount.addAndGet(UsageCollector.currentRelayMsgCount.getAndSet(0) - relayMsgCountSnapshot);
                UsageCollector.currentCreateMsgCount.addAndGet(UsageCollector.currentCreateMsgCount.getAndSet(0) - createMsgCountSnapshot);
            } else {
                logger.info("Heartbeat unsuccessful, need to register first");
                try {
                    ret = client.registerNode(nodeInformation);
                } catch (TException e) {
                    logger.warn("Encountered TException while trying to register node: " + e.getMessage());
                    continue;
                }
                if (ret) {
                    logger.info("Registered successfully!");
                } else {
                    logger.warn("Registration failed, trying again in {}ms", sleepInterval);
                }
            }
        }
    }
}
