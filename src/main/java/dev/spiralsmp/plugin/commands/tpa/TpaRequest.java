package dev.spiralsmp.plugin.commands.tpa;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

final class TpaRequest {
    private final UUID sender;
    private final UUID receiver;
    private final long timestamp;
    private BukkitTask expirationTask;

    TpaRequest(UUID sender, UUID receiver, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    UUID sender() {
        return sender;
    }

    UUID receiver() {
        return receiver;
    }

    long timestamp() {
        return timestamp;
    }

    void setExpirationTask(BukkitTask task) {
        this.expirationTask = task;
    }

    void cancelExpiration() {
        if (expirationTask != null) {
            expirationTask.cancel();
            expirationTask = null;
        }
    }
}