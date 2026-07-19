package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.managers.BackupManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "autobackup",
        description = "Manage the SMP auto-backup system.",
        permission = "spiralsmp.backup.admin"
)
public class AutoBackupCommand implements SpiralCommand {

    private final BackupManager backupManager;

    public AutoBackupCommand(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                // Ensure only admins can execute or see this in tab-completion
                .requires(source -> source.getSender().hasPermission(getInfo().permission()))

                .then(Commands.literal("start")
                        .executes(ctx -> {
                            backupManager.startAutoBackup();
                            ctx.getSource().getSender().sendRichMessage("<green>Auto-backup scheduled.</green>");
                            return Command.SINGLE_SUCCESS;
                        }))

                .then(Commands.literal("stop")
                        .executes(ctx -> {
                            backupManager.stopAutoBackup();
                            ctx.getSource().getSender().sendRichMessage("<red>Auto-backup temporarily stopped.</red>");
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}