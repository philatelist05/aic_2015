package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSAKeyGenerator;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.EncryptException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 10.11.2014.
 */
public class CellTest {

    private static final String shortText = "This is a short text.";

    // A text with 591 characters. 2 Data Cells are needed.
    private static final String longText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    private static final BigInteger g = DHKeyExchange.generateRelativePrime();
    private static final BigInteger p = DHKeyExchange.generateRelativePrime();
    private static KeyPair keyPair;

    @BeforeClass
    public static void init() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        keyPair = new RSAKeyGenerator().generateKeys(0);
    }

    private InputStream getDataInput(String text) {
        byte[] data = text.getBytes(Charset.forName("ASCII"));
        return new ByteArrayInputStream(data);
    }

    private Cell simulateCellTransfer(Cell cell) throws IOException, DecodeException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        cell.send(sink);

        byte[] wire = sink.toByteArray();
        assertEquals(Cell.CELL_BYTES, wire.length);

        return Cell.receive(new ByteArrayInputStream(wire));
    }

    private String simulateTarget(DataCommand cmd) throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        cmd.sendData(sink);

        return new String(sink.toByteArray(), "ASCII");
    }

    private String sendAndReceiveDataCommand(DataCommand dataCmd) throws Exception {
        short circuitID = 123;

        byte[] key1 = createSessionKey();
        byte[] key2 = createSessionKey();

        assertFalse(Arrays.equals(key1, key2));

        // build encrypted Data Relay Cell
        RelayCellPayload relayPayload = new RelayCellPayload(dataCmd).encrypt(key1).encrypt(key2);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(key2).decrypt(key1).decode();
        assertTrue(receivedCmd instanceof DataCommand);

        DataCommand receivedDataCmd = (DataCommand)receivedCmd;
        assertEquals(dataCmd.getSequenceNumber(), receivedDataCmd.getSequenceNumber());

        return simulateTarget(receivedDataCmd);
    }

    private DHHalf createDHHalf() {
        return new DHHalf(g, p, new byte[] {1, 2, 3});
    }

    private byte[] createDHPublicKey() {
        return new byte[] {1, 2, 3};
    }

    private byte[] createSignature() {
        return new byte[] {1, 2, 3};
    }

    private byte[] createSessionKey() throws Exception {
        DHKeyExchange keyExchange = new DHKeyExchange();
        keyExchange.initExchange(DHKeyExchange.generateRelativePrime(), DHKeyExchange.generateRelativePrime());

        BigInteger p = DHKeyExchange.generateRelativePrime();
        BigInteger g = DHKeyExchange.generateRelativePrime();

        DHKeyExchange keyExchangeA = new DHKeyExchange();
        DHKeyExchange keyExchangeB = new DHKeyExchange();

        keyExchangeA.initExchange(p, g);
        byte[] publicKeyB = keyExchangeB.initExchange(p,g);

        return keyExchangeA.completeExchange(publicKeyB);
    }

    @Test
    public void singleDataCommand() throws Exception {
        InputStream input = getDataInput(shortText);

        DataCommand dataCmd = new DataCommand(10l, input);
        String data = sendAndReceiveDataCommand(dataCmd);
        assertEquals(shortText, data);

        try {
            new DataCommand(10l, input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }

    @Test
    public void singleDataCommand2() throws Exception {
        byte[] rawData = shortText.getBytes();

        DataCommand dataCmd = new DataCommand(10l, rawData);

        String data = sendAndReceiveDataCommand(dataCmd);
        assertEquals(shortText, data);
    }

    @Test
    public void manyDataCommands() throws Exception {
        InputStream input = getDataInput(longText);

        DataCommand dataCmd = new DataCommand(10l, input);
        String part0 = sendAndReceiveDataCommand(dataCmd);

        dataCmd = new DataCommand(10l, input);
        String part1 = sendAndReceiveDataCommand(dataCmd);

        assertEquals(longText, part0 + part1);

        try {
            new DataCommand(10l, input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }

    @Test
    public void createCell() throws IOException, DecodeException, EncryptException, DecryptException {
        short circuitID = 123;
        Endpoint endpoint = new Endpoint("8.8.8.8", 80);
        DHHalf dh = createDHHalf();
        EncryptedDHHalf encDH = dh.encrypt(keyPair.getPublic());

        CreateCell createCell = new CreateCell(circuitID, endpoint, encDH);
        Cell receivedCell = simulateCellTransfer(createCell);

        assertTrue(receivedCell instanceof CreateCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        CreateCell receivedCreateCell = (CreateCell)receivedCell;

        assertEquals(endpoint, receivedCreateCell.getEndpoint());

        DHHalf dhHalf = receivedCreateCell.getDHHalf().decrypt(keyPair.getPrivate());
        assertEquals(dh.getG(), dhHalf.getG());
        assertEquals(dh.getP(), dhHalf.getP());
        assertArrayEquals(dh.getPublicKey(), dhHalf.getPublicKey());
    }

    @Test
    public void createResponseCell() throws IOException, DecodeException {
        short circuitID = 123;
        byte[] dhPublicKey = createDHPublicKey();
        byte[] signature = createSignature();

        CreateResponseCell createResultCell = new CreateResponseCell(circuitID, dhPublicKey, signature, CreateStatus.CircuitIDAlreadyExists);
        Cell receivedCell = simulateCellTransfer(createResultCell);

        assertTrue(receivedCell instanceof CreateResponseCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        CreateResponseCell receivedCreateResponseCell = (CreateResponseCell)receivedCell;

        assertArrayEquals(dhPublicKey, receivedCreateResponseCell.getDhPublicKey());
        assertArrayEquals(signature, receivedCreateResponseCell.getSignature());
        assertEquals(CreateStatus.CircuitIDAlreadyExists, receivedCreateResponseCell.getStatus());
    }

    @Test
    public void destroyCell() throws IOException, DecodeException {
        short circuitID = 123;

        DestroyCell destroyCell = new DestroyCell(circuitID);
        Cell receivedCell = simulateCellTransfer(destroyCell);

        assertTrue(receivedCell instanceof DestroyCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
    }

    @Test
    public void extendCommand() throws Exception {
        short circuitID = 123;
        Endpoint endpoint = new Endpoint("8.8.8.8", 80);
        DHHalf dh = createDHHalf();
        EncryptedDHHalf encDH = dh.encrypt(keyPair.getPublic());
        byte[] key = createSessionKey();

        ExtendCommand extendCommand = new ExtendCommand(endpoint, g, p, encDH);
        RelayCellPayload relayPayload = new RelayCellPayload(extendCommand).encrypt(key);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(key).decode();
        assertTrue(receivedCmd instanceof ExtendCommand);
        ExtendCommand receivedExtendCmd = (ExtendCommand)receivedCmd;

        assertEquals(endpoint, receivedExtendCmd.getEndpoint());

        DHHalf dhHalf = receivedExtendCmd.getDHHalf().decrypt(keyPair.getPrivate());
        assertEquals(dh.getG(), dhHalf.getG());
        assertEquals(dh.getP(), dhHalf.getP());
        assertArrayEquals(dh.getPublicKey(), dhHalf.getPublicKey());
    }

    @Test
    public void extendResponseCommand() throws Exception {
        short circuitID = 123;
        byte[] dhPublicKey = createDHPublicKey();
        byte[] signature = createSignature();
        byte[] key = createSessionKey();

        ExtendResponseCommand extendResponseCommand = new ExtendResponseCommand(dhPublicKey, signature);
        RelayCellPayload relayPayload = new RelayCellPayload(extendResponseCommand).encrypt(key);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(key).decode();
        assertTrue(receivedCmd instanceof ExtendResponseCommand);
        ExtendResponseCommand receivedExtendResponseCmd = (ExtendResponseCommand)receivedCmd;

        assertArrayEquals(dhPublicKey, receivedExtendResponseCmd.getDHPublicKey());
        assertArrayEquals(signature, receivedExtendResponseCmd.getSignature());
    }

    @Test
    public void connectCommand() throws Exception {
        short circuitID = 123;
        Inet4Address address = (Inet4Address)Inet4Address.getByName("127.0.0.1");
        int port = 80;
        Endpoint endpoint = new Endpoint(address, port);
        byte[] key = createSessionKey();

        ConnectCommand connectCommand = new ConnectCommand(endpoint);
        RelayCellPayload relayPayload = new RelayCellPayload(connectCommand).encrypt(key);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(key).decode();
        assertTrue(receivedCmd instanceof ConnectCommand);
        ConnectCommand receivedConnectCommand = (ConnectCommand)receivedCmd;

        assertEquals(endpoint, receivedConnectCommand.getEndpoint());
    }

    @Test
    public void connectResponseCommand() throws Exception {
        short circuitID = 123;
        byte[] key = createSessionKey();

        ConnectResponseCommand connectResponseCommand = new ConnectResponseCommand();
        RelayCellPayload relayPayload = new RelayCellPayload(connectResponseCommand).encrypt(key);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(key).decode();
        assertTrue(receivedCmd instanceof ConnectResponseCommand);
    }

    @Test(expected = DecodeException.class)
    public void invalidCell() throws IOException, DecodeException {
        byte[] packet = new byte[Cell.CELL_BYTES];
        for (int i = 0; i < packet.length; i++) {
            packet[i] = -1;
        }

        ByteArrayInputStream input = new ByteArrayInputStream(packet);

        Cell receivedCell = Cell.receive(input);
    }
}
