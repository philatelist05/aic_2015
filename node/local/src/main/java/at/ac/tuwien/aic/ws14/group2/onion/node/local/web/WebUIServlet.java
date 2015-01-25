package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.LocalNodeStarter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Stefan on 23.01.15.
 */
public class WebUIServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebInformationCallbackImpl callback = new WebInformationCallbackImpl();
        LocalNodeStarter.setWebInformationCallback(callback);
        Template template = new Template("webapp/templates/index.hbs");
        template.render(resp.getWriter(), new Object());
    }


}
