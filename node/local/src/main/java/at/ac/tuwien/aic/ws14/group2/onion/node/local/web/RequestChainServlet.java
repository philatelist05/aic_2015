package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.client.SocksClient;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.SocksException;
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
        String targetServiceHost = configuration.getTargetServiceHost();
        int targetServicePort = configuration.getTargetServicePort();
        InetSocketAddress targetAddress = new InetSocketAddress(targetServiceHost, targetServicePort);
        int localNodeServerPort = configuration.getLocalNodeServerPort();
        String localNodeHost = "localhost";
        InetSocketAddress serverAddress = new InetSocketAddress(localNodeHost, localNodeServerPort);

        String response = doSocks5Request(targetAddress, serverAddress);

        PrintWriter writer = resp.getWriter();
        writer.println(response);
        writer.flush();
        writer.close();
    }

    private String doSocks5Request(InetSocketAddress target, InetSocketAddress server) throws ServletException {
        try {
            SocksClient socksClient = new SocksClient(server);
            return socksClient.send(target, "GET / HTTP/1.1\r\n\r\n");
        } catch (IOException | SocksException e) {
            throw new ServletException("Unable to issue socks5 request to " + target, e);
        }
    }
}
