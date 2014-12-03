package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Bucket;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksDataForwarder extends Thread implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksDataForwarder.class.getName());

	private final Short circuitId;
	private final LocalNodeCore localNodeCore;
	private final ServerSocket serverSocket;
	private final PriorityBlockingQueue<Bucket> responseBuffer;
	private volatile boolean stop;
	private Socket clientSocket;
	private ResponseHandlerThread responseHandlerThread;

	public SocksDataForwarder(Short circuitId, LocalNodeCore localNodeCore) throws IOException {
		this.circuitId = circuitId;
		this.localNodeCore = localNodeCore;
		// Create socket for the actual data from the originator
		this.serverSocket = new ServerSocket(0);
		this.responseBuffer = new PriorityBlockingQueue<>();
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
		// Put the data into the priority queue
		this.responseBuffer.put(new Bucket(data, sequenceNumber));

		// Notify the thread about a new object in the queue if it waits for a missing number
		if (this.responseHandlerThread != null)
			this.responseHandlerThread.notifyAll();
	}

	@Override
	public void run() {
		if (!serverSocket.isBound())
			throw new IllegalStateException(
					"TCP listener socket not initialized");

		try {
			try {

				// Start listening for a connection
				clientSocket = serverSocket.accept();

				logger.info("Got TCP connection from originator for data transfer");

				// Start thread handling the responses
				this.responseHandlerThread = new ResponseHandlerThread(this.responseBuffer, clientSocket.getOutputStream());
				this.responseHandlerThread.setName("Response handler thread of " + this.getName());
				this.responseHandlerThread.setUncaughtExceptionHandler(this.getUncaughtExceptionHandler());
				this.responseHandlerThread.start();

				// Initialize variables for sending
				byte[] buffer = new byte[DataCommand.MAX_DATA_LENGTH];
				InputStream inputStream = clientSocket.getInputStream();

				logger.info("Start transferring data from the originator");
				// Start sending loop
				stop = false;
				while (!stop) {
					// Read as many bytes as possible but not more than buffer
					int actualBytesRead = inputStream.read(buffer);

					if (actualBytesRead < 1) {
						if (actualBytesRead < 0) // If at EOF, stop
							stop = true;
						continue;
					}

					// Forward data to the circuit
					byte[] payload = Arrays.copyOf(buffer, actualBytesRead);
					localNodeCore.sendData(circuitId, payload);
				}
			} finally {
				if (this.responseHandlerThread != null)
					this.responseHandlerThread.close();
				if (this.clientSocket != null)
					this.clientSocket.close();
				serverSocket.close();
			}
		} catch (Exception e) {
			// The if-clause down here is because of what is described in
			// http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
			// under "What if a thread doesn't respond to Thread.interrupt?"

			// So the socket is closed when the ClientNetworkService is
			// closed and in that special case no error should be
			// propagated.
			if (!stop) {
				Thread.UncaughtExceptionHandler eh = this.getUncaughtExceptionHandler();
				if (eh != null)
					eh.uncaughtException(this, e);
				else
					logger.error("Uncaught exception in thread: " + this.getName(), e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		stop = true;
		if (this.responseHandlerThread != null)
			this.responseHandlerThread.close();
		this.interrupt();
		if (this.clientSocket != null)
			this.clientSocket.close();
		serverSocket.close();
	}

	private static class ResponseHandlerThread extends Thread implements AutoCloseable {
		private static final Logger logger = LoggerFactory.getLogger(ResponseHandlerThread.class.getName());

		private final PriorityBlockingQueue<Bucket> responseBuffer;
		private final OutputStream outputStream;
		private volatile boolean stop;
		private long lastSentSequenceNumber;

		public ResponseHandlerThread(PriorityBlockingQueue<Bucket> responseBuffer, OutputStream outputStream) {
			super();

			this.responseBuffer = Objects.requireNonNull(responseBuffer);
			this.outputStream = Objects.requireNonNull(outputStream);
			this.lastSentSequenceNumber = 0;
		}

		@Override
		public void run() {
			try {
				try {
					logger.info("Start transferring data back to the originator");

					stop = false;
					while (!stop) {
						Bucket bucket = this.responseBuffer.take();

						long currentSequenceNumber = Short.toUnsignedLong(bucket.getNr());
						long nextSequenceNumber = this.lastSentSequenceNumber + 1;

						if (currentSequenceNumber <= lastSentSequenceNumber) {
							// Something terribly wrong happened, run away!
							logger.error("Missing packets! Something terribly wrong must have happened. Exiting.");
							this.stop = true;
							continue;
						} else if (currentSequenceNumber > nextSequenceNumber) {
							// There are missing buckets, so put the current bucket back, wait and try again
							this.responseBuffer.put(bucket);
							this.wait();
							continue;
						}

						// Send data back
						this.outputStream.write(bucket.getData());
						this.lastSentSequenceNumber = currentSequenceNumber;
					}
				} finally {
					this.outputStream.close();
				}
			} catch (Exception e) {
				// The if-clause down here is because of what is described in
				// http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
				// under "What if a thread doesn't respond to Thread.interrupt?"

				// So the socket is closed when the ClientNetworkService is
				// closed and in that special case no error should be
				// propagated.
				if (!this.stop) {
					Thread.UncaughtExceptionHandler eh = this.getUncaughtExceptionHandler();
					if (eh != null)
						eh.uncaughtException(this, e);
					else
						logger.error("Uncaught exception in thread: " + this.getName(), e);
				}
			}
		}

		@Override
		public void close() throws IOException {
			stop = true;
			this.interrupt();
			this.outputStream.close();
		}
	}
}
