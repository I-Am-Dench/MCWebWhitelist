package theof.webwhitelist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import theof.webwhitelist.webserver.Context;

import java.math.BigInteger;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static Gson gson = new Gson();
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");

    public static long toSeconds(long millis) {
        return millis / 1000;
    }

    public static String colorize(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void log(Object msg) {
        CommandSender sender = Bukkit.getServer().getConsoleSender();
        sender.sendMessage(colorize(String.format("[&dWebWhitelist&r] %s", msg.toString())));
    }

    public static UUID parseUUID(String id) {
        if (id == null) {
            return null;
        }

        Matcher matcher = UUID_PATTERN.matcher(id);
        if (matcher.find()) {
            try {
                return UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {}
        }

        if (id.length() != 32) {
            return null;
        }

        // Taken from https://stackoverflow.com/questions/18986712/creating-a-uuid-from-a-string-with-no-dashes#answer-30760478
        try {
            BigInteger b1 = new BigInteger(id.substring(0, 16), 16);
            BigInteger b2 = new BigInteger(id.substring(16, 32), 16);
            return new UUID(b1.longValue(), b2.longValue());
        } catch (NumberFormatException ignored) {}

        return null;
    }

    public static UUID getUUIDFromJson(Context c) {
        JsonElement json = c.parseJson();
        UUIDPayload payload = null;

        try {
            payload = gson.fromJson(json, UUIDPayload.class);
        } catch (JsonSyntaxException ignored) {}

        if (payload == null) {
            return null;
        }

        return parseUUID(payload.uuid);
    }

    public static String getCodeFromJson(Context c) {
        JsonElement json = c.parseJson();
        CodePayload payload = null;

        try {
            payload = gson.fromJson(json, CodePayload.class);
        } catch (JsonSyntaxException ignored) {}

        if (payload == null) {
            return null;
        }

        return payload.code;
    }

    private static class UUIDPayload {
        public String uuid;
    }

    private static class CodePayload {
        public String code;
    }
}
