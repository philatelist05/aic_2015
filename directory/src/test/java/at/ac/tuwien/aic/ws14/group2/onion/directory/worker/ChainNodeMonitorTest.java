package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.junit.Before;
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
    private final int firstNodeInfo = 1;
    private final int secondNodeInfo = 2;
    private final int thirdNodeInfo = 3;
    private final int timeout = 1000;

    private ConcurrentSkipListSet<Integer> emptyNodeSet;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    @Before
    public void setUp() throws Exception {
        emptyNodeSet = new ConcurrentSkipListSet<>();
    }

    @Test
    public void testDeactivateInactiveNode() {
        ConcurrentSkipListSet<Integer> activeNodes = new ConcurrentSkipListSet<>();
        activeNodes.add(firstNodeInfo);

        NodeUsage deadNodeUsage = new NodeUsage(
                dateTimeFormatter.format(LocalDateTime.now().minusDays(1)),
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 2, ChronoUnit.MILLIS)),
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(deadNodeUsage);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, timeout));
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
                0,
                0);

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(activeNodeUsage);


        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, 5000));
        chainNodeMonitor.run();

        verify(registry).getActiveNodeIDs();
        verify(registry).getLastNodeUsage(firstNodeInfo);
        verify(registry, never()).deactivate(firstNodeInfo);

    }

    @Test
    public void testNoChangeInRegistryWhenNoActiveNodes() throws TException, InterruptedException {
        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(emptyNodeSet);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, 0));
        chainNodeMonitor.run();

        verify(registry, never()).activate(any(Integer.class));
        verify(registry, never()).deactivate(any(Integer.class));
        verify(registry, never()).getLastNodeUsage(any(Integer.class));
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
                dateTimeFormatter.format(LocalDateTime.now().minus(timeout * 5, ChronoUnit.MILLIS)),
                3,
                4
        );

        ChainNodeRegistry registry = mock(ChainNodeRegistry.class);
        when(registry.getActiveNodeIDs()).thenReturn(activeNodes);
        when(registry.getLastNodeUsage(firstNodeInfo)).thenReturn(firstActiveNodeUsage);
        when(registry.getLastNodeUsage(secondNodeInfo)).thenReturn(secondActiveNodeUsage);
        when(registry.getLastNodeUsage(thirdNodeInfo)).thenReturn(deadNodeUsage);

        Thread chainNodeMonitor = new Thread(new ChainNodeMonitor(registry, timeout * 5));
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