package theof.webwhitelist.webserver;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;

public class Context {

    private final HttpExchange exchange;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final LinkedList<Handler> pipeline = new LinkedList<>();

    private int status = Status.OK;
    private int bytesWritten = -1;

    private JsonElement jsonPayload = null; // Cache for the parsed json body, if valid. Returned without re-parsing if not null

    private final Set<String> CORS_allowedMethods = new HashSet<>();

    public Context(HttpExchange t) {
        this.exchange = t;
    }

    private void writeToBody(byte[] bytes) throws IOException {
        this.buffer.write(bytes);
        this.bytesWritten = this.bytesWritten == -1 ? bytes.length : this.bytesWritten + bytes.length;
    }

    public void send() throws IOException {
        if (this.bodyIsEmpty() && this.status != Status.NO_CONTENT) {
            writeToBody(Status.toString(this.status).getBytes());
        }

        // Apply CORS
        if (!this.CORS_allowedMethods.isEmpty()) {
            String allowedMethods = String.join(", ", this.CORS_allowedMethods);
            this.getResponseHeaders().set(Header.ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
        }

        this.exchange.sendResponseHeaders(this.status, this.bytesWritten); // Open response first...

        // ...then write the output stream to the response
        if (!this.bodyIsEmpty()) {
            this.buffer.writeTo(this.exchange.getResponseBody());
        }

        this.exchange.getResponseBody().close();
        this.buffer.close();
    }

    public Context status(int status) {
        this.status = status;
        return this;
    }

    public boolean sendStatus(int status) throws IOException {
        this.status(status).send();
        return true;
    }

    public boolean sendString(String str) throws IOException {
        byte[] bytes = str.getBytes();
        this.writeToBody(bytes);
        this.send();
        return true;
    }

    public boolean json(JsonElement element) throws IOException {
        this.getResponseHeaders().add(Header.CONTENT_TYPE, "application/json");
        this.sendString(element.toString());
        return true;
    }

    public Headers getResponseHeaders() {
        return this.exchange.getResponseHeaders();
    }

    public String get(String key) {
        return this.exchange.getRequestHeaders().getFirst(key);
    }

    public void set(String key, String value) {
        this.getResponseHeaders().set(key, value);
    }

    public int contentLength() throws NumberFormatException {
        Headers headers = this.exchange.getRequestHeaders();
        return Integer.parseInt(headers.getFirst(Header.CONTENT_LENGTH));
    }

    public void addControlAllowMethods(Method ...methods) {
        for (Method method : methods) {
            this.CORS_allowedMethods.add(method.toString());
        }
    }

    public URI getRequestURI() {
        return this.exchange.getRequestURI();
    }

    public Map<String, String> getQuery() {
        Map<String, String> query = new HashMap<>();
        String queryString = this.getRequestURI().getQuery();

        if (queryString == null) {
            return query;
        }

        for (String pair : queryString.split("&")) {
            String[] keyValues = pair.split("=");
            if (keyValues.length > 1) {
                query.put(keyValues[0], keyValues[1]);
            } else if (keyValues.length == 1) {
                query.put(keyValues[0], "true");
            }
        }

        return query;
    }

    public JsonElement parseJson() {
        if (this.jsonPayload == null) {
            InputStream is = this.exchange.getRequestBody();
            this.jsonPayload = JsonParser.parseReader(new InputStreamReader(is));
        }
        return this.jsonPayload;
    }

    public boolean next() throws IOException {
        try {
            Handler handler = this.pipeline.removeFirst();
            if (handler != null) {
                return handler.handle(this);
            }
        } catch (NoSuchElementException ignored) {}
        return this.sendStatus(Status.OK);
    }

    public boolean pipeline(List<Handler> pipeline) throws IOException {
        this.pipeline.addAll(pipeline);
        return this.next();
    }

    public boolean bodyIsEmpty() {
        return this.bytesWritten == -1;
    }
}
