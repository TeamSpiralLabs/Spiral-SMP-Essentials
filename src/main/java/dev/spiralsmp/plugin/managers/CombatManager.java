package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager implements Listener {
    private static CombatManager instance;
    private final Main plugin;
    private final Map<UUID, Long> combatLog = new HashMap<>();
    private BukkitTask actionBarTask;

    private CombatManager(Main plugin) {
        this.plugin = plugin;
        startActionBarTask();
    }

    public static CombatManager getInstance(Main plugin) {
        if (instance == null) {
            instance = new CombatManager(plugin);
        }
        return instance;
    }

    public static CombatManager getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            if (instance.actionBarTask != null) {
                instance.actionBarTask.cancel();
            }
            instance.combatLog.clear();
            instance = null;
        }
    }

    private long getCooldownMs() {
        return plugin.getConfig().getLong("combat-cooldown-seconds", 10) * 1000;
    }

    private void startActionBarTask() {
        actionBarTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentMs = System.currentTimeMillis();
            long cooldownMs = getCooldownMs();

            combatLog.entrySet().removeIf(entry -> {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player == null) return true;

                long elapsed = currentMs - entry.getValue();
                if (elapsed >= cooldownMs) {
                    player.sendActionBar(Component.text("You are no longer in combat", NamedTextColor.GREEN));
                    return true;
                } else {
                    long secondsLeft = ((cooldownMs - elapsed) / 1000) + 1;
                    player.sendActionBar(
                            Component.text("In Combat ", NamedTextColor.RED)
                                    .append(Component.text(secondsLeft + "s", NamedTextColor.GOLD))
                    );
                    return false;
                }
            });
        }, 0L, 20L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        if (event instanceof EntityDamageByEntityEvent damageByEntity) {
            Player attacker = null;

            if (damageByEntity.getDamager() instanceof Player damager) {
                attacker = damager;
            } else if (damageByEntity.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
                attacker = shooter;
            }

            // prevent self inflicted
            if (attacker != null && !attacker.equals(victim)) {
                tag(victim);
                tag(attacker);

                updateActionBarInstantly(victim);
                updateActionBarInstantly(attacker);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // check if in combat before quit
        if (isInCombat(player)) {
            player.setHealth(0.0);
        }
        combatLog.remove(player.getUniqueId());
    }

    private void tag(Player player) {
        combatLog.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void updateActionBarInstantly(Player player) {
        long secondsLeft = getRemainingSeconds(player);
        player.sendActionBar(
                Component.text("⚔ In Combat ", NamedTextColor.RED)
                        .append(Component.text(secondsLeft + "s", NamedTextColor.GOLD))
        );
    }

    public boolean isInCombat(Player player) {
        if (!combatLog.containsKey(player.getUniqueId())) return false;
        long timeSinceLastAttack = System.currentTimeMillis() - combatLog.get(player.getUniqueId());
        return timeSinceLastAttack < getCooldownMs();
    }

    public void clearCombat(Player player) {
        combatLog.remove(player.getUniqueId());
        player.sendActionBar(Component.empty());
    }

    public long getRemainingSeconds(Player player) {
        if (!isInCombat(player)) return 0;
        long timeLeft = getCooldownMs() - (System.currentTimeMillis() - combatLog.get(player.getUniqueId()));
        return (timeLeft / 1000) + 1;
    }
}