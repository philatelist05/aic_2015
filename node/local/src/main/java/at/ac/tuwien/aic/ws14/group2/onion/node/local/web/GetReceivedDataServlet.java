package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Stefan on 27.01.15.
 */
public class GetReceivedDataServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new ServletException("Session is not initialized");
        }

        WebInformationCallbackImpl callback = (WebInformationCallbackImpl) session.getAttribute("callback");
        long id = (long) session.getAttribute("id");
        List<byte[]> dataReceived = callback.getDataReceived(id);

        PrintWriter writer = resp.getWriter();
        dataReceived.forEach(bytes -> writer.print(Arrays.toString(bytes)));
        writer.flush();
        writer.close();
    }
}
