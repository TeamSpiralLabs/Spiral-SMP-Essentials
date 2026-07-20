package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Statistic;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "playertime",
        description = "Check your total playtime on the server.",
        aliases = {"playtime", "pt"}
)
public class PlayertimeCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> executeAsPlayer(ctx, player -> {
                    int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                    long totalSeconds = ticksPlayed / 20;

                    long days = totalSeconds / 86400;
                    long hours = (totalSeconds % 86400) / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    StringBuilder timeString = new StringBuilder();
                    if (days > 0) timeString.append(days).append("d ");
                    if (hours > 0) timeString.append(hours).append("h ");
                    if (minutes > 0) timeString.append(minutes).append("m ");
                    timeString.append(seconds).append("s");

                    player.sendMessage(
                            Component.text("Your playtime: ").color(NamedTextColor.GRAY)
                                    .append(Component.text(timeString.toString().trim()).color(NamedTextColor.GREEN))
                    );

                    return Command.SINGLE_SUCCESS;
                }));
    }
}