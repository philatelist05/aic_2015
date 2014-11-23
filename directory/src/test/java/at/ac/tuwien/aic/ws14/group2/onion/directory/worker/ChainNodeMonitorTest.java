package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.thrift.TException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import org.junit.BeforeClass;
import org.junit.Test;


import java.security.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChainNodeMonitorTest {

    static final Logger logger = LogManager.getLogger(ChainNodeMonitor.class.getName());

    private static ChainNodeRegistry chainNodeRegistry;
    private int timeout = 10;

    private static ChainNodeInformation information;
    private static NodeUsage nodeUsage;
    private static PrivateKey privateKey;
    private long sleepInterval = 300;
    private long waitTime = 100;

    private static LocalDateTime lastSuccessfulHeartBeat = LocalDateTime.now();
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    @BeforeClass
    public static void startUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        logger.info("Setting up Test environment");
        Security.addProvider(new BouncyCastleProvider());

        RSAKeyGenerator keyGenerator = new RSAKeyGenerator();
        KeyPair rsaKeyPair = keyGenerator.generateKeys(0);
        privateKey = rsaKeyPair.getPrivate();
        information = new ChainNodeInformation(23456, "localhost", Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));

        LocalDateTime now = LocalDateTime.now();
        nodeUsage = new NodeUsage(lastSuccessfulHeartBeat.format(dateTimeFormatter), now.format(dateTimeFormatter), 3, 3);

        chainNodeRegistry.addNewChainNode(information);
        chainNodeRegistry.addNodeUsage(information, nodeUsage);
    }

    @Test
    public void testTimeParsing() {
        logger.info("Testing time parsing");
        fail("Not implemented yet");

    }

    @Test
    public void testRun() throws TException, InterruptedException {
        logger.info("Testing health");
        DirectoryService.Client client = mock(DirectoryService.Client.class);
        when(client.registerNode(information)).thenReturn(true);

        logger.info("Starting ChainNodeMonitor");
        Thread chainNodeMonitor = new Thread (new ChainNodeMonitor(chainNodeRegistry, timeout));
        chainNodeMonitor.start();
        when(client.heartbeat(information, nodeUsage)).thenReturn(true);

        Set<ChainNodeInformation> active = chainNodeRegistry.getActiveNodes();
        assertEquals(active, information);

        Thread.sleep(sleepInterval + waitTime);

        Set<ChainNodeInformation> notactive = chainNodeRegistry.getActiveNodes();
        boolean inactive = false;
        if(notactive.isEmpty()){
            inactive = true;
        }

        assertEquals(true, inactive);

    }


}