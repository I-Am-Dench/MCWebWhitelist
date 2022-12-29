package theof.webwhitelist.webserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RouteHandlers extends HashMap<Method, List<Handler>> {

    public List<Handler> pipeline(Method method) {
        return this.getOrDefault(method, new ArrayList<>());
    }

    public void addHandlers(Method method, Handler ...handlers) {
        List<Handler> pipeline = this.pipeline(method);
        pipeline.addAll(Arrays.asList(handlers));
        this.put(method, pipeline);
    }
}
