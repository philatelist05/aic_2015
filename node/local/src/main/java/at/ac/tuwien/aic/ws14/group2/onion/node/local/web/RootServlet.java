package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.LocalNodeStarter;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stefan on 23.01.15.
 */
public class RootServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(RootServlet.class.getName());
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            session = req.getSession(true);
            WebInformationCallbackImpl callback = new WebInformationCallbackImpl();
            session.setAttribute("callback", callback);
            LocalNodeStarter.setWebInformationCallback(callback);
            logger.info("Registered callback");
        }

        Configuration configuration = ConfigurationFactory.getConfiguration();
        String targetServiceHost = configuration.getTargetServiceHost();
        int targetServicePort = configuration.getTargetServicePort();

        Map<String, Object> props = new HashMap<>();
        props.put("default-hostname", targetServiceHost);
        props.put("default-port", targetServicePort);

        Template template = new Template("webapp/templates/root.hbs");
        template.render(resp.getWriter(), props);
    }
}
