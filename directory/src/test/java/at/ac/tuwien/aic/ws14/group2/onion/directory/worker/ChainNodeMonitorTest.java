package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
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
    static final Logger logger = LogManager.getLogger(ChainNodeMonitorTest.class.getName());
    private static ConcurrentSkipListSet<Integer> emptyNodeSet;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private static Configuration configuration;
    private static long timeout = 1000;
    private final int firstNodeInfo = 1;
    private final int secondNodeInfo = 2;
    private final int thirdNodeInfo = 3;

    @BeforeClass
    public static void startUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        logger.info("Setting up Test environment");
        emptyNodeSet = new ConcurrentSkipListSet<>();

        configuration = mock(Configuration.class);
        when(configuration.getDirectoryAutoStartRegion()).thenReturn(null);
        when(configuration.getDirectoryNumberOfNodes()).thenReturn((short) -1);
        when(configuration.getDirectoryNodeHeartbeatTimeout()).thenReturn(1000L);
        timeout = configuration.getDirectoryNodeHeartbeatTimeout();
    }

    @Test
    public void testDeactivateInactiveNode() {
        ConcurrentSkipListSet<Integer> activeNodes = new ConcurrentSkipListSet<>();
        activeNodes.add(firstNodeInfo);

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 2, ChronoUnit.MILLIS)),
                0l, 0l, 0l, 0l, 0l);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(deadNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, configuration));
        chainNodeMonitor.run();

        verify(registry).getActiveNodeIDs();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry).deactivate(firstNodeInfo);

    }

    @Test
    public void testDoNotDeactivateActiveNode() {
        ConcurrentSkipListSet<Integer> activeNodes = new ConcurrentSkipListSet<>();
        activeNodes.add(firstNodeInfo);

        NodeUsage activeNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now()),
                0l, 0l, 0l, 0l, 0l);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(activeNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, configuration));
        chainNodeMonitor.run();

        verify(registry).getActiveNodeIDs();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry, never()).deactivate(firstNodeInfo);

    }

    @Test
    public void testNoChangeInRegistryWhenNoActiveNodes() throws TException, InterruptedException {
        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(emptyNodeSet);

        when(configuration.getDirectoryNodeHeartbeatTimeout()).thenReturn(0L);
        Thread chainNodeMonitorThread = new Thread(new ChainNodeMonitor(registry, configuration));
        chainNodeMonitorThread.run();

        verify(registry, never()).activate(any(Integer.class));
        verify(registry, never()).deactivate(any(Integer.class));
        verify(registry, never()).getLastNodeUsage(any(Integer.class));
        when(configuration.getDirectoryNodeHeartbeatTimeout()).thenReturn(1000L);
    }

    @Test
    public void testDeactivateInactiveAndDoNotDeactivateActiveNodes(){
        ConcurrentSkipListSet<Integer> activeNodes = new ConcurrentSkipListSet<>();
        activeNodes.add(firstNodeInfo);
        activeNodes.add(secondNodeInfo);
        activeNodes.add(thirdNodeInfo);

        NodeUsage firstActiveNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(3)),
                dateTimeFormatter.format(LocalDateTime.now()),
                2l, 3l, 0l, 0l, 0l
        );

        NodeUsage secondActiveNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(4)),
                dateTimeFormatter.format(LocalDateTime.now()),
                2l, 3l, 0l, 0l, 0l
        );

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusHours(3)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 5, ChronoUnit.MILLIS)),
                3l, 4l, 0l, 0l, 0l
        );

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(firstActiveNodeUsage);
        when(registry.getLastNodeUsage(secondNodeInfo)).thenReturn(secondActiveNodeUsage);
        when(registry.getLastNodeUsage(thirdNodeInfo)).thenReturn(deadNodeUsage);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, configuration));
        chainNodeMonitor.run();

        verify(registry).getActiveNodeIDs();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry, never()).deactivate(firstNodeInfo);

        verify(registry).getLastNodeUsage(secondNodeInfo);
        verify(registry, never()).deactivate(secondNodeInfo);

        verify(registry).getLastNodeUsage(thirdNodeInfo);
        verify(registry).deactivate(thirdNodeInfo);
    }

}