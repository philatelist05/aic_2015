package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksWorkerThreadFactory implements ThreadFactory {
	private final Thread.UncaughtExceptionHandler eh;
	private int tCount;

	public SocksWorkerThreadFactory(Thread.UncaughtExceptionHandler eh) {
		Objects.requireNonNull(eh, "eh must not be null");

		this.eh = eh;
		tCount = 0;
	}

	@Override
	public Thread newThread(Runnable run) {
		Thread thread = new Thread(run);
		thread.setName("SocksWorker thread Nr. " + ++tCount);
		thread.setUncaughtExceptionHandler(eh);
		return thread;
	}
}
