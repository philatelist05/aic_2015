package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.shortThat;
import static org.mockito.Mockito.*;

public class LocalNodeCoreTest {
    private static ConcurrentSkipListSet<Short> circuitIDs;
    private static ConcurrentHashMap<Short, ChainMetaData> chains;
    private static ConcurrentHashMap<Short, SocksCallBack> callbacks;
    private static ChainMetaData chain1;
    private static ChainMetaData chain2;
    private static ChainMetaData chain3;
    private static ConcurrentHashMap<Integer, ChainNodeMetaData> nodes;

    @BeforeClass
    public static void initTestData() {
        nodes = new ConcurrentHashMap<>();
        nodes.put(0, new ChainNodeMetaData(null, new Endpoint("localhost", 12345), null, "", "", ""));
        nodes.put(1, new ChainNodeMetaData(null, new Endpoint("localhost", 12346), null, "", "", ""));
        nodes.put(2, new ChainNodeMetaData(null, new Endpoint("localhost", 12347), null, "", "", ""));
        chain1 = new ChainMetaData((short) 1, new ConcurrentHashMap<>(nodes));
        chain2 = new ChainMetaData((short) 2, new ConcurrentHashMap<>(nodes));
        chain3 = new ChainMetaData((short) 3, new ConcurrentHashMap<>(nodes));
    }

    @Before
    public void setUp() {
        circuitIDs = mock(ConcurrentSkipListSet.class);
        chains = mock(ConcurrentHashMap.class);
        callbacks = mock(ConcurrentHashMap.class);

    }

    @Test
    public void testGetChainMetaData() throws Exception {
        when(chains.get(eq((short) 1))).thenReturn(chain1);
        when(chains.get(eq((short) 2))).thenReturn(chain2);
        when(chains.get(eq((short) 3))).thenReturn(chain3);
        when(chains.get(shortThat(new BaseMatcher<Short>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof Short) {
                    if (((Short) item).compareTo((short) 3) > 0 || ((Short) item).compareTo((short) 1) < 0) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        }))).thenReturn(null);

        Endpoint endpoint = new Endpoint("localhost", 12345);
        LocalNodeCore nodeCore = new LocalNodeCore(endpoint, circuitIDs, chains, callbacks);

        assertEquals(chain1, nodeCore.getChainMetaData((short) 1));
        assertEquals(chain2, nodeCore.getChainMetaData((Short.valueOf((short) 2))));
        assertEquals(chain3, nodeCore.getChainMetaData((Short.valueOf((short) 3))));
        assertNull(nodeCore.getChainMetaData((Short.valueOf((short) 4))));
        assertNull(nodeCore.getChainMetaData((Short.valueOf((short) 0))));
    }
/*
    @Test
    public void testGetCallBack() throws Exception {
        fail("Need to implement SocksCallback first..");
    }

    @Test
    public void testCreateChain() throws Exception {
        fail("Need to implement SocksCallback first..");
        LocalNodeCore nodeCore = mock(LocalNodeCore.class);
        SocksCallBack callback = null;    //FIXME update with implementation
        nodeCore.createChain(chain1, callback);
        verify(nodeCore).sendCell(any(CreateCell.class), any(Circuit.class), callback);
    }        */

    @Test
    public void testSendCell() throws Exception {

    }

    @Test
    public void testGetAndReserveFreeCircuitID() throws Exception {

    }

    @Test
    public void testRemoveChain() throws Exception {

    }
}