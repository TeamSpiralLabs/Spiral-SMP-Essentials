package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "ping",
        description = "Check your latency on the server."
)
public class PingCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> executeAsPlayer(ctx, player -> {
                    int ping = player.getPing();
                    NamedTextColor color = ping < 100 ? NamedTextColor.GREEN : (ping < 200 ? NamedTextColor.YELLOW : NamedTextColor.RED);

                    player.sendMessage(
                            Component.text("Your ping is: ").color(NamedTextColor.GRAY).append(Component.text(ping + "ms").color(color))
                    );

                    return Command.SINGLE_SUCCESS;
                }));
    }
}