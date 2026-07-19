package dev.spiralsmp.plugin.events;

import dev.spiralsmp.plugin.commands.tpa.TpaManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final TpaManager tpaManager;

    public PlayerQuitListener(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tpaManager.cleanupOnQuit(event.getPlayer());
    }
}
