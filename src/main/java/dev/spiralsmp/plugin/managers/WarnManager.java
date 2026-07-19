package dev.spiralsmp.plugin.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarnManager {
    private static WarnManager instance;
    private final Map<UUID, Integer> warns = new HashMap<>();

    public WarnManager() {
        instance = this;
    }

    public static WarnManager getInstance() {
        return instance;
    }

    public int addWarn(UUID uuid) {
        warns.put(uuid, warns.getOrDefault(uuid, 0) + 1);
        return warns.get(uuid);
    }

    public int getWarns(UUID uuid) {
        return warns.getOrDefault(uuid, 0);
    }
}