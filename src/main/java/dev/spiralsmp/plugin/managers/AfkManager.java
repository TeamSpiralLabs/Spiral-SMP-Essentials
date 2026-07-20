package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager implements Listener {
    private static AfkManager instance;
    private final Main plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private BukkitTask afkCheckTask;

    private AfkManager(Main plugin) {
        this.plugin = plugin;
        startAfkCheckTask();
    }

    public static AfkManager getInstance(Main plugin) {
        if (instance == null) {
            instance = new AfkManager(plugin);
        }
        return instance;
    }

    public static AfkManager getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            if (instance.afkCheckTask != null) {
                instance.afkCheckTask.cancel();
            }
            instance.lastActivity.clear();
            instance = null;
        }
    }

    private long getKickThresholdMs() {
        return plugin.getConfig().getLong("afk.kick-minutes", 15) * 60 * 1000;
    }

    private void startAfkCheckTask() {
        // run every 20 ticks (1 sec)
        afkCheckTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentMs = System.currentTimeMillis();
            long thresholdMs = getKickThresholdMs();

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.hasPermission("spiralsmp.afk.bypass")) continue;

                long lastActive = lastActivity.getOrDefault(player.getUniqueId(), currentMs);

                if (currentMs - lastActive >= thresholdMs) {
                    player.kick(
                            Component.text("You have been kicked for being AFK for too long.", NamedTextColor.RED)
                    );
                    lastActivity.remove(player.getUniqueId());
                }
            }
        }, 0L, 20L);
    }

    private void updateActivity(Player player) {
        long now = System.currentTimeMillis();
        long last = lastActivity.getOrDefault(player.getUniqueId(), 0L);

        // debounce
        if (now - last > 1000) {
            lastActivity.put(player.getUniqueId(), now);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastActivity.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition() || event.hasChangedOrientation()) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }
}