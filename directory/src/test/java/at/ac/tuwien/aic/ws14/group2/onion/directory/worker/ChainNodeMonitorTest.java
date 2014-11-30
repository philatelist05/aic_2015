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
    private static ChainNodeInformation firstNodeInfo;
    private static ChainNodeInformation secondNodeInfo;
    private static ChainNodeInformation thirdNodeInfo;
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
        firstNodeInfo = new ChainNodeInformation(23456, "localhost", Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));                                      */
        firstNodeInfo = new ChainNodeInformation(23456, "localhost", "PUBLICKEYFAKE");
        secondNodeInfo = new ChainNodeInformation(34567, "localhost", "PUBLICKEYFAKEONE");
        thirdNodeInfo = new ChainNodeInformation(45678, "localhost", "PUBLICKEYFAKETWO");

    }

    @Test
    public void testDeactivateInactiveNode() {
        logger.info("Testing deactivation of inactive node");

        ConcurrentSkipListSet<ChainNodeInformation> activeNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        activeNodes.add(firstNodeInfo);

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 2, ChronoUnit.MILLIS)),
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(deadNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, timeout));
        chainNodeMonitor.run();

        verify(registry).getActiveNodes();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry).deactivate(firstNodeInfo);

    }

    @Test
    public void testDoNotDeactivateActiveNode() {
        logger.info("Testing deactivation of inactive node");

        ConcurrentSkipListSet<ChainNodeInformation> activeNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        activeNodes.add(firstNodeInfo);

        NodeUsage activeNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now()),
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(activeNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, 5000));
        chainNodeMonitor.run();

        verify(registry).getActiveNodes();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry, never()).deactivate(firstNodeInfo);

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

    @Test
    public void testDeactivateInactiveAndDoNotDeactivateActiveNodes(){
        ConcurrentSkipListSet<ChainNodeInformation> activeNodes = new ConcurrentSkipListSet<>();
        activeNodes.add(firstNodeInfo);
        activeNodes.add(secondNodeInfo);
        activeNodes.add(thirdNodeInfo);

        NodeUsage firstActiveNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(3)),
                dateTimeFormatter.format(LocalDateTime.now()),
                2,
                3
        );

        NodeUsage secondActiveNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(4)),
                dateTimeFormatter.format(LocalDateTime.now()),
                2,
                3
        );

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(3)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout, ChronoUnit.MILLIS)),
                3,
                4
        );

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodes()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(firstActiveNodeUsage);
        when(registry.getLastNodeUsage(secondNodeInfo)).thenReturn(secondActiveNodeUsage);
        when(registry.getLastNodeUsage(thirdNodeInfo)).thenReturn(deadNodeUsage);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, timeout));
        chainNodeMonitor.run();

        verify(registry).getActiveNodes();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry, never()).deactivate(firstNodeInfo);

        verify(registry).getLastNodeUsage(secondNodeInfo);
        verify(registry, never()).deactivate(secondNodeInfo);

        verify(registry).getLastNodeUsage(thirdNodeInfo);
        verify(registry).deactivate(thirdNodeInfo);
    }

}