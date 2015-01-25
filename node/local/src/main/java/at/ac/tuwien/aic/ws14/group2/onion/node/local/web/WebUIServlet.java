package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.LocalNodeStarter;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.client.SocksClient;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.SocksException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by Stefan on 23.01.15.
 */
public class WebUIServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(WebUIServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebInformationCallbackImpl callback = new WebInformationCallbackImpl();
        HttpSession session = req.getSession(true);
        session.setAttribute("callback", callback);

        LocalNodeStarter.setWebInformationCallback(callback);

        Configuration configuration = ConfigurationFactory.getConfiguration();
        String targetServiceHost = configuration.getTargetServiceHost();
        int targetServicePort = configuration.getTargetServicePort();
        InetSocketAddress targetAddress = new InetSocketAddress(targetServiceHost, targetServicePort);
        int localNodeServerPort = configuration.getLocalNodeServerPort();
        String localNodeHost = "localhost";
        InetSocketAddress serverAddress = new InetSocketAddress(localNodeHost, localNodeServerPort);

        String response = doSocks5Request(targetAddress, serverAddress);

        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("response", response);

        Template template = new Template("webapp/templates/index.hbs");
        template.render(resp.getWriter(), scopes);
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
