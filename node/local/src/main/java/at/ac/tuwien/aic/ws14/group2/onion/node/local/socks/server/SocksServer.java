package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by klaus on 11/25/14.
 */
public class SocksServer extends Thread implements Closeable {

	private final ServerSocket socket;
	private final Collection<SocksWorker> socksWorkerCollection;
	private volatile boolean stop;
	private ExecutorService pool;

	public SocksServer(int port) throws IOException {
		if (port > 0xFFFF || port <= 0)
			throw new IllegalArgumentException("port must not be greater than " + 0xFFFF + " or less or equal 0");

		this.socket = new ServerSocket(port);
		this.socksWorkerCollection = new ConcurrentSkipListSet<>();
	}

	@Override
	public void run() {
		if (!socket.isBound())
			throw new IllegalStateException(
					"TCP listener socket not initialized");

		stop = false;

		pool = Executors.newCachedThreadPool(new SocksWorkerThreadFactory(
				getUncaughtExceptionHandler()));

		try {
			try {
				while (!stop) {
					Socket clientSocket = socket.accept();
					SocksWorker socksWorker = new SocksWorker(clientSocket, this.new SocksWorkerCloseCallback());
					socksWorkerCollection.add(socksWorker);
					pool.execute(socksWorker);
				}
			} finally {
				if (pool != null)
					shutdownAndAwaitTermination(pool);
				socket.close();
			}
		} catch (IOException e) {
			// The if-clause down here is because of what is described in
			// http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
			// under "What if a thread doesn't respond to Thread.interrupt?"

			// So the socket is closed when the ClientNetworkService is
			// closed and in that special case no error should be
			// propagated.
			if (!stop) {
				UncaughtExceptionHandler eh = this
						.getUncaughtExceptionHandler();
				if (eh != null)
					eh.uncaughtException(this, e);
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
				for (SocksWorker socksWorker : socksWorkerCollection) {
					socksWorker.close();
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private class SocksWorkerCloseCallback implements Consumer<SocksWorker> {
		@Override
		public void accept(SocksWorker socksWorker) {
			SocksServer.this.socksWorkerCollection.remove(Objects.requireNonNull(socksWorker));
		}
	}
}
