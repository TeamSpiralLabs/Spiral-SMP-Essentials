package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import dev.spiralsmp.plugin.utils.SoundUtil;
import dev.spiralsmp.plugin.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarmupManager implements Listener {
    private static WarmupManager instance;
    private final Main plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<UUID, Location> startLocations = new HashMap<>();

    private WarmupManager(Main plugin) {
        this.plugin = plugin;
    }

    public static WarmupManager getInstance(Main plugin) {
        if (instance == null) {
            instance = new WarmupManager(plugin);
        }
        return instance;
    }

    public static WarmupManager getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            for (BukkitTask task : instance.activeTasks.values()) {
                task.cancel();
            }
            instance.activeTasks.clear();
            instance.startLocations.clear();
            instance = null;
        }
    }

    private long getWarmupSeconds(String commandName) {
        return plugin.getConfig().getLong("command-warmups." + commandName, 0);
    }

    public void executeWithWarmup(Player player, String commandName, String actionVerb, Runnable action) {
        long warmupSeconds = getWarmupSeconds(commandName);

        if (warmupSeconds <= 0) {
            action.run();
            return;
        }

        cancelWarmup(player, false);
        startLocations.put(player.getUniqueId(), player.getLocation());

        BukkitTask task = new BukkitRunnable() {
            long timeLeft = warmupSeconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    player.sendActionBar(Component.empty());
                    activeTasks.remove(player.getUniqueId());
                    startLocations.remove(player.getUniqueId());
                    action.run();
                    this.cancel();
                } else {
                    MessageUtil.sendWarmupTickBar(player, actionVerb, timeLeft);
                    SoundUtil.TICK.play(player);
                    timeLeft--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        activeTasks.put(player.getUniqueId(), task);
    }

    public void cancelWarmup(Player player, boolean notify) {
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        startLocations.remove(player.getUniqueId());

        if (task != null) {
            task.cancel();
            player.sendActionBar(Component.empty());

            if (notify) {
                SoundUtil.CANCELLED.play(player);
            }
        }
    }

@EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        Player player = event.getPlayer();
        if (!activeTasks.containsKey(player.getUniqueId())) return;

        Location start = startLocations.get(player.getUniqueId());
        if (start == null) return;

        if (start.getWorld() != event.getTo().getWorld() || start.distanceSquared(event.getTo()) > 0.1) {
            cancelWarmup(player, true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (activeTasks.containsKey(player.getUniqueId())) {
                cancelWarmup(player, true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelWarmup(event.getPlayer(), false);
    }
}