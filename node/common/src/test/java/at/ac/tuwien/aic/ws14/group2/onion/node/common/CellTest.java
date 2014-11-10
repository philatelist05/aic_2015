package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 10.11.2014.
 */
public class CellTest {

    private static final String shortText = "This is a short text.";

    // A text with 591 characters. 2 Data Cells are needed.
    private static final String longText = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    private InputStream getDataInput(String text) {
        byte[] data = text.getBytes(Charset.forName("ASCII"));
        return new ByteArrayInputStream(data);
    }

    private Cell simulateCellTransfer(Cell cell) throws IOException {
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

    private String sendAndReceiveDataCommand(InputStream input) throws IOException, DecodeException {
        short circuitID = 123;

        // build encrypted Data Relay Cell
        DataCommand dataCmd = new DataCommand(input);
        RelayCellPayload relayPayload = new RelayCellPayload(dataCmd).encrypt(null);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(null).decode();
        assertTrue(receivedCmd instanceof DataCommand);
        DataCommand receivedDataCmd = (DataCommand)receivedCmd;

        return simulateTarget(receivedDataCmd);
    }

    private byte[] createDH() {
        // TODO: use crypto classes
        byte[] data = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        for (int i = 1; i < data.length; i++) {
            data[i] = (byte)i;
        }
        return data;
    }

    private byte[] createSignature() {
        // TODO: use crypto classes
        byte[] data = new byte[Cell.SIGNATURE_BYTES];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)i;
        }
        return data;
    }

    @Test
    public void singleDataCommand() throws IOException, DecodeException {
        InputStream input = getDataInput(shortText);

        String data = sendAndReceiveDataCommand(input);
        assertEquals(shortText, data);

        try {
            new DataCommand(input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }

    @Test
    public void manyDataCommands() throws IOException, DecodeException {
        InputStream input = getDataInput(longText);

        String part0 = sendAndReceiveDataCommand(input);
        String part1 = sendAndReceiveDataCommand(input);

        assertEquals(longText, part0 + part1);

        try {
            new DataCommand(input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }

    @Test
    public void createResponseCell() throws IOException {
        short circuitID = 123;
        byte[] dh = createDH();
        byte[] signature = createSignature();

        CreateResponseCell createResultCell = new CreateResponseCell(circuitID, dh, signature);
        Cell receivedCell = simulateCellTransfer(createResultCell);

        assertTrue(receivedCell instanceof CreateResponseCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        CreateResponseCell receivedCreateResponseCell = (CreateResponseCell)receivedCell;

        assertArrayEquals(dh, receivedCreateResponseCell.getDiffieHellmanHalf());
        assertArrayEquals(signature, receivedCreateResponseCell.getSignature());
    }

    @Test
    public void destroyCell() throws IOException {
        short circuitID = 123;

        DestroyCell destroyCell = new DestroyCell(circuitID);
        Cell receivedCell = simulateCellTransfer(destroyCell);

        assertTrue(receivedCell instanceof DestroyCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
    }

    @Test
    public void extendResponseCommand() throws IOException, DecodeException {
        short circuitID = 123;
        byte[] dh = createDH();
        byte[] signature = createSignature();

        ExtendResponseCommand extendResponseCommand = new ExtendResponseCommand(dh, signature);
        RelayCellPayload relayPayload = new RelayCellPayload(extendResponseCommand).encrypt(null);
        RelayCell relayCell = new RelayCell(circuitID, relayPayload);

        // send and receive cell
        Cell receivedCell = simulateCellTransfer(relayCell);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(null).decode();
        assertTrue(receivedCmd instanceof ExtendResponseCommand);
        ExtendResponseCommand receivedExtendResponseCmd = (ExtendResponseCommand)receivedCmd;

        assertArrayEquals(dh, receivedExtendResponseCmd.getDiffieHellmanHalf());
        assertArrayEquals(signature, receivedExtendResponseCmd.getSignature());
    }
}
