package theof.webwhitelist.webserver;

import java.io.IOException;

@FunctionalInterface
public interface Handler {
    boolean handle(Context c) throws IOException;
}
