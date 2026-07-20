package dev.spiralsmp.plugin.commands.base;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.spiralsmp.plugin.managers.CombatManager;
import dev.spiralsmp.plugin.managers.CommandCooldownManager;
import dev.spiralsmp.plugin.utils.MessageUtil;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public interface SpiralCommand {
    LiteralArgumentBuilder<CommandSourceStack> build();

    default CommandInfo getInfo() {
        CommandInfo cmdInfo = this.getClass().getAnnotation(CommandInfo.class);
        if (cmdInfo == null) {
            throw new IllegalStateException("Command class " + this.getClass().getSimpleName() + " is missing @CommandInfo annotation");
        }
        return cmdInfo;
    }

    default boolean passesPreChecks(Player player) {
        CommandInfo cmdInfo = getInfo();
        String cmdName = cmdInfo.name();

        CombatManager combatManager = CombatManager.getInstance();
        if (cmdInfo.requiresCombatCheck() && combatManager != null && combatManager.isInCombat(player)) {
            long secondsLeft = combatManager.getRemainingSeconds(player);
            MessageUtil.sendCombatCooldownBar(player, cmdName, secondsLeft);
            SoundUtil.ERROR.play(player);
            return false;
        }

        CommandCooldownManager cooldownManager = CommandCooldownManager.getInstance();
        if (cmdInfo.requiresCooldownCheck() && cooldownManager != null && cooldownManager.isOnCooldown(player, cmdName)) {
            long secondsLeft = cooldownManager.getRemainingSeconds(player, cmdName);
            MessageUtil.sendCommandCooldownBar(player, cmdName, secondsLeft);
            SoundUtil.ERROR.play(player);
            return false;
        }

        return true;
    }

    default int executeAsPlayer(CommandContext<CommandSourceStack> ctx, ToIntFunction<Player> action) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return 0;
        }
        return action.applyAsInt(player);
    }
}