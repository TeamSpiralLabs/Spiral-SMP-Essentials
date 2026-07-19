package dev.spiralsmp.plugin.commands.tpa;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.commands.SpiralCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
abstract class TpaResponseCommand implements SpiralCommand {
    private final String argumentName;

    protected TpaResponseCommand(String argumentName) {
        this.argumentName = argumentName;
    }

    protected abstract void handleLatest(Player player);

    protected abstract void handleSpecific(Player player, String targetName);

    protected abstract Iterable<String> suggestionNames(UUID playerId);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> executeAsPlayer(ctx, player -> {
                    if (!passesPreChecks(player)) return 0;
                    handleLatest(player);
                    return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.argument(argumentName, StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                                return builder.buildFuture();
                            }
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String name : suggestionNames(player.getUniqueId())) {
                                if (name.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(name);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> executeAsPlayer(ctx, player -> {
                            if (!passesPreChecks(player)) return 0;
                            handleSpecific(player, StringArgumentType.getString(ctx, argumentName));
                            return Command.SINGLE_SUCCESS;
                        }))
                );
    }
}