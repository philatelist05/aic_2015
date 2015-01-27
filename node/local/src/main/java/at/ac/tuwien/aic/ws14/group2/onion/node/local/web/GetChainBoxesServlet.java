package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Stefan on 27.01.15.
 */
public class GetChainBoxesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new ServletException("Session is not initialized");
        }
        long id = (long) session.getAttribute("id");
        WebInformationCallbackImpl callback = (WebInformationCallbackImpl) session.getAttribute("callback");

    }
}
