package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.managers.BackupManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "backup",
        description = "Manually trigger a server backup.",
        permission = "spiralsmp.backup.admin"
)
public class BackupCommand implements SpiralCommand {
    private final BackupManager backupManager;

    public BackupCommand(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .requires(source -> source.getSender().isOp())
                .executes(ctx -> {
                    backupManager.runManualBackup(ctx.getSource());
                    return Command.SINGLE_SUCCESS;
                });
    }
}