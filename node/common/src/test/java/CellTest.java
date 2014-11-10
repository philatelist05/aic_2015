import at.ac.tuwien.aic.ws14.group2.onion.node.common.*;
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

    private InputStream simulateCellTransfer(Cell cell) throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        cell.send(sink);

        byte[] wire = sink.toByteArray();
        assertEquals(Cell.CELL_BYTES, wire.length);

        return new ByteArrayInputStream(wire);
    }

    private String simulateTarget(DataCommand cmd) throws IOException {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        cmd.sendData(sink);

        return new String(sink.toByteArray(), "ASCII");
    }

    private String sendAndReceiveDataCell(InputStream input) throws IOException, DecodeException {
        short circuitID = 123;

        // build encrypted Data Relay Cell
        DataCommand dataCmd = new DataCommand(input);
        RelayCellPayload relayPayload = new RelayCellPayload(dataCmd).encrypt(null);
        RelayCell relayCell = new RelayCell(relayPayload, circuitID);

        // send cell
        InputStream source = simulateCellTransfer(relayCell);

        // receive cell
        Cell receivedCell = Cell.receive(source);
        assertTrue(receivedCell instanceof RelayCell);
        assertEquals(receivedCell.getCircuitID(), circuitID);
        RelayCell receivedRelayCell = (RelayCell)receivedCell;

        // decode command
        Command receivedCmd = receivedRelayCell.getPayload().decrypt(null).decode();
        assertTrue(receivedCmd instanceof DataCommand);
        DataCommand receivedDataCmd = (DataCommand)receivedCmd;

        return simulateTarget(receivedDataCmd);
    }

    @Test
    public void singleDataCell() throws IOException, DecodeException {
        InputStream input = getDataInput(shortText);

        String data = sendAndReceiveDataCell(input);
        assertEquals(shortText, data);

        try {
            new DataCommand(input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }

    @Test
    public void manyDataCells() throws IOException, DecodeException {
        InputStream input = getDataInput(longText);

        String part0 = sendAndReceiveDataCell(input);
        String part1 = sendAndReceiveDataCell(input);

        assertEquals(longText, part0 + part1);

        try {
            new DataCommand(input);
            fail();
        } catch (DecodeException ex) {
            // end of stream
        }
    }
}
