package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefan on 27.01.15.
 */
public class RequestOverviewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new ServletException("Session is not initialized");
        }
        WebInformationCallbackImpl callback = (WebInformationCallbackImpl) session.getAttribute("callback");
        List<Long> ids = callback.getIds();
        Template template = new Template("webapp/templates/chainOverview.hbs");
        template.render(resp.getWriter(), new Context(ids));

    }

    static class Context {
        List<Item> items;

        Context(List<Long> ids) {
            items = new ArrayList<>();
            ids.forEach(id -> items.add(new Item(id)));
        }
        List<Item> requests() {
            return items;
        }

        static class Item {
            Item(Long id) {
                this.id = id;
            }
            Long id;
        }
    }
}