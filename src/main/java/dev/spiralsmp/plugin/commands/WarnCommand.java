package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.managers.WarnManager;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "warn",
        description = "Warn a player.",
        permission = "spiralsmp.warn.admin"
)
public class WarnCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .requires(ctx -> ctx.getSender().hasPermission(getInfo().permission()))
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> executeWarn(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "target"),
                                        StringArgumentType.getString(ctx, "reason")
                                ))
                        )
                );
    }

    private int executeWarn(CommandSourceStack source, String targetName, String reason) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            source.getSender().sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
            return 0;
        }

        int totalWarns = WarnManager.getInstance().addWarn(target.getUniqueId());

        target.sendMessage(Component.text("You have been warned! Reason: " + reason).color(NamedTextColor.RED));
        target.sendMessage(Component.text("You now have " + totalWarns + " warning(s).").color(NamedTextColor.YELLOW));
        SoundUtil.ERROR.play(target);

        source.getSender().sendMessage(
                Component.text("You warned " + target.getName() + " for: " + reason).color(NamedTextColor.GREEN)
        );

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}