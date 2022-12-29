package theof.webwhitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerProfile;
import theof.webwhitelist.webserver.Context;
import theof.webwhitelist.webserver.Status;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public class Handlers {

    @FunctionalInterface
    public interface PlayerWhitelistHandler {
        boolean handle(PlayerProfile player, JsonElement request);
    }

    @FunctionalInterface
    public interface RemoveWhitelistHandler {
        boolean handle(PlayerProfile player);
    }

    private static PlayerWhitelistHandler onPlayerWhitelist = (p, r) -> true;
    private static RemoveWhitelistHandler onRemoveWhitelist = p -> true;

    public static void doOnPlayerWhitelist(PlayerWhitelistHandler handler) {
        onPlayerWhitelist = handler;
    }

    public static void doOnRemoveWhitelist(RemoveWhitelistHandler handler) {
        onRemoveWhitelist = handler;
    }

    private static JsonElement marshalExpiration(long expiration) {
        JsonObject obj = new JsonObject();
        obj.addProperty("expiration", expiration);
        return obj;
    }

    private static JsonElement marshalPlayer(PlayerProfile player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", player.getName());
        obj.addProperty("id", player.getUniqueId().toString());
        return obj;
    }

    public static boolean createRequest(Context c) throws IOException {
        UUID uuid = Utils.getUUIDFromJson(c);
        if (uuid == null) {
            return c.sendStatus(Status.BAD_REQUEST);
        }

        if (WebWhitelist.plugin().requests().hasRequested(uuid)) {
            long expiration = WebWhitelist.plugin().requests().getExpiration(uuid);
            return c.status(Status.CONFLICT).json(marshalExpiration(expiration));
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isWhitelisted()) {
            return c.sendStatus(Status.FORBIDDEN);
        }

        long expiration = WebWhitelist.plugin().requests().createRequest(uuid);
        return c.status(Status.CREATED).json(marshalExpiration(expiration));
    }

    public static boolean whitelistPlayer(Context c) throws IOException {
        String code = Utils.getCodeFromJson(c);
        if (code == null) {
            return c.sendStatus(Status.BAD_REQUEST);
        }

        WhitelistRequests.LinkRequest request = WebWhitelist.plugin().requests().popRequest(code);
        if (request == null) {
            return c.sendStatus(Status.NOT_FOUND);
        }

        try {
            PlayerProfile profile = request.getPlayer().getPlayerProfile().update().join();
            if (!onPlayerWhitelist.handle(profile, c.parseJson())) {
                return c.sendStatus(Status.CONFLICT);
            }

            Whitelist.add(profile);
            return c.status(Status.OK).json(marshalPlayer(profile));
        } catch (CancellationException | CompletionException e) {
            System.out.println(e);
            return c.sendStatus(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static boolean removeWhitelist(Context c) throws IOException {
        Map<String, String> query = c.getQuery();
        if (!query.containsKey("uuid")) {
            return c.sendStatus(Status.BAD_REQUEST);
        }

        UUID uuid = Utils.parseUUID(query.get("uuid"));
        if (uuid == null) {
            return c.sendStatus(Status.BAD_REQUEST);
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if (!onRemoveWhitelist.handle(player.getPlayerProfile())) {
            return c.sendStatus(Status.CONFLICT);
        }

        Whitelist.remove(player);
        return c.sendStatus(Status.NO_CONTENT);
    }
}
