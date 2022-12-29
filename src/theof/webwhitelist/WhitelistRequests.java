package theof.webwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhitelistRequests {

    private static final long REQUEST_LIFETIME = 600; // In seconds

    private static final String CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final Map<String, LinkRequest> REQUESTS = new HashMap<>();
    private final Map<String, Integer> TASK_IDS = new HashMap<>();
    private final Map<UUID, LinkRequest> REQUESTS_BY_PLAYER = new HashMap<>();

    public long createRequest(UUID uuid) {
        String code = generateCode(CODE_LENGTH);
        LinkRequest request = new LinkRequest(code, uuid);
        this.REQUESTS.put(code, request);
        this.REQUESTS_BY_PLAYER.put(uuid, request);

        long delay = request.getExpiration() - Utils.toSeconds(Clock.systemUTC().millis());
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(WebWhitelist.plugin(), () -> {
            popRequest(code);
        }, delay * 20); // * 20 converts seconds into game ticks

        this.TASK_IDS.put(code, taskId);

        return request.getExpiration();
    }

    public LinkRequest popRequest(String code) {
        LinkRequest request = this.REQUESTS.get(code);
        if (request == null) {
            return null;
        }

        Bukkit.getScheduler().cancelTask(this.TASK_IDS.get(code));
        this.TASK_IDS.remove(code);
        this.REQUESTS_BY_PLAYER.remove(request.getUUID());
        this.REQUESTS.remove(code);

        return request;
    }

    public boolean hasRequested(UUID uuid) {
        return this.REQUESTS_BY_PLAYER.containsKey(uuid);
    }

    public long getExpiration(UUID uuid) {
        LinkRequest request = this.REQUESTS_BY_PLAYER.get(uuid);
        if (request != null) {
            return request.getExpiration();
        }
        return -1;
    }

    public String getCode(UUID uuid) {
        LinkRequest request = this.REQUESTS_BY_PLAYER.get(uuid);
        if (request != null) {
            return request.code;
        }
        return null;
    }

    private char randomChar(String s) {
        double rand = Math.random();
        int i = (int)(rand * s.length());
        return s.charAt(i);
    }

    private String generateString(int length) {
        StringBuilder builder = new StringBuilder();

        for (int i=0; i < length; i++)
            builder.append(randomChar(CODE_CHARS));

        return builder.toString();
    }

    private String generateCode(int length) {
        String code;
        do {
            code = generateString(length);
        } while (this.REQUESTS.containsKey(code));

        return code;
    }

    public static class LinkRequest {
        private final UUID uuid;
        private final String code;
        private final long expiration;

        public LinkRequest(String code, UUID uuid) {
            this.code = code;
            this.uuid = uuid;
            this.expiration = Utils.toSeconds(Clock.systemUTC().millis()) + REQUEST_LIFETIME;
        }

        public UUID getUUID() {
            return this.uuid;
        }

        public OfflinePlayer getPlayer() {
            return Bukkit.getOfflinePlayer(this.uuid);
        }

        public long getExpiration() {
            return this.expiration;
        }
    }
}
