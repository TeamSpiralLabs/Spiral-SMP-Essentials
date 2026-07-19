package dev.spiralsmp.plugin.commands;

import dev.spiralsmp.plugin.Main;
import dev.spiralsmp.plugin.commands.tpa.TpAcceptCommand;
import dev.spiralsmp.plugin.commands.tpa.TpCancelCommand;
import dev.spiralsmp.plugin.commands.tpa.TpDenyCommand;
import dev.spiralsmp.plugin.commands.tpa.TpaCommand;
import dev.spiralsmp.plugin.commands.tpa.TpaManager;
import dev.spiralsmp.plugin.managers.BackupManager;
import dev.spiralsmp.plugin.registry.HelpRegistry;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandRegister {
    public static void registerAll(Commands paperCommands, HelpRegistry helpRegistry, TpaManager tpaManager, BackupManager backupManager, Main plugin) {
        helpRegistry.clear();

        List<SpiralCommand> commands = new ArrayList<>();

        // Core commands
        commands.add(new CoreCommand(plugin));

        if (plugin.getConfig().getBoolean("modules.hub", true)) {
            commands.add(new HubCommand());
        }

        if (plugin.getConfig().getBoolean("modules.home", true)) {
            commands.add(new HomeCommand());
        }

        if (tpaManager != null) {
            commands.add(new TpaCommand(tpaManager));
            commands.add(new TpAcceptCommand(tpaManager));
            commands.add(new TpDenyCommand(tpaManager));
            commands.add(new TpCancelCommand(tpaManager));
        }

        if (backupManager != null) {
            commands.add(new AutoBackupCommand(backupManager));
            commands.add(new BackupCommand(backupManager));
        }

        for (SpiralCommand cmd : commands) {
            CommandInfo cmdInfo = cmd.getInfo();
            String name = cmdInfo.name();
            String description = cmdInfo.description();
            String permission = cmdInfo.permission();
            List<String> aliases = List.of(cmdInfo.aliases());

            paperCommands.register(cmd.build().build(), description, aliases);

            helpRegistry.register("/" + name, description, permission);
            for (String alias : aliases) {
                helpRegistry.registerAlias("/" + alias, "Alias for /" + name + ".", permission);
            }
        }
    }
}