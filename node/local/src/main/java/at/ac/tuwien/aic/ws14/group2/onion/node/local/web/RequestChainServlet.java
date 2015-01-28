package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.client.SocksClient;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

/**
 * Created by Stefan on 27.01.15.
 */
public class RequestChainServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Configuration configuration = ConfigurationFactory.getConfiguration();

		InetSocketAddress targetAddress = getTargetAddress(req);

		int localNodeServerPort = configuration.getLocalNodeServerPort();
		String localNodeHost = "localhost";
		InetSocketAddress serverAddress = new InetSocketAddress(localNodeHost, localNodeServerPort);

		String response = doSocks5Request(targetAddress, serverAddress);

		PrintWriter writer = resp.getWriter();
		writer.println(response);
		writer.flush();
		writer.close();
	}

	private InetSocketAddress getTargetAddress(HttpServletRequest req) {
		Configuration configuration = ConfigurationFactory.getConfiguration();
		String host = req.getParameter("host");
		if (host == null || host.isEmpty()) {
			host = configuration.getTargetServiceHost();
		}

		int port;
		try {
			port = Integer.parseInt(req.getParameter("port"));
		} catch (NumberFormatException e) {
			port = configuration.getTargetServicePort();
		}
		if (port < 0 || port > 65535) {
			port = configuration.getTargetServicePort();
		}

		return new InetSocketAddress(host, port);
	}

	private String doSocks5Request(InetSocketAddress target, InetSocketAddress server) throws ServletException {
		try {
			SocksClient socksClient = new SocksClient(server);

			return socksClient.sendHttpGet(target);
		} catch (IOException e) {
			throw new ServletException("Unable to issue socks5 request to " + target, e);
		}
	}
}
