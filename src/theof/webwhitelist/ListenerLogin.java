package theof.webwhitelist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ListenerLogin implements Listener {

    public ListenerLogin() {
        Bukkit.getPluginManager().registerEvents(this, WebWhitelist.plugin());
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        PlayerLoginEvent.Result result = e.getResult();
        if (result == PlayerLoginEvent.Result.KICK_WHITELIST && WebWhitelist.plugin().requests().hasRequested(player.getUniqueId())) {
            String code = WebWhitelist.plugin().requests().getCode(player.getUniqueId());
            if (code != null) {
                String message = Utils.colorize(String.format("Your link code is: &a%s", code));
                e.setKickMessage(message);
            }
        }
    }
}
