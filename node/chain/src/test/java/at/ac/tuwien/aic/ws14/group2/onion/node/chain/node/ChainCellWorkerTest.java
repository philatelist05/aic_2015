package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSASignAndVerify;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Vector;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Thomas on 30.11.2014.
 */
public class ChainCellWorkerTest {

    private ConnectionWorkerFactory mockedConnectionWorkerFactory;
    private ConnectionWorker mockedConnectionWorker;
    private Circuit mockedCircuit;

    private static BigInteger g = DHKeyExchange.generateRelativePrime();
    private static BigInteger p = DHKeyExchange.generateRelativePrime();
    private static KeyPair keyPair;

    private Endpoint incomingEndpoint;
    private Circuit incomingCircuit;
    private Endpoint outgoingEndpoint;


    @BeforeClass
    public static void init() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        keyPair = new RSAKeyGenerator().generateKeys(0);
    }

    @Before
    public void setUp() throws Exception {
        mockedConnectionWorkerFactory = mock(ConnectionWorkerFactory.class);
        mockedConnectionWorker = mock(ConnectionWorker.class);
        mockedCircuit = mock(Circuit.class);

        incomingEndpoint = new Endpoint("1.1.1.1", 10);
        incomingCircuit = new Circuit(incomingEndpoint);

        outgoingEndpoint = new Endpoint("2.2.2.2", 20);
    }

    private DHHalf createDHHalf() throws Exception {
        DHKeyExchange dh = new DHKeyExchange();
        return new DHHalf(g, p, dh.initExchange(p, g));
    }

    private EncryptedDHHalf createEncryptedDHHalf() throws Exception {
        return createDHHalf().encrypt(keyPair.getPublic());
    }

    @Test
    public void testCreateCell() throws Exception {
        Endpoint fakeEndpoint = new Endpoint("localhost", 12345);
        DHKeyExchange dh = new DHKeyExchange();
        DHHalf dhHalf = new DHHalf(g, p, dh.initExchange(p, g));
        EncryptedDHHalf encDHHalf = dhHalf.encrypt(keyPair.getPublic());

        CreateCell cell = new CreateCell((short)1, outgoingEndpoint, encDHHalf);
        ChainCellWorker chainCellWorker = new ChainCellWorker(mockedConnectionWorker, cell, null, fakeEndpoint, keyPair.getPrivate(), mockedConnectionWorkerFactory);

        final Vector<Circuit> addedCircuits = new Vector<>();
        final Vector<byte[]> secretKeys = new Vector<>();

        Mockito.doAnswer((InvocationOnMock invocation) -> {
                Circuit arg = invocation.getArgumentAt(0, Circuit.class);
                addedCircuits.add(arg);
                assertEquals(1, arg.getCircuitID());
                return null;
            }).when(mockedConnectionWorker).addCircuit(any(Circuit.class));
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            CreateResponseCell arg = (CreateResponseCell)invocation.getArgumentAt(0, Cell.class);

            secretKeys.add(dh.completeExchange(arg.getDhPublicKey()));

            assertEquals(1, arg.getCircuitID());
            assertTrue(RSASignAndVerify.verifySig(arg.getDhPublicKey(), keyPair.getPublic(), arg.getSignature()));
            return null;
        }).when(mockedConnectionWorker).sendCell(any(Cell.class));

        chainCellWorker.run();

        Mockito.verify(mockedConnectionWorker).addCircuit(any(Circuit.class));
        Mockito.verify(mockedConnectionWorker).sendCell(any(Cell.class));

        assertArrayEquals(addedCircuits.elementAt(0).getSessionKey(), secretKeys.elementAt(0));
    }
}
