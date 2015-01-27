package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.client;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.GeneralServerFailureException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.SocksException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.*;

import java.io.*;
import java.net.*;
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
	 * <p>
	 * Send data to the destination over the SOCKS server and receive the response as return value.
	 * </p><p>
	 * Attention: This method <strong>blocks</strong> until the entire response is received, i.e. until an
	 * EOF is received.
	 * </p>
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

			// We are ready to send
			outputStream.write(data);
			outputStream.flush();

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096); // 4 KiB buffer
			int length;

			byte[] buffer = new byte[4096];
			while ((length = inputStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, length);
			}

			return byteArrayOutputStream.toByteArray();
		} finally {
			if (socket != null)
				socket.close();

		}
	}

	public String sendHttpGet(InetSocketAddress destination) throws IOException {
		Objects.requireNonNull(destination);

		URL destinationUrl = new URL(String.format("http://%s:%d", destination.getHostString(), destination.getPort()));

		Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, socksServer);

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) destinationUrl.openConnection(socksProxy);

			// Get response
			InputStream inputStream = connection.getInputStream();
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

				StringBuilder response = new StringBuilder();

				bufferedReader.lines().forEach((s) -> response.append(String.format("%s%n", s)));

				// Delete last newline character
				int newlineLength = String.format("%n").length();
				response.delete(response.length() - newlineLength, response.length());

				return response.toString();
			}

		} finally {
			if (connection != null)
				connection.disconnect();

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
