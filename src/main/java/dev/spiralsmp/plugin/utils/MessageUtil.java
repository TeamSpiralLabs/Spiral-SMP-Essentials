package dev.spiralsmp.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class MessageUtil {
    private MessageUtil() {
        throw new UnsupportedOperationException("MessageUtil failed to start");
    }

    public static void sendActionBar(Player player, Component component) {
        player.sendActionBar(component);
    }

    public static void sendErrorBar(Player player, String message) {
        sendActionBar(player, Component.text(message).color(NamedTextColor.RED));
    }

    public static void sendSuccessBar(Player player, String message) {
        sendActionBar(player, Component.text(message).color(NamedTextColor.GREEN));
    }

    public static void sendCombatCooldownBar(Player player, String commandName, long secondsLeft) {
        Component message = Component.text("Cannot use ", NamedTextColor.RED)
                .append(Component.text("/" + commandName, NamedTextColor.GOLD))
                .append(Component.text(" in combat! Wait ", NamedTextColor.RED))
                .append(Component.text(secondsLeft + "s", NamedTextColor.GOLD))
                .append(Component.text(".", NamedTextColor.RED));

        sendActionBar(player, message);
    }

    public static void sendCommandCooldownBar(Player player, String commandName, long secondsLeft) {
        Component message = Component.text("You must wait ", NamedTextColor.YELLOW)
                .append(Component.text(secondsLeft + "s", NamedTextColor.GOLD))
                .append(Component.text(" before using ", NamedTextColor.YELLOW))
                .append(Component.text("/" + commandName, NamedTextColor.GOLD))
                .append(Component.text(" again.", NamedTextColor.YELLOW));

        sendActionBar(player, message);
    }

    public static void sendWarmupTickBar(Player player, String actionVerb, long secondsLeft) {
        Component message = Component.text(actionVerb + " in ", NamedTextColor.YELLOW)
                .append(Component.text(secondsLeft + "s", NamedTextColor.GOLD));

        sendActionBar(player, message);
    }
}