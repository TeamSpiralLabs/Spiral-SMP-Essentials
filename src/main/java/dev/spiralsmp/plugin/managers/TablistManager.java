package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TablistManager implements Listener {
    private static TablistManager instance;
    private final Main plugin;
    private BukkitTask updateTask;

    private TablistManager(Main plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public static TablistManager getInstance(Main plugin) {
        if (instance == null) {
            instance = new TablistManager(plugin);
        }
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            if (instance.updateTask != null) {
                instance.updateTask.cancel();
            }
            instance = null;
        }
    }

    private void startUpdateTask() {
        long intervalSeconds = plugin.getConfig().getLong("tablist-update-interval-seconds", 5);
        long intervalTicks = Math.max(1, intervalSeconds) * 20L;
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTablist(player);
                }
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateTablist(event.getPlayer());
    }

    private void updateTablist(Player player) {
        String serverNameFormat = plugin.getConfig().getString("tablist-server-name", "Server Name");

        // header
        Component header = Component.text("\n")
                .append(MiniMessage.miniMessage().deserialize(serverNameFormat))
                .append(Component.text("\n"))
                .append(Component.text(Bukkit.getOnlinePlayers().size() + " Players", NamedTextColor.WHITE))
                .append(Component.text("\n"));

        // footer
        long playTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long totalMinutes = playTicks / 20 / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String playtimeText = hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";

        int kills = player.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = player.getStatistic(Statistic.DEATHS);

        Component footer = Component.text("\n")
                .append(Component.text("Playtime: ", NamedTextColor.AQUA))
                .append(Component.text(playtimeText, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Kills: ", NamedTextColor.GREEN))
                .append(Component.text(kills, NamedTextColor.WHITE))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text("Deaths: ", NamedTextColor.RED))
                .append(Component.text(deaths, NamedTextColor.WHITE))
                .append(Component.text("\n"));

        player.sendPlayerListHeaderAndFooter(header, footer);

        // ping
        int ping = player.getPing();
        NamedTextColor pingColor;
        if (ping < 80) {
            pingColor = NamedTextColor.GREEN;
        } else if (ping < 160) {
            pingColor = NamedTextColor.YELLOW;
        } else {
            pingColor = NamedTextColor.RED;
        }

        Component tabName = Component.text(player.getName()).color(NamedTextColor.WHITE)
                .append(Component.space())
                .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                .append(Component.text(ping + "ms").color(pingColor))
                .append(Component.text("]").color(NamedTextColor.DARK_GRAY));

        player.playerListName(tabName);
    }
}