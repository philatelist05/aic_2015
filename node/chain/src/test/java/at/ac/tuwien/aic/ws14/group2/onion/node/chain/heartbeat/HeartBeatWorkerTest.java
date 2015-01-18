package at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.*;

import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class HeartBeatWorkerTest {

    static final Logger logger = LogManager.getLogger(HeartBeatWorkerTest.class.getName());

    private static ChainNodeInformation information;
    private static PrivateKey privateKey;
    private long sleepInterval = 300;
    private long waitTime = 250;

    @BeforeClass
    public static void setUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        logger.info("Setting up Test environment");
        Security.addProvider(new BouncyCastleProvider());

        RSAKeyGenerator keyGenerator = new RSAKeyGenerator();
        KeyPair rsaKeyPair = keyGenerator.generateKeys(0);
        privateKey = rsaKeyPair.getPrivate();
        information = new ChainNodeInformation(12345, "localhost", Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));
    }

    @Test
    public void testRegistration() throws Exception {
        logger.info("Testing registration");
        DirectoryService.Client client = mock(DirectoryService.Client.class);
        when(client.heartbeat(anyInt(), anyObject())).thenReturn(false).thenReturn(true);
        when(client.registerNode(information)).thenReturn(1);

        logger.info("Starting HeartBeatWorker");
        Thread heartBeatWorkerThread = new Thread(new HeartBeatWorker(client, information, sleepInterval, privateKey));
        heartBeatWorkerThread.start();

        Thread.sleep(sleepInterval + waitTime);

        logger.info("Interrupting HeartBeatWorker");
        while (!heartBeatWorkerThread.isInterrupted()) {
            heartBeatWorkerThread.interrupt();
        }

        logger.info("Verifying results");
        verify(client).heartbeat(eq(-1), anyObject());
        verify(client).registerNode(information);
    }

    @Test
    public void testingHeartbeat() throws Exception {
        logger.info("Testing heartbeat");
        DirectoryService.Client client = mock(DirectoryService.Client.class);
        when(client.heartbeat(geq(0), anyObject())).thenReturn(true);
        when(client.heartbeat(lt(0), anyObject())).thenReturn(false);

        logger.info("Starting HeartBeatWorker");
        Thread heartBeatWorkerThread = new Thread(new HeartBeatWorker(client, information, sleepInterval, privateKey));
        heartBeatWorkerThread.start();

        Thread.sleep((sleepInterval + waitTime) * 5);

        logger.info("Interrupting HeartBeatWorker");
        while (!heartBeatWorkerThread.isInterrupted()) {
            heartBeatWorkerThread.interrupt();
        }

        logger.info("Verifying results");
        verify(client, atLeast(5)).heartbeat(geq(0), anyObject());
    }

    @Test
    public void testingFailingClient() throws Exception {
        logger.info("Testing heartbeat behaviour using failing client");
        DirectoryService.Client client = mock(DirectoryService.Client.class);
        when(client.heartbeat(anyInt(), anyObject())).thenThrow(new TException("Mocked exception!"));

        logger.info("Starting HeartBeatWorker");
        Thread heartBeatWorkerThread = new Thread(new HeartBeatWorker(client, information, sleepInterval, privateKey));
        heartBeatWorkerThread.start();

        Thread.sleep((sleepInterval + waitTime) * 5);

        logger.info("Interrupting HeartBeatWorker");
        while (!heartBeatWorkerThread.isInterrupted()) {
            heartBeatWorkerThread.interrupt();
        }

        logger.info("Verifying results");
        verify(client, atLeast(5)).heartbeat(eq(-1), anyObject());
    }
}