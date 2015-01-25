package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.*;

/**
 * Created by Stefan on 25.01.15.
 */
public class Template {

    private String location;

    public Template(String location) {
        this.location = location;
    }

    public void render(Writer writer, Object scope) throws IOException {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MustacheFactory mf = new DefaultMustacheFactory();
        InputStream in = cl.getResourceAsStream(location);
        Mustache mustache = mf.compile(new InputStreamReader(in), "test");
        mustache.execute(writer, scope);
        writer.flush();
    }

    public void render(Writer writer, Object[] scopes) throws IOException {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(cl.getResourceAsStream(location)), "test");
        mustache.execute(writer, scopes);
        writer.flush();
    }
}
