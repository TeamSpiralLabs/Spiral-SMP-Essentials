package dev.spiralsmp.plugin.commands.admin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.commands.base.CommandInfo;
import dev.spiralsmp.plugin.commands.base.SpiralCommand;
import dev.spiralsmp.plugin.managers.WarnManager;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "warncount",
        description = "Check how many warnings a player has.",
        aliases = {"warnings"},
        permission = "spiralsmp.warn.admin"
)
public class WarnCountCommand implements SpiralCommand {
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
                        .executes(ctx -> executeCheck(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "target")
                        ))
                );
    }

    private int executeCheck(CommandSourceStack source, String targetName) {
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            source.getSender().sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
            return 0;
        }

        int warnCount = WarnManager.getInstance().getWarns(target.getUniqueId());

        source.getSender().sendMessage(
                Component.text("Total Warnings for ").color(NamedTextColor.GRAY)
                        .append(Component.text(target.getName()).color(NamedTextColor.GOLD))
                        .append(Component.text(": ").color(NamedTextColor.GRAY))
                        .append(Component.text(warnCount).color(warnCount > 0 ? NamedTextColor.RED : NamedTextColor.GREEN)));

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}