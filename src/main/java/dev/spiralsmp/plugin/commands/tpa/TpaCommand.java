package dev.spiralsmp.plugin.commands.tpa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.spiralsmp.plugin.commands.CommandInfo;
import dev.spiralsmp.plugin.commands.SpiralCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "tpa",
        description = "TP to the specified Player",
        requiresCombatCheck = true
)
public class TpaCommand implements SpiralCommand {

    private final TpaManager tpaManager;

    public TpaCommand(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .then(Commands.argument("target", ArgumentTypes.player())
                        .suggests((ctx, builder) -> {
                            if (!(ctx.getSource().getExecutor() instanceof Player sender)) {
                                return builder.buildFuture();
                            }

                            String remaining = builder.getRemaining().toLowerCase();
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                // only suggest the name if it matches what they are typing not themselves
                                if (!onlinePlayer.equals(sender) && onlinePlayer.getName().toLowerCase().startsWith(remaining)) {
                                    builder.suggest(onlinePlayer.getName());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> executeAsPlayer(ctx, sender -> {
                            if (!passesPreChecks(sender)) return 0;

                            try {
                                Player target = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).getFirst();
                                tpaManager.sendRequest(sender, target);
                            } catch (CommandSyntaxException ignored) {
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                );
    }
}