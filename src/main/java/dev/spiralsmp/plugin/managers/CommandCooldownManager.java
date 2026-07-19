package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandCooldownManager implements Listener {
    private static CommandCooldownManager instance;
    private final Main plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    private CommandCooldownManager(Main plugin) {
        this.plugin = plugin;
    }

    public static CommandCooldownManager getInstance(Main plugin) {
        if (instance == null) {
            instance = new CommandCooldownManager(plugin);
        }
        return instance;
    }

    public static CommandCooldownManager getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.cooldowns.clear();
            instance = null;
        }
    }

    private long getCooldownMs(String commandName) {
        return plugin.getConfig().getLong("command-cooldowns." + commandName, 0) * 1000;
    }

    public boolean isOnCooldown(Player player, String commandName) {
        long cooldownMs = getCooldownMs(commandName);
        if (cooldownMs <= 0) return false;
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null || !playerCooldowns.containsKey(commandName)) return false;

        return System.currentTimeMillis() < playerCooldowns.get(commandName);
    }

    public long getRemainingSeconds(Player player, String commandName) {
        if (!isOnCooldown(player, commandName)) return 0;

        long expirationTime = cooldowns.get(player.getUniqueId()).get(commandName);
        long timeLeft = expirationTime - System.currentTimeMillis();
        return (timeLeft / 1000) + 1;
    }

    public void setCooldown(Player player, String commandName) {
        long cooldownMs = getCooldownMs(commandName);
        if (cooldownMs <= 0) return;

        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                 .put(commandName, System.currentTimeMillis() + cooldownMs);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}