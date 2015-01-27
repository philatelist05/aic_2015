package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Bucket;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksDataForwarder extends Thread implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksDataForwarder.class.getName());

	private final Short circuitId;
	private final LocalNodeCore localNodeCore;
	private final PriorityBlockingQueue<Bucket> responseBuffer;
	private final Socket clientSocket;
	private final BlockingQueue<Object> newBucketNotifyingQueue;
	private volatile boolean stop;
	private ResponseHandlerThread responseHandlerThread;

	public SocksDataForwarder(Socket clientSocket, Short circuitId, LocalNodeCore localNodeCore) throws IOException {
		this.circuitId = circuitId;
		this.localNodeCore = localNodeCore;
		this.clientSocket = Objects.requireNonNull(clientSocket);
		this.responseBuffer = new PriorityBlockingQueue<>();
		this.newBucketNotifyingQueue = new LinkedBlockingQueue<>();
	}

	/**
	 * Send data back to the originator.
	 *
	 * @param sequenceNumber the sequence number associated with the DataCommand that contained the data
	 * @param data           the raw data contained in the data cell
	 */
	public void sendDataBack(long sequenceNumber, byte[] data) {
		// Put the data into the priority queue
		this.responseBuffer.put(new Bucket(data, sequenceNumber));

		// Notify the thread about a new object in the queue if it waits for a missing number
		// TODO (KK) This is maybe not the optimal method of synchronization!
		newBucketNotifyingQueue.offer(new Object());
	}

	@Override
	public void run() {
		if (!clientSocket.isConnected())
			throw new IllegalStateException(
					"TCP listener socket not initialized");

		try {
			try {

				// Start thread handling the responses
				logger.info("Start thread handling the responses");
				this.responseHandlerThread = new ResponseHandlerThread(this.responseBuffer,
						clientSocket.getOutputStream(), newBucketNotifyingQueue, localNodeCore, circuitId);
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

					if (this.localNodeCore.hasWebCallback())
						this.localNodeCore.getWebCallback().dataSent(this.circuitId, payload);
				}
			} finally {
				if (this.responseHandlerThread != null)
					this.responseHandlerThread.close();
				this.clientSocket.close();
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
		this.clientSocket.close();
	}

	private static class ResponseHandlerThread extends Thread implements AutoCloseable {
		private static final Logger logger = LoggerFactory.getLogger(ResponseHandlerThread.class.getName());

		private final PriorityBlockingQueue<Bucket> responseBuffer;
		private final OutputStream outputStream;
		private final BlockingQueue<Object> newBucketNotifyingQueue;
		private final LocalNodeCore localNodeCore;
		private final Short circuitId;
		private volatile boolean stop;
		private long lastSentSequenceNumber;

		public ResponseHandlerThread(PriorityBlockingQueue<Bucket> responseBuffer, OutputStream outputStream,
		                             BlockingQueue<Object> newBucketNotifyingQueue, LocalNodeCore localNodeCore,
		                             Short circuitId) {
			super();
			this.localNodeCore = Objects.requireNonNull(localNodeCore);
			this.circuitId = Objects.requireNonNull(circuitId);
			this.responseBuffer = Objects.requireNonNull(responseBuffer);
			this.outputStream = Objects.requireNonNull(outputStream);
			this.lastSentSequenceNumber = -1;
			this.newBucketNotifyingQueue = Objects.requireNonNull(newBucketNotifyingQueue);
		}

		@Override
		public void run() {
			try {
				try {
					logger.info("Start transferring data back to the originator");

					stop = false;
					while (!stop) {
						Bucket bucket = this.responseBuffer.take();

						long currentSequenceNumber = bucket.getNr();

						if (currentSequenceNumber <= lastSentSequenceNumber) {
							// Something terribly wrong happened, run away!
							logger.error("Missing packets! Something terribly wrong must have happened. Exiting.");
							this.stop = true;
							continue;
						} else if (currentSequenceNumber > lastSentSequenceNumber + 1) {
							// There are missing buckets, so put the current bucket back, wait and try again
							this.responseBuffer.put(bucket);
							// TODO (KK) This is maybe not the optimal method of synchronization!
							this.newBucketNotifyingQueue.take();
							continue;
						}
						// assert currentSequenceNumber == lastSentSequenceNumber + 1
						this.newBucketNotifyingQueue.poll();

						// Send data back
						this.outputStream.write(bucket.getData());
						this.lastSentSequenceNumber = currentSequenceNumber;

						if (this.localNodeCore.hasWebCallback())
							this.localNodeCore.getWebCallback().dataReceived(this.circuitId, bucket.getData());
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
