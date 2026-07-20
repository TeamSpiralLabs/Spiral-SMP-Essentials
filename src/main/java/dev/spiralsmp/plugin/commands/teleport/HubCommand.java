package dev.spiralsmp.plugin.commands.teleport;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.commands.base.CommandInfo;
import dev.spiralsmp.plugin.commands.base.SpiralCommand;
import dev.spiralsmp.plugin.managers.CommandCooldownManager;
import dev.spiralsmp.plugin.managers.WarmupManager;
import dev.spiralsmp.plugin.utils.MessageUtil;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "hub",
        description = "Teleports the player to the hub",
        aliases = {"spawn", "lobby"},
        requiresCooldownCheck = true,
        requiresCombatCheck = true
)
public class HubCommand implements SpiralCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> executeAsPlayer(ctx, player -> {
                    if (!passesPreChecks(player)) return 0;

                    WarmupManager.getInstance().executeWithWarmup(player, getInfo().name(), "Teleporting", () -> {
                        Location spawnLocation = Bukkit.getWorlds().getFirst().getSpawnLocation();

                        player.teleportAsync(spawnLocation).thenAccept(success -> {
                            if (success) {
                                SoundUtil.SUCCESS.play(player);
                                MessageUtil.sendSuccessBar(player, "Teleported to World Spawn");
                                CommandCooldownManager.getInstance().setCooldown(player, getInfo().name());
                            } else {
                                SoundUtil.ERROR.play(player);
                            }
                        });
                    });

                    return Command.SINGLE_SUCCESS;
                }));
    }
}