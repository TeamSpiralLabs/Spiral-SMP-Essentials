package dev.spiralsmp.plugin.commands.teleport;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.Main;
import dev.spiralsmp.plugin.commands.base.CommandInfo;
import dev.spiralsmp.plugin.commands.base.SpiralCommand;
import dev.spiralsmp.plugin.managers.CommandCooldownManager;
import dev.spiralsmp.plugin.managers.WarmupManager;
import dev.spiralsmp.plugin.utils.MessageUtil;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "rtp",
        description = "Randomly teleport to a safe location.",
        aliases = {"randomtp"},
        requiresCombatCheck = true,
        requiresCooldownCheck = true
)
public class RtpCommand implements SpiralCommand {
    private final Main plugin;

    // unsafe materials (prevent tp here)
    private static final Set<Material> UNSAFE_MATERIALS = Set.of(
            Material.LAVA, Material.WATER, Material.MAGMA_BLOCK, Material.CACTUS, Material.FIRE, Material.POWDER_SNOW
    );

    public RtpCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> executeAsPlayer(ctx, this::executeRtp));
    }

    private int executeRtp(Player player) {
        if (!passesPreChecks(player)) return 0;

        int minRadius = plugin.getConfig().getInt("rtp.min-radius", 500);
        int maxRadius = plugin.getConfig().getInt("rtp.max-radius", 5000);

        WarmupManager.getInstance().executeWithWarmup(player, getInfo().name(), "Searching for location", () -> {

            findSafeLocation(player.getWorld(), minRadius, maxRadius).thenAccept(location -> {
                if (location == null) {
                    player.sendMessage(Component.text("Could not find a safe location. Please try again.").color(NamedTextColor.RED));
                    SoundUtil.ERROR.play(player);
                    return;
                }

                player.teleportAsync(location).thenAccept(success -> {
                    if (success) {
                        SoundUtil.SUCCESS.play(player);
                        MessageUtil.sendSuccessBar(player, "Randomly teleported!");
                        CommandCooldownManager.getInstance().setCooldown(player, getInfo().name());
                    } else {
                        player.sendMessage(Component.text("Teleportation failed. Something went wrong.").color(NamedTextColor.RED));
                        SoundUtil.ERROR.play(player);
                    }
                });
            });
        });

        return Command.SINGLE_SUCCESS;
    }

    private CompletableFuture<Location> findSafeLocation(World world, int min, int max) {
        return attemptFindSafeLocation(world, min, max, 10);
    }

    private CompletableFuture<Location> attemptFindSafeLocation(World world, int min, int max, int attemptsLeft) {
        if (attemptsLeft <= 0) {
            return CompletableFuture.completedFuture(null); // Failed to find a spot
        }

        int x = getRandomCoordinate(min, max);
        int z = getRandomCoordinate(min, max);

        // load chunk
        return world.getChunkAtAsync(x >> 4, z >> 4).thenCompose(chunk -> {
            int y = world.getHighestBlockYAt(x, z);

            Block block = world.getBlockAt(x, y, z);
            Block blockAbove = world.getBlockAt(x, y + 1, z);
            Block blockAbove2 = world.getBlockAt(x, y + 2, z);

            // check if block is solid and safe
            if (!UNSAFE_MATERIALS.contains(block.getType()) && block.getType().isSolid()
                    && blockAbove.getType().isAir() && blockAbove2.getType().isAir()) {

                // center player
                Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);
                return CompletableFuture.completedFuture(loc);
            } else {
                // if unsafe
                return attemptFindSafeLocation(world, min, max, attemptsLeft - 1);
            }
        });
    }

    private int getRandomCoordinate(int min, int max) {
        int coord = ThreadLocalRandom.current().nextInt(min, max + 1);
        return ThreadLocalRandom.current().nextBoolean() ? coord : -coord;
    }
}