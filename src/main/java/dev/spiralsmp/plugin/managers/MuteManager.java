package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager implements Listener {
    private static MuteManager instance;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();

    public MuteManager(JavaPlugin plugin) {
        instance = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static MuteManager getInstance() {
        return instance;
    }

    public void mute(UUID uuid, long durationMillis) {
        long expiry = durationMillis == -1 ? -1 : System.currentTimeMillis() + durationMillis;
        mutedPlayers.put(uuid, expiry);
    }

    public void unmute(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    public boolean isMuted(UUID uuid) {
        if (!mutedPlayers.containsKey(uuid)) return false;

        long expiry = mutedPlayers.get(uuid);
        if (expiry == -1) return true; // -1 permanent

        if (System.currentTimeMillis() > expiry) {
            mutedPlayers.remove(uuid); // mute expired
            return false;
        }
        return true;
    }

    public long getRemainingMillis(UUID uuid) {
        if (!mutedPlayers.containsKey(uuid)) return 0;
        long expiry = mutedPlayers.get(uuid);
        if (expiry == -1) return -1;
        return Math.max(0, expiry - System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (isMuted(player.getUniqueId())) {
            event.setCancelled(true);

            long remaining = getRemainingMillis(player.getUniqueId());
            if (remaining == -1) {
                player.sendMessage(Component.text("You are permanently muted.").color(NamedTextColor.RED));
            } else {
                long minutes = (remaining / 1000) / 60;
                long seconds = (remaining / 1000) % 60;
                player.sendMessage(Component.text("You are muted for " + minutes + "m " + seconds + "s.").color(NamedTextColor.RED));
            }
            SoundUtil.ERROR.play(player);
        }
    }
}