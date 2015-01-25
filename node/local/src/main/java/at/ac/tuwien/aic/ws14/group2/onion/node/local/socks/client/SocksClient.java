package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.client;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.GeneralServerFailureException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.SocksException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Created by klaus on 1/25/15.
 */
public class SocksClient {
	private final InetSocketAddress socksServer;

	public SocksClient(InetSocketAddress socksServer) {
		this.socksServer = socksServer;
	}

	/**
	 * Send data to the destination over the SOCKS server and receive the response as return value.
	 * This method <strong>blocks</strong> until the entire response is received.
	 *
	 * @param destination the IP address and port of the destination server
	 * @param data        the data which should be sent over the SOCKS server to the destination
	 * @return the response from the destination
	 */
	public byte[] send(InetSocketAddress destination, byte[] data) throws IOException, SocksException {
		Objects.requireNonNull(destination);
		Objects.requireNonNull(data);

		Socket socket = null;
		try {
			socket = new Socket(socksServer.getAddress(), socksServer.getPort());

			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			OutputStream outputStream = socket.getOutputStream();


			// Send the method selection request
			MethodSelectionRequest methodSelectionRequest = new MethodSelectionRequest(Method.NO_AUTHENTICATION_REQUIRED);
			outputStream.write(methodSelectionRequest.toByteArray());

			// Read method selection reply
			MethodSelectionReply methodSelectionReply = MethodSelectionReply.fromInputStream(inputStream);

			// Check reply
			if (!Method.NO_AUTHENTICATION_REQUIRED.equals(methodSelectionReply.getMethod())) {
				// No supported method selected from the server
				throw new MessageParsingException("Server doesn't accept the no-authentication method");
			}

			// Send the command request
			CommandRequest commandRequest = new CommandRequest(Command.CONNECT, new SocksAddress(destination));
			outputStream.write(commandRequest.toByteArray());

			// Read the command reply
			CommandReply commandReply;
			try {
				commandReply = CommandReply.fromInputStream(inputStream);
			} catch (AddressTypeNotSupportedException e) {
				// We trust that the server doesn't send a corrupted SOCKS message
				throw new RuntimeException(e);
			}

			if (!ReplyType.SUCCEEDED.equals(commandReply.getReplyType())) {

				// Reply did not succeed
				try {
					throw commandReply.getReplyType().getExceptionClass().newInstance();
				} catch (Exception e) {
					throw new GeneralServerFailureException(String.format("Server exception with type 0x%02X = %s",
							commandReply.getReplyType().getValue(), commandReply.getReplyType().name()));
				}
			}

			// We are read to send
			outputStream.write(data);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096); // 4 KiB buffer
			int c;

			// TODO (KK) Maybe we can use the Channels of Java 8 here instead
			while ((c = inputStream.read()) != -1) {
				byteArrayOutputStream.write(c);
			}

			return byteArrayOutputStream.toByteArray();
		} finally {
			if (socket != null)
				socket.close();

		}
	}

	/**
	 * Send UTF-8 encoded data
	 *
	 * @see #send(java.net.InetSocketAddress, byte[])
	 */
	public String send(InetSocketAddress destination, String data) throws IOException, SocksException {
		return new String(send(destination, data.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"));
	}
}
