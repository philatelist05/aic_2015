package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TargetWorkerTest {

    private TargetWorker targetWorker;
    private long timeout;
    private TargetForwarder targetForwarder;

    @Before
    public void setUp() throws Exception {
        Configuration config = ConfigurationFactory.getConfiguration();
        timeout = config.getTargetWorkerTimeout();
        targetForwarder = mock(TargetForwarder.class);
        targetWorker = new TargetWorker(mock(ConnectionWorker.class), targetForwarder);
        targetWorker.startForwarding();
    }

    @After
    public void tearDown() throws Exception {
        targetWorker.close();
    }

    @Test
    public void testSendNothingOnce() throws Exception {
        Thread.sleep(timeout + 100);
        verify(targetForwarder, times(1)).forward(aryEq(new byte[]{}));
    }

    @Test
    public void testSendNothingTwice() throws Exception {
        Thread.sleep(2 * timeout + 200);
        verify(targetForwarder, times(2)).forward(aryEq(new byte[]{}));
    }

    @Test
    public void testSendDataOnce() throws Exception {
        byte[] data = "Data".getBytes();
        targetWorker.sendData(data, 0l);
        Thread.sleep(timeout + 100);
        verify(targetForwarder, times(1)).forward(aryEq(data));
    }

    @Test
    public void testSendDataOnceFollowedByNoData() throws Exception {
        byte[] data = "Data".getBytes();
        targetWorker.sendData(data, 0l);
        Thread.sleep(2* timeout + 200);
        verify(targetForwarder).forward(aryEq(data));
        verify(targetForwarder).forward(aryEq(new byte[]{}));
    }

    @Test
    public void testSendDataCorrectOrdering() throws Exception {
        byte[] data1 = "Data1".getBytes();
        byte[] data2 = "Data2".getBytes();
        targetWorker.sendData(data2, 2l);
        targetWorker.sendData(data1, 1l);
        Thread.sleep(2* timeout + 200);
        verify(targetForwarder).forward(aryEq(ArrayUtils.addAll(data1, data2)));
        verify(targetForwarder).forward(aryEq(new byte[]{}));
    }

    @Test
    public void testSendDataWithGaps() throws Exception {
        byte[] data1 = "Data1".getBytes();
        byte[] data2 = "Data2".getBytes();
        targetWorker.sendData(data2, 3l);
        targetWorker.sendData(data1, 1l);
        Thread.sleep(2* timeout + 200);
        verify(targetForwarder, times(0)).forward(any());
    }
}