package at.ac.tuwien.aic.ws14.group2.onion.node.local.heartbeat;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DirectoryNotReachableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class HeartBeatWorker implements Runnable {

    static final Logger logger = LogManager.getLogger(HeartBeatWorker.class.getName());

    private TSSLTransportFactory.TSSLTransportParameters clientParams;
    private final int THRIFT_PORT = 9090; //TODO get from config
    private ChainNodeInformation nodeInformation;
    private long sleepInterval;
    private AtomicLong currentRelayMsgCount;
    private AtomicLong currentCreateMsgCount;
    // TODO use JodaTime here!?
    private Date lastHeartBeat;
    private DateFormat dateFormat;

    public HeartBeatWorker(ChainNodeInformation nodeInformation, long sleepInterval) {
        this.nodeInformation = nodeInformation;
        this.sleepInterval = sleepInterval;
        clientParams = new TSSLTransportFactory.TSSLTransportParameters();
        clientParams.setTrustStore("keys/thrift-test.jks", "password");  //TODO use keystore with directory public key here!
        currentRelayMsgCount = new AtomicLong(0);
        currentCreateMsgCount = new AtomicLong(0);
        lastHeartBeat = new Date();
        dateFormat = DateFormat.getDateTimeInstance();
    }

    @Override
    public void run() {

        logger.debug("Creating SSL Transport using Thrift");
        TTransport transport = null;
        try {
            transport = TSSLTransportFactory.getClientSocket("localhost", THRIFT_PORT, 0, clientParams);
        } catch (TTransportException e) {
            logger.warn("Encountered TTransportException: " + e.getMessage());
            throw new DirectoryNotReachableException();
        }

        TProtocol protocol = new TBinaryProtocol(transport);
        DirectoryService.Client client = new DirectoryService.Client(protocol);

        while (true) {
            Date currentEndTime = new Date();
            long relayMsgCountSnapshot = currentRelayMsgCount.get();
            long createMsgCountSnapshot = currentCreateMsgCount.get();
            NodeUsage usage = new NodeUsage(dateFormat.format(lastHeartBeat), dateFormat.format(currentEndTime), relayMsgCountSnapshot, createMsgCountSnapshot);

            logger.debug("Trying to send heartbeat");
            boolean ret;
            try {
                ret = client.heartbeat(nodeInformation, usage);
            } catch (TException e) {
                logger.warn("Encountered TException: " + e.getMessage());
                throw new DirectoryNotReachableException();    // FIXME probably not good
            }

            if (ret) {
                logger.debug("Heartbeat successful!");
                lastHeartBeat = currentEndTime;
                currentRelayMsgCount.addAndGet(currentRelayMsgCount.getAndSet(0) - relayMsgCountSnapshot);
                currentCreateMsgCount.addAndGet(currentCreateMsgCount.getAndSet(0) - createMsgCountSnapshot);
            } else {
                logger.info("Heartbeat unsuccessful, need to register first");
                try {
                    client.registerNode(nodeInformation);
                } catch (TException e) {
                    logger.warn("Encountered TException: " + e.getMessage());
                    throw new DirectoryNotReachableException();       // FIXME probably not good
                }
            }
            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                logger.warn("Interrupted");
                break;
            }
        }
        transport.close();
    }
}
