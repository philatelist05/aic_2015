package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.CommandNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ErrorCode;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;

/**
 * Created by klaus on 11/30/14.
 */
public class SocksWorker implements Runnable, AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(SocksWorker.class.getName());
	private static final int CHAIN_LENGTH = 3;

	private final Socket commandSocket;
	private final Consumer<SocksWorker> closeCallback;
	private final SynchronousQueue<ChainMetaData> chainEstablishedAnswerQueue = new SynchronousQueue<>();
	private final LocalNodeCore localNodeCore;
	private final DirectoryService.Client directoryClient;
	private Short circuitId;
	private SocksDataForwarderServer socksDataForwarderServer;

	public SocksWorker(Socket commandSocket, LocalNodeCore localNodeCore, DirectoryService.Client directoryClient, Consumer<SocksWorker> closeCallback) {
		this.commandSocket = Objects.requireNonNull(commandSocket);
		this.closeCallback = Objects.requireNonNull(closeCallback);
		this.localNodeCore = Objects.requireNonNull(localNodeCore);
		this.directoryClient = Objects.requireNonNull(directoryClient);
	}

	@Override
	public void run() {
		if (!commandSocket.isConnected())
			throw new IllegalStateException("TCP socket not connected");

		try {
			try {
				DataInputStream inputStream = new DataInputStream(commandSocket.getInputStream());
				OutputStream outputStream = commandSocket.getOutputStream();

				/*
				 * SOCKS connection initialization
				 */

				// Read the method selection request
				MethodSelectionRequest methodSelectionRequest = MethodSelectionRequest.fromInputStream(inputStream);

				// Check request
				if (Arrays.stream(methodSelectionRequest.getMethods()).noneMatch(Method.NO_AUTHENTICATION_REQUIRED::equals)) {
					// No supported method found

					// Tell the originator
					MethodSelectionReply reply = new MethodSelectionReply(Method.NO_ACCEPTABLE_METHODS);
					outputStream.write(reply.toByteArray());

					// ...and quit
					return;
				}

				// Send method selection reply
				MethodSelectionReply methodSelectionReply = new MethodSelectionReply(Method.NO_AUTHENTICATION_REQUIRED);
				outputStream.write(methodSelectionReply.toByteArray());

				// Read the command request
				CommandRequest commandRequest;
				try {
					commandRequest = CommandRequest.fromInputStream(inputStream);

					if (!Command.CONNECT.equals(commandRequest.getCommand()))
						throw new CommandNotSupportedException(commandRequest.getCommand().getValue());
				} catch (AddressTypeNotSupportedException | CommandNotSupportedException e) {
					// Something is not supported

					// Tell the originator
					CommandReply reply = new CommandReply(e.getReplyType(), new SocksAddress(commandSocket.getLocalAddress(), commandSocket.getLocalPort()));
					outputStream.write(reply.toByteArray());

					// ...and quit
					return;
				}

				// Create the chain
				createChain();

				SocksAddress destination = commandRequest.getDestination();

				// Connect to the target
				localNodeCore.connectTo(circuitId, convertEndpointToSocksAddress(destination));

				// Create and start the forwarder of for the client data
				socksDataForwarderServer = new SocksDataForwarderServer(circuitId, localNodeCore);
				socksDataForwarderServer.start();

				// Send succeeded command reply
				CommandReply commandReply = new CommandReply(ReplyType.SUCCEEDED, new SocksAddress(socksDataForwarderServer.getInetAddress(), socksDataForwarderServer.getLocalPort()));
				outputStream.write(commandReply.toByteArray());

				// Wait on SocksDataForwarderServer
				socksDataForwarderServer.join();

			} finally {
				close();
			}
		} catch (Exception e) {
			Thread.UncaughtExceptionHandler eh = Thread.currentThread()
					.getUncaughtExceptionHandler();
			if (eh != null)
				eh.uncaughtException(Thread.currentThread(), e);
			else
				logger.error("Uncaught exception in thread: " + Thread.currentThread().getName(), e);
		}
	}

	private Endpoint convertEndpointToSocksAddress(SocksAddress socksAddress) {
		Objects.requireNonNull(socksAddress);

		switch (socksAddress.getAddressType()) {
			case DOMAINNAME:
				return new Endpoint(socksAddress.getHostName(), socksAddress.getPort());
			case IP_V4_ADDRESS:
			case IP_V6_ADDRESS:
				return new Endpoint(socksAddress.getAddress(), socksAddress.getPort());
			default:
				throw new IllegalStateException("address type is in an illegal state");

		}
	}

	private void createChain() throws TException, InterruptedException {
		// Get chain from the directory
		List<ChainNodeInformation> chainMetaDataList = directoryClient.getChain(CHAIN_LENGTH);
		ChainMetaData chainMetaData = ChainMetaData.fromChainNodeInformationList(chainMetaDataList);

		// Create the chain
		localNodeCore.createChain(chainMetaData, new SocksCallbackImpl());

		// Wait for completion of the chain creation
		chainMetaData = this.chainEstablishedAnswerQueue.take();
		circuitId = chainMetaData.getCircuitID();
	}

	@Override
	public void close() throws IOException {
		commandSocket.close();

		if (socksDataForwarderServer != null)
			socksDataForwarderServer.close();

		this.closeCallback.accept(this);
	}

	private class SocksCallbackImpl implements SocksCallBack {

		@Override
		public void chainEstablished(ChainMetaData chainMetaData) {
			SocksWorker.this.chainEstablishedAnswerQueue.offer(chainMetaData);
		}

		@Override
		public void chainDestroyed() {
			try {
				SocksWorker.this.close();
			} catch (IOException e) {
				logger.error("Uncaught exception while closing SocksWorker", e);
			}
		}

		@Override
		public void responseData(Endpoint endpoint, Short sequenceNumber, byte[] data) {
			if (SocksWorker.this.socksDataForwarderServer == null) {
				logger.error("Got response data when socksDataForwarderServer was still null. Ignoring data!");
				return;
			}

			// Send response data
			SocksWorker.this.socksDataForwarderServer.sendDataBack(endpoint, sequenceNumber, data);
		}

		@Override
		public void error(ErrorCode errorCode) {
			try {
				SocksWorker.this.close();
			} catch (IOException e) {
				logger.error("Uncaught exception while closing SocksWorker", e);
			}
		}
	}
}
