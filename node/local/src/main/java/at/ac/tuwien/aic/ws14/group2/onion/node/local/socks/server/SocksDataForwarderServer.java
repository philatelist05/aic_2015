package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksDataForwarderServer extends Thread implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksDataForwarderServer.class.getName());

	private final Short circuitId;
	private final LocalNodeCore localNodeCore;
	private final ServerSocket socket;
	private final Collection<SocksDataForwarder> socksDataForwarderCollection;
	private final SortedMap<Short, byte[]> responseBuffer;
	private ExecutorService pool;
	private volatile boolean stop;

	public SocksDataForwarderServer(Short circuitId, LocalNodeCore localNodeCore) throws IOException {
		this.circuitId = circuitId;
		this.localNodeCore = localNodeCore;
		// Create socket for the actual data from the originator
		this.socket = new ServerSocket(0);
		this.socksDataForwarderCollection = new CopyOnWriteArrayList<>();
		this.responseBuffer = new ConcurrentSkipListMap<>();
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	public InetAddress getInetAddress() {
		return this.socket.getInetAddress();
	}

	/**
	 * Send data back to the originator.
	 *
	 * @param endpoint       the endpoint from which the request came
	 * @param sequenceNumber the sequence number associated with the DataCommand that contained the data
	 * @param data           the raw data contained in the data cell
	 */
	public void sendDataBack(Endpoint endpoint, Short sequenceNumber, byte[] data) {
		// TODO (KK) Send data back
	}

	@Override
	public void run() {
		if (!socket.isBound())
			throw new IllegalStateException(
					"TCP listener socket not initialized");

		pool = Executors.newCachedThreadPool(new SocksDataForwarderThreadFactory(Thread.currentThread().getUncaughtExceptionHandler()));

		try {
			try {
				stop = false;
				while (!stop) {
					Socket clientSocket = socket.accept();
					SocksDataForwarder socksDataForwarder = new SocksDataForwarder(clientSocket, circuitId, localNodeCore, this.new SocksDataForwarderCloseCallback());
					socksDataForwarderCollection.add(socksDataForwarder);
					pool.execute(socksDataForwarder);
				}
			} finally {
				if (pool != null)
					shutdownAndAwaitTermination(pool);
				socket.close();
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
		if (pool != null)
			shutdownAndAwaitTermination(pool);
		this.interrupt();
		socket.close();
	}

	private void shutdownAndAwaitTermination(ExecutorService pool)
			throws IOException {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				pool.shutdownNow();

				// Close the clients connections so they can really terminate
				for (SocksDataForwarder socksDataForwarder : socksDataForwarderCollection) {
					socksDataForwarder.close();
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private class SocksDataForwarderCloseCallback implements Consumer<SocksDataForwarder> {
		@Override
		public void accept(SocksDataForwarder socksDataForwarder) {
			SocksDataForwarderServer.this.socksDataForwarderCollection.remove(Objects.requireNonNull(socksDataForwarder));
		}
	}
}
