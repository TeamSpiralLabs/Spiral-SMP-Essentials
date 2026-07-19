package dev.spiralsmp.plugin.registry;

public record CommandEntry(String command, String description, String permission, boolean isAlias) {
    public CommandEntry(String command, String description, String permission) {
        this(command, description, permission, false);
    }
}