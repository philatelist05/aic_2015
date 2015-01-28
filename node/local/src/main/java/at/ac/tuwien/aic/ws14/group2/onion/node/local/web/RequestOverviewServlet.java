package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

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
        Map<Long, Date> ids = callback.getIds();
        Template template = new Template("webapp/templates/chainOverview.hbs");
        template.render(resp.getWriter(), new Context(ids));

    }

    static class Context {
        List<Item> items;

        Context(Map<Long, Date> ids) {
            items = new ArrayList<>();
            SortedMap<Date, Long> sorted = new TreeMap<>(Date::compareTo);
            ids.forEach((aLong1, date1) -> sorted.put(date1, aLong1));
            sorted.forEach((date1, aLong1) -> items.add(new Item(aLong1, date1.toString())));
        }
        List<Item> requests() {
            return items;
        }

        static class Item {
            Item(Long id, String time) {
                this.id = id;
                this.time = time;
            }
            Long id;
            String time;
        }
    }
}
