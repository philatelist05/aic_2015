package at.ac.tuwien.aic.ws14.group2.onion.target;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RestApplication extends Application {
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classSet = new HashSet<>();
        classSet.add(RestService.class);
        return classSet;
    }
}
