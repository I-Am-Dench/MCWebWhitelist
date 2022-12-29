package theof.webwhitelist;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import theof.webwhitelist.webserver.Context;
import theof.webwhitelist.webserver.Method;
import theof.webwhitelist.webserver.Status;
import theof.webwhitelist.webserver.WebServer;

import java.io.IOException;

public class WebWhitelist extends JavaPlugin {

    private static WebWhitelist plugin;

    private static final String CONFIG_HOST = "host";
    private static final String CONFIG_PORT = "port";
    private static final String CONFIG_SECRET = "secret-key";
    private static final String CONFIG_WS_CORS = "webserver.useCORS";

    private static final String HEADER_SECRET_KEY = "X-Secret-Key";

    private final WhitelistRequests requests = new WhitelistRequests();
    private WebServer webserver = null;

    private String host;
    private int port;
    private String secretKey;
    private boolean useCORS;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.configure();

        if (secretKey == null) {
            Utils.log("&cPlease add a secret key to the config.yml!");
            Utils.log("&cWeb server was not initialized.");
            return;
        }

        try {
            this.webserver = new WebServer(this.host, this.port, useCORS);

            this.initializeRoutes(this.webserver);

            Utils.log(String.format("&aWeb server started listening on %s:%d", this.host, this.port));
            this.webserver.enable();

            new ListenerLogin();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log("&cWeb server failed initialization.");
        }
    }

    @Override
    public void onDisable() {
        if (this.serverIsRunning()) {
            this.webserver.disable();
            Utils.log("&eWeb server stopped.");
        }
    }

    public boolean verifyKey(Context c) throws IOException {
        String key = c.get(HEADER_SECRET_KEY);
        if (this.secretKey.equals(key)) {
            return c.next();
        } else {
            return c.sendStatus(Status.UNAUTHORIZED);
        }
    }

    public void initializeRoutes(WebServer server) {
        server.Get("/", c -> c.sendStatus(Status.OK));

        server.Post("/requests", this::verifyKey, Handlers::createRequest);
        server.Post("/whitelist", this::verifyKey, Handlers::whitelistPlayer);
        server.Delete("/whitelist", this::verifyKey, Handlers::removeWhitelist);

        if (this.useCORS) {
            server.Use(Method.OPTIONS, "/whitelist", c -> {
                c.addControlAllowMethods(Method.DELETE);
                return c.sendStatus(Status.OK);
            });
        }
    }

    public static WebWhitelist plugin() {
        return plugin;
    }

    public void doOnPlayerWhitelist(Handlers.PlayerWhitelistHandler handler) {
        Handlers.doOnPlayerWhitelist(handler);
    }

    public void doOnRemoveWhitelist(Handlers.RemoveWhitelistHandler handler) {
        Handlers.doOnRemoveWhitelist(handler);
    }

    public WhitelistRequests requests() {
        return this.requests;
    }

    public void configure() {
        FileConfiguration config = this.getConfig();

        this.host = config.getString(CONFIG_HOST, "127.0.0.1");
        this.port = config.getInt(CONFIG_PORT, 3000);
        this.secretKey = config.getString(CONFIG_SECRET);
        this.useCORS = config.getBoolean(CONFIG_WS_CORS, false);
    }

    public boolean serverIsRunning() {
        return this.webserver != null;
    }
}
