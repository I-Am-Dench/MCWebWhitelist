package theof.webwhitelist;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerProfile;

import java.io.*;

/*
* The default Spigot implementation for whitelisting is broken, so this is the fixed version
* */
public class Whitelist {

    private static final String whitelistFile = "whitelist.json";
    private static final Gson gson = new Gson();

    private static JsonArray readWhitelist() {
        JsonElement list = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(whitelistFile));
            list = JsonParser.parseReader(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list == null || !list.isJsonArray()) {
            return new JsonArray();
        }
        return list.getAsJsonArray();
    }

    private static boolean writeWhitelist(JsonArray array) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(whitelistFile));
            writer.write(array.toString());
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void add(PlayerProfile profile) {
        if (Bukkit.getOfflinePlayer(profile.getUniqueId()).isWhitelisted()) {
            return;
        }

        JsonArray array = readWhitelist();
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", profile.getUniqueId().toString());
        obj.addProperty("name", profile.getName());
        array.add(obj);

        if (writeWhitelist(array)) {
            Bukkit.reloadWhitelist();
        }
    }

    public static void remove(OfflinePlayer player) {
        if (!player.isWhitelisted()) {
            return;
        }

        JsonArray array = readWhitelist();
        for (JsonElement element : array) {
            try {
                User user = gson.fromJson(element, User.class);
                if (player.getUniqueId().equals(Utils.parseUUID(user.uuid))) {
                    array.remove(element);
                    break;
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        if (writeWhitelist(array)) {
            Bukkit.reloadWhitelist();
        }
    }

    private static class User {
        public String uuid;
    }
}
