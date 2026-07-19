package dev.spiralsmp.plugin.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelpRegistry {
    private static HelpRegistry instance;
    private final List<CommandEntry> entries = new ArrayList<>();

    private HelpRegistry() {
    }

    public static HelpRegistry getInstance() {
        if (instance == null) {
            instance = new HelpRegistry();
        }
        return instance;
    }

    public void register(String command, String description, String permission) {
        entries.add(new CommandEntry(command, description, permission, false));
    }

    public void registerAlias(String command, String description, String permission) {
        entries.add(new CommandEntry(command, description, permission, true));
    }

    public List<CommandEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}