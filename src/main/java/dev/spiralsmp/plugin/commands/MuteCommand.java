package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.managers.MuteManager;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "mute",
        description = "Mute a player.",
        permission = "spiralsmp.mute.admin"
)
public class MuteCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .requires(ctx -> ctx.getSender().hasPermission(getInfo().permission()))
                .then(Commands.argument("target", StringArgumentType.word())
                        // perm mute
                        .executes(ctx -> executeMute(ctx.getSource(), StringArgumentType.getString(ctx, "target"), -1))

                        // mute in minutes
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(1))
                                .executes(ctx -> executeMute(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "target"),
                                        IntegerArgumentType.getInteger(ctx, "minutes")
                                ))
                        )
                );
    }

    private int executeMute(CommandSourceStack source, String targetName, int minutes) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            source.getSender().sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
            return 0;
        }

        long durationMillis = minutes == -1 ? -1 : minutes * 60L * 1000L;
        MuteManager.getInstance().mute(target.getUniqueId(), durationMillis);

        String timeString = minutes == -1 ? "permanently" : "for " + minutes + " minute(s)";

        source.getSender().sendMessage(
                Component.text("You have muted " + target.getName() + " " + timeString + ".").color(NamedTextColor.GREEN)
        );
        target.sendMessage(
                Component.text("You have been muted " + timeString + ".").color(NamedTextColor.RED)
        );

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}