package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChainNodeMonitorTest {

    static final Logger logger = LogManager.getLogger(ChainNodeMonitor.class.getName());
    private static ChainNodeInformation information;
    private static ConcurrentSkipListSet<ChainNodeInformation> emptyNodeSet;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private int timeout = 1000;

    @BeforeClass
    public static void startUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        logger.info("Setting up Test environment");

        emptyNodeSet = new ConcurrentSkipListSet<>();
        /*Security.addProvider(new BouncyCastleProvider());

        RSAKeyGenerator keyGenerator = new RSAKeyGenerator();
        KeyPair rsaKeyPair = keyGenerator.generateKeys(0);
        privateKey = rsaKeyPair.getPrivate();
        information = new ChainNodeInformation(23456, "localhost", Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));                                      */
        information = new ChainNodeInformation(23456, "localhost", "PUBLICKEYFAKE");

    }

    @Test
    public void testDeactivateInactiveNode() {
        logger.info("Testing deactivation of inactive node");

        ConcurrentSkipListSet<ChainNodeInformation> activeNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        activeNodes.add(information);

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 2, ChronoUnit.MILLIS)),
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(information)).thenReturn(deadNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, timeout));
        chainNodeMonitor.run();

        verify(registry).getActiveNodes();
        verify(registry).getLastNodeUsage(information);
        verify(registry).deactivate(information);

    }

    @Test
    public void testDoNotDeactivateActiveNode() {
        logger.info("Testing deactivation of inactive node");

        ConcurrentSkipListSet<ChainNodeInformation> activeNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        activeNodes.add(information);

        NodeUsage activeNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now()),
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(information)).thenReturn(activeNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, 5000));
        chainNodeMonitor.run();

        verify(registry).getActiveNodes();
        verify(registry).getLastNodeUsage(information);
        verify(registry, never()).deactivate(information);

    }

    @Test
    public void testNoChangeWithNoActiveNodes() throws TException, InterruptedException {
        logger.info("Testing that nothing changes in the registry when there are no active nodes");

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(emptyNodeSet);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, 0));
        chainNodeMonitor.run();

        verify(registry, never()).activate(any(ChainNodeInformation.class));
        verify(registry, never()).deactivate(any(ChainNodeInformation.class));
        verify(registry, never()).getLastNodeUsage(any(ChainNodeInformation.class));
    }


}