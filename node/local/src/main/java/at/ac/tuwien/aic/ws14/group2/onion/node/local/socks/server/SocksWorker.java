package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksWorker implements Runnable, Closeable {

	private final Socket commandSocket;
	private final Consumer<SocksWorker> closeCallback;

	public SocksWorker(Socket commandSocket, Consumer<SocksWorker> closeCallback) {
		this.commandSocket = Objects.requireNonNull(commandSocket);
		this.closeCallback = Objects.requireNonNull(closeCallback);
	}

	@Override
	public void run() {
		if (!commandSocket.isConnected())
			throw new IllegalStateException("TCP socket not connected");

		// TODO (KK) Implement SOCKS connection initialization

		// TODO (KK) Implement data forwarding
	}

	@Override
	public void close() throws IOException {
		// TODO (KK)
		this.commandSocket.close();

		this.closeCallback.accept(this);
	}
}
