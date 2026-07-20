package dev.spiralsmp.plugin.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.commands.base.CommandInfo;
import dev.spiralsmp.plugin.commands.base.SpiralCommand;
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
        name = "unmute",
        description = "Unmute a player.",
        permission = "spiralsmp.unmute.admin"
)
public class UnmuteCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .requires(ctx -> ctx.getSender().hasPermission(getInfo().permission()))
                .then(Commands.argument("target", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            String remaining = builder.getRemaining().toLowerCase();
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getName().toLowerCase().startsWith(remaining)) {
                                    builder.suggest(p.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> executeUnmute(ctx.getSource(), StringArgumentType.getString(ctx, "target")))
                );
    }

    private int executeUnmute(CommandSourceStack source, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            source.getSender().sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
            return 0;
        }

        if (!MuteManager.getInstance().isMuted(target.getUniqueId())) {
            source.getSender().sendMessage(Component.text(target.getName() + " is not currently muted.").color(NamedTextColor.RED));
            return 0;
        }

        MuteManager.getInstance().unmute(target.getUniqueId());

        source.getSender().sendMessage(
                Component.text("You have unmuted " + target.getName() + ".").color(NamedTextColor.GREEN)
        );
        target.sendMessage(
                Component.text("You have been unmuted.").color(NamedTextColor.GREEN)
        );

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}