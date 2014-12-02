package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksDataForwarder implements Runnable, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksDataForwarder.class.getName());

	private final Short circuitId;
	private final LocalNodeCore localNodeCore;
	private final Consumer<SocksDataForwarder> closeCallback;
	private final Socket socket;
	private volatile boolean stop;

	public SocksDataForwarder(Socket socket, Short circuitId, LocalNodeCore localNodeCore, Consumer<SocksDataForwarder> closeCallback) {

		this.closeCallback = Objects.requireNonNull(closeCallback);
		this.circuitId = Objects.requireNonNull(circuitId);
		this.localNodeCore = Objects.requireNonNull(localNodeCore);
		this.socket = Objects.requireNonNull(socket);
	}

	@Override
	public void run() {
		if (!socket.isConnected())
			throw new IllegalStateException("TCP socket not connected");

		byte[] buffer = new byte[DataCommand.MAX_DATA_LENGTH];

		try {
			try {

				InputStream inputStream = socket.getInputStream();

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
				close();
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
		socket.close();

		this.closeCallback.accept(this);
	}
}
