package dev.spiralsmp.plugin;

import dev.spiralsmp.plugin.commands.CommandRegister;
import dev.spiralsmp.plugin.commands.tpa.TpaManager;
import dev.spiralsmp.plugin.events.EndBlockerListener;
import dev.spiralsmp.plugin.events.PlayerDeathListener;
import dev.spiralsmp.plugin.events.PlayerQuitListener;
import dev.spiralsmp.plugin.managers.BackupManager;
import dev.spiralsmp.plugin.managers.CombatManager;
import dev.spiralsmp.plugin.managers.CommandCooldownManager;
import dev.spiralsmp.plugin.managers.TablistManager;
import dev.spiralsmp.plugin.managers.WarmupManager;
import dev.spiralsmp.plugin.registry.HelpRegistry;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private BackupManager backupManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 1. Backups
        if (getConfig().getBoolean("modules.backups", true)) {
            this.backupManager = new BackupManager(this);
            this.backupManager.startAutoBackup();
        }

        // 2. TPA System
        TpaManager tpaManager = null;
        if (getConfig().getBoolean("modules.tpa", true)) {
            tpaManager = new TpaManager(this);
            getServer().getPluginManager().registerEvents(new PlayerQuitListener(tpaManager), this);
        }

        // 3. Command Registry
        HelpRegistry helpRegistry = HelpRegistry.getInstance();
        TpaManager finalTpaManager = tpaManager;
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                CommandRegister.registerAll(event.registrar(), helpRegistry, finalTpaManager, this.backupManager, this));

        // 4. Combat Log
        if (getConfig().getBoolean("modules.combat-log", true)) {
            getServer().getPluginManager().registerEvents(CombatManager.getInstance(this), this);
        }

        // 5. Tablist
        if (getConfig().getBoolean("modules.tablist", true)) {
            getServer().getPluginManager().registerEvents(TablistManager.getInstance(this), this);
        }

        // 6. End Blocker
        if (getConfig().getBoolean("modules.end-blocker", false)) {
            getServer().getPluginManager().registerEvents(new EndBlockerListener(), this);
        }

        // Systems that are foundational and ALWAYS enabled
        getServer().getPluginManager().registerEvents(WarmupManager.getInstance(this), this);
        getServer().getPluginManager().registerEvents(CommandCooldownManager.getInstance(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        getLogger().info("Spiral-SMP-Essentials enabled");
    }

    @Override
    public void onDisable() {
        if (this.backupManager != null) {
            this.backupManager.stopAutoBackup();
        }

        TablistManager.shutdown();
        CombatManager.shutdown();
        WarmupManager.shutdown();
        CommandCooldownManager.shutdown();

        getLogger().info("Spiral-SMP-Essentials disabled");
    }
}
