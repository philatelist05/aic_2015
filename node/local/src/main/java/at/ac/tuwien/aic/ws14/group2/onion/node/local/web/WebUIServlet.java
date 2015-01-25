package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;

/**
 * Created by Stefan on 23.01.15.
 */
public class WebUIServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HashMap<String, Object> scopes = new HashMap<>();
        scopes.put("name", "Mustache");
        scopes.put("feature", "Perfect!");

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader("{{name}}, {{feature}}!"), "example");
        Writer wr = resp.getWriter();
        mustache.execute(wr, scopes);
        wr.flush();
        wr.close();
    }
}
