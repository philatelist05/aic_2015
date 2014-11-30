package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateResponseCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateStatus;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LocalCellWorkerTest {

    private LocalNodeCore mockedNodeCore;
    private ConnectionWorker mockedConnectionWorker;
    private Circuit mockedCircuit;

    @Before
    public void setUp() throws Exception {
        mockedNodeCore = mock(LocalNodeCore.class);
        mockedConnectionWorker = mock(ConnectionWorker.class);
        mockedCircuit = mock(Circuit.class);
    }

    @Test
    public void testCreateCell() throws Exception {
        CreateCell cell = new CreateCell((short) 1, null, null);
        LocalCellWorker cellWorker = new LocalCellWorker(mockedConnectionWorker, cell, null, mockedNodeCore);
        cellWorker.run();
        verifyZeroInteractions(mockedConnectionWorker);
        verifyZeroInteractions(mockedNodeCore);
    }

    @Test
    public void testCreateResponseCellWithCircuitIDCollision() throws Exception {
        ChainMetaData mockedChainMetaData = mock(ChainMetaData.class);
        SocksCallBack mockedCallBack = mock(SocksCallBack.class);
        when(mockedNodeCore.getCallBack(eq((short) 1))).thenReturn(mockedCallBack);
        when(mockedNodeCore.getChainMetaData(eq((short) 1))).thenReturn(mockedChainMetaData);
        when(mockedCircuit.getCircuitID()).thenReturn((short) 1);
        CreateResponseCell cell = new CreateResponseCell((short) 1, null, null, CreateStatus.CircuitIDAlreadyExists);
        LocalCellWorker cellWorker = new LocalCellWorker(mockedConnectionWorker, cell, mockedCircuit, mockedNodeCore);
        cellWorker.run();
        verify(mockedNodeCore).removeChain(eq((short) 1));
        verify(mockedNodeCore).createChain(mockedChainMetaData, mockedCallBack);
    }

    @Test
    public void testCreateResponseCellWithNoOngoingKeyExchange() throws Exception {
        ChainMetaData mockedChainMetaData = mock(ChainMetaData.class);
        SocksCallBack mockedCallBack = mock(SocksCallBack.class);
        when(mockedNodeCore.getCallBack(eq((short) 1))).thenReturn(mockedCallBack);
        when(mockedNodeCore.getChainMetaData(eq((short) 1))).thenReturn(mockedChainMetaData);
        when(mockedCircuit.getCircuitID()).thenReturn((short) 1);
        when(mockedCircuit.getDHKeyExchange()).thenReturn(null);
        CreateResponseCell cell = new CreateResponseCell((short) 1, null, null);
        LocalCellWorker cellWorker = new LocalCellWorker(mockedConnectionWorker, cell, mockedCircuit, mockedNodeCore);
        cellWorker.run();
        verifyZeroInteractions(mockedCallBack);
        verifyZeroInteractions(mockedChainMetaData);
        verifyZeroInteractions(mockedConnectionWorker);
    }
}