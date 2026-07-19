package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Date;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "tempban",
        description = "Temporarily ban a player.",
        permission = "spiralsmp.ban.admin"
)
public class TempBanCommand implements SpiralCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .requires(ctx -> ctx.getSender().hasPermission(getInfo().permission()))
                .then(Commands.argument("target", StringArgumentType.word())
                        .then(Commands.argument("minutes", IntegerArgumentType.integer(1))
                                // with a reason
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> executeTempBan(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "target"),
                                                IntegerArgumentType.getInteger(ctx, "minutes"),
                                                StringArgumentType.getString(ctx, "reason")
                                        ))
                                )
                                // without a reason
                                .executes(ctx -> executeTempBan(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "target"),
                                        IntegerArgumentType.getInteger(ctx, "minutes"),
                                        "No reason provided"
                                ))
                        )
                );
    }

    private int executeTempBan(CommandSourceStack source, String targetName, int minutes, String reason) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            source.getSender().sendMessage(Component.text("Player not found or is offline.").color(NamedTextColor.RED));
            return 0;
        }

        Instant unbanInstant = Instant.now().plusSeconds(minutes * 60L);
        Bukkit.getBanList(BanListType.PROFILE).addBan(
                target.getPlayerProfile(),
                reason,
                unbanInstant,
                source.getSender().getName()
        );

        Component kickMessage = Component.text("You are temporarily banned for " + minutes + " minute(s).\n\nReason: ").color(NamedTextColor.RED)
                .append(Component.text(reason).color(NamedTextColor.WHITE));
        target.kick(kickMessage);

        source.getSender().sendMessage(
                Component.text("You have temporarily banned " + target.getName() + " for " + minutes + " minute(s).").color(NamedTextColor.GREEN)
        );

        if (source.getSender() instanceof Player admin) SoundUtil.TICK.play(admin);
        return 1;
    }
}