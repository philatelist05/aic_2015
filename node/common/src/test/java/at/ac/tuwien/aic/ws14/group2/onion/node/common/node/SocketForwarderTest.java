package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SocketForwarderTest {

    private SocketForwarder socketForwarder;
    private TargetWorker targetWorker;

    @After
    public void tearDown() throws Exception {
        socketForwarder.close();
    }

    @Test
    public void testSocketInputOneCell() throws Exception {
        InputStream in = new ByteArrayInputStream(randomByteArray(1));
        OutputStream out = new ByteArrayOutputStream();
        SocketFactory socketFactory = createSocketFactory(in, out);
        targetWorker = mock(TargetWorker.class);
        socketForwarder = createSocketForwarder(socketFactory, targetWorker);

        Thread.sleep(1000);
        verify(targetWorker, times(1)).sendCell(Matchers.<Cell>any());
    }

    @Test
    public void testSocketInputOneCellMaxLength() throws Exception {
        InputStream in = new ByteArrayInputStream(randomByteArray(DataCommand.MAX_DATA_LENGTH));
        OutputStream out = new ByteArrayOutputStream();
        SocketFactory socketFactory = createSocketFactory(in, out);
        targetWorker = mock(TargetWorker.class);
        socketForwarder = createSocketForwarder(socketFactory, targetWorker);

        Thread.sleep(1000);
        verify(targetWorker, times(1)).sendCell(Matchers.<Cell>any());
    }

    @Test
    public void testSocketInputTwoCells() throws Exception {
        InputStream in = new ByteArrayInputStream(randomByteArray(DataCommand.MAX_DATA_LENGTH + 1));
        OutputStream out = new ByteArrayOutputStream();
        SocketFactory socketFactory = createSocketFactory(in, out);
        targetWorker = mock(TargetWorker.class);
        socketForwarder = createSocketForwarder(socketFactory, targetWorker);

        Thread.sleep(1000);
        verify(targetWorker, times(2)).sendCell(Matchers.<Cell>any());
    }

    @Test
    public void testSocketInputTwoCellsMaxLength() throws Exception {
        InputStream in = new ByteArrayInputStream(randomByteArray(DataCommand.MAX_DATA_LENGTH * 2));
        OutputStream out = new ByteArrayOutputStream();
        SocketFactory socketFactory = createSocketFactory(in, out);
        targetWorker = mock(TargetWorker.class);
        socketForwarder = createSocketForwarder(socketFactory, targetWorker);

        Thread.sleep(1000);
        verify(targetWorker, times(2)).sendCell(Matchers.<Cell>any());
    }

    @Test
    public void testForward() throws Exception {
        InputStream in = new ByteArrayInputStream("".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SocketFactory socketFactory = createSocketFactory(in, out);
        targetWorker = mock(TargetWorker.class);
        socketForwarder = createSocketForwarder(socketFactory, targetWorker);

        byte[] data = "TestData".getBytes();
        socketForwarder.forward(data);
        assertArrayEquals(data, out.toByteArray());
    }

    private SocketFactory createSocketFactory(InputStream in, OutputStream out) throws IOException {
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(in);
        when(socket.getOutputStream()).thenReturn(out);
        return new FakeSocketFactory(socket);
    }

    private SocketForwarder createSocketForwarder(SocketFactory socketFactory, TargetWorker worker) throws IOException {
        Circuit circuit = mock(Circuit.class);
        when(circuit.getCircuitID()).thenReturn((short) 0);
        when(circuit.getSessionKey()).thenReturn(createSessionKey());
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getPort()).thenReturn(0);
        when(endpoint.getAddress()).thenReturn(InetAddress.getLocalHost());
        SocketForwarder forwarder = new SocketForwarder(circuit, socketFactory);
        forwarder.connectTo(endpoint);
        forwarder.setTargetWorkerCallback(worker);
        return forwarder;
    }

    private byte[] createSessionKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey skey = kgen.generateKey();
            return skey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            return new byte[]{};
        }
    }

    private byte[] randomByteArray(int length) {
        Random random = new Random();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}