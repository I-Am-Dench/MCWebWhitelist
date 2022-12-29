package theof.webwhitelist.webserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {

    public static final int SHUTDOWN_DELAY = 5;

    private final HttpServer server;
    private final Map<String, RouteHandlers> ROUTE_HANDLERS = new HashMap<>();

    private final boolean useCORS;

    public WebServer(String host, int port, boolean useCORS) throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);
        this.useCORS = useCORS;
        this.server = HttpServer.create(address, 0);
        this.server.setExecutor(null);
    }

    public void enable() {
        this.server.start();
    }

    public void disable() {
        this.server.stop(SHUTDOWN_DELAY);
    }

    private String normalizePath(String path) {
        if (path == null || path.length() == 0) {
            return "/";
        }

        StringBuilder builder = new StringBuilder();
        if (path.charAt(0) != '/')
            builder.append('/');

        builder.append(path);

        if (path.charAt(path.length()-1) != '/')
            builder.append('/');

        return URI.create(builder.toString()).normalize().getPath();
    }

    private RouteHandlers getHandlers(String path) {
        return this.ROUTE_HANDLERS.getOrDefault(path, new RouteHandlers());
    }

    private boolean handleCORS(Context c) throws IOException {
        c.set(Header.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        c.set(Header.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

        c.addControlAllowMethods(Method.GET, Method.OPTIONS);
        return c.next();
    }

    private void routeRequest(HttpExchange t) throws IOException {
        String path = this.normalizePath(t.getRequestURI().getPath());
        Method method = Method.valueOf(t.getRequestMethod());

        Context context = new Context(t);
        if (!this.ROUTE_HANDLERS.containsKey(path)) {
            context.status(Status.NOT_FOUND).sendString(String.format("Cannot %s %s", method, path));
            return;
        }

        Map<Method, List<Handler>> handlers = this.getHandlers(path);
        if (!handlers.containsKey(method)) {
            context.sendStatus(Status.METHOD_NOT_ALLOWED);
            return;
        }

        boolean success = false;
        try {
            success = context.pipeline(handlers.get(method));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = String.format("Error occurred in [%s] %s: %s", method, path, e.getMessage());
            System.out.println(msg);
        }

        if (!success) {
            context.sendStatus(Status.INTERNAL_SERVER_ERROR);
            return;
        }

        Headers headers = context.getResponseHeaders();
        if (context.bodyIsEmpty() && !headers.containsKey(Header.STATUS)) {
            context.sendString("empty");
        }
    }

    public void Use(Method method, String path, Handler ...handler) {
        String normalized = this.normalizePath(path);
        this.server.createContext(normalized, this::routeRequest);

        RouteHandlers handlers = this.getHandlers(normalized);

        if (this.useCORS) {
            handlers.addHandlers(Method.OPTIONS, this::handleCORS);
            handlers.addHandlers(method, this::handleCORS);
        }

        handlers.addHandlers(method, handler);


        this.ROUTE_HANDLERS.put(normalized, handlers);
    }

    public void All(String path, Handler handler) {
        for (Method method : Method.values()) {
            Use(method, path, handler);
        }
    }

    public void Get(String path, Handler ...handlers) { Use(Method.GET, path, handlers); }
    public void Post(String path, Handler ...handlers) { Use(Method.POST, path, handlers); }
    public void Delete(String path, Handler ...handlers) { Use(Method.DELETE, path, handlers); }
    public void Put(String path, Handler ...handlers) { Use(Method.PUT, path, handlers); }
    public void Patch(String path, Handler ...handlers) { Use(Method.PATCH, path, handlers); }
}
