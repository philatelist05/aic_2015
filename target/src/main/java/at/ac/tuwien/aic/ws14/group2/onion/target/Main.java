package at.ac.tuwien.aic.ws14.group2.onion.target;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Created by stefan on 22.11.14.
 */
public class Main {
	public static void main(String[] args) throws Exception {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		Server jettyServer = new Server(8080);
		jettyServer.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(
                com.sun.jersey.spi.container.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
           "javax.ws.rs.Application",
           RestApplication.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
	}
}
