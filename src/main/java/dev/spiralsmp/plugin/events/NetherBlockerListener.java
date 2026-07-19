package dev.spiralsmp.plugin.events;

import dev.spiralsmp.plugin.utils.MessageUtil;
import dev.spiralsmp.plugin.utils.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetherBlockerListener implements Listener {
    private final Map<UUID, Long> messageCooldowns = new HashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 3000; // 3 seconds

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Intercept only if the teleport was caused by stepping into a Nether Portal
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            if (!messageCooldowns.containsKey(playerId) || (currentTime - messageCooldowns.get(playerId)) > MESSAGE_COOLDOWN_MS) {
                messageCooldowns.put(playerId, currentTime);

                MessageUtil.sendErrorBar(player, "The Nether dimension is currently locked!");
                SoundUtil.ERROR.play(player);
            }
        }
    }
}