package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarnManager {
    private static WarnManager instance;
    private final Main plugin;
    private final Map<UUID, Integer> warns = new HashMap<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    public WarnManager(Main plugin) {
        this.plugin = plugin;
        instance = this;

        setupDataFile();
        loadWarns();
    }

    public static WarnManager getInstance() {
        return instance;
    }

    private void setupDataFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml!");
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadWarns() {
        if (dataConfig.contains("warnings")) {
            for (String uuidStr : dataConfig.getConfigurationSection("warnings").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    int count = dataConfig.getInt("warnings." + uuidStr);
                    warns.put(uuid, count);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID found in data.yml: " + uuidStr);
                }
            }
        }
    }

    public void saveWarns() {
        dataConfig.set("warnings", null);

        for (Map.Entry<UUID, Integer> entry : warns.entrySet()) {
            dataConfig.set("warnings." + entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save to data.yml!");
        }
    }

    public int addWarn(UUID uuid) {
        int newCount = warns.getOrDefault(uuid, 0) + 1;
        warns.put(uuid, newCount);

        saveWarns();

        return newCount;
    }

    public int getWarns(UUID uuid) {
        return warns.getOrDefault(uuid, 0);
    }
}