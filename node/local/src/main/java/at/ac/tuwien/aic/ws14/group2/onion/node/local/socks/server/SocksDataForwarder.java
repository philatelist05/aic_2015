package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksDataForwarder extends Thread implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksDataForwarder.class.getName());

	private final Short circuitId;
	private final LocalNodeCore localNodeCore;
	private final ServerSocket serverSocket;
	private final SortedMap<Short, byte[]> responseBuffer;
	private volatile boolean stop;
	private Socket clientSocket;

	public SocksDataForwarder(Short circuitId, LocalNodeCore localNodeCore) throws IOException {
		this.circuitId = circuitId;
		this.localNodeCore = localNodeCore;
		// Create socket for the actual data from the originator
		this.serverSocket = new ServerSocket(0);
		this.responseBuffer = new ConcurrentSkipListMap<>();
	}

	public int getLocalPort() {
		return this.serverSocket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return this.serverSocket.getInetAddress();
	}

	/**
	 * Send data back to the originator.
	 *
	 * @param sequenceNumber the sequence number associated with the DataCommand that contained the data
	 * @param data           the raw data contained in the data cell
	 */
	public void sendDataBack(Short sequenceNumber, byte[] data) {
		// TODO (KK) Send data back
	}

	@Override
	public void run() {
		if (!serverSocket.isBound())
			throw new IllegalStateException(
					"TCP listener socket not initialized");

		try {
			try {
				clientSocket = serverSocket.accept();

				byte[] buffer = new byte[DataCommand.MAX_DATA_LENGTH];
				InputStream inputStream = clientSocket.getInputStream();

				stop = false;
				while (!stop) {
					int actualBytesRead = inputStream.read(buffer);

					if (actualBytesRead < 1) {
						stop = true;
						continue;
					}

					// TODO (KK) Forward data to the circuit
				}
			} finally {
				serverSocket.close();
				clientSocket.close();
			}
		} catch (Exception e) {
			// The if-clause down here is because of what is described in
			// http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
			// under "What if a thread doesn't respond to Thread.interrupt?"

			// So the socket is closed when the ClientNetworkService is
			// closed and in that special case no error should be
			// propagated.
			if (!stop) {
				Thread.UncaughtExceptionHandler eh = Thread.currentThread()
						.getUncaughtExceptionHandler();
				if (eh != null)
					eh.uncaughtException(Thread.currentThread(), e);
				else
					logger.error("Uncaught exception in thread: " + Thread.currentThread().getName(), e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		stop = true;
		this.interrupt();
		serverSocket.close();
		clientSocket.close();
	}

}
