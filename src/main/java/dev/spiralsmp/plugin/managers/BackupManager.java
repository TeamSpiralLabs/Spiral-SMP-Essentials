package dev.spiralsmp.plugin.managers;

import dev.spiralsmp.plugin.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {
    private final Main plugin;
    private ScheduledTask backupTask;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public BackupManager(Main plugin) {
        this.plugin = plugin;
    }

    public void startAutoBackup() {
        if (backupTask != null && !backupTask.isCancelled()) {
            return;
        }

        long interval = plugin.getConfig().getLong("backup.interval-minutes", 240);
        backupTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, _ -> executeBackup(null), 1, interval, TimeUnit.MINUTES);
    }

    // NOTE: this only cancels the currently scheduled/runtime task
    public void stopAutoBackup() {
        if (backupTask != null) {
            backupTask.cancel();
            backupTask = null;
        }
    }

    // Manual backup
    public void runManualBackup(CommandSourceStack source) {
        Bukkit.getAsyncScheduler().runNow(plugin, _ -> executeBackup(source));
    }

    private void executeBackup(CommandSourceStack source) {
        String mode = (source == null) ? "automated background" : "manual background";
        plugin.getLogger().info("Starting " + mode + " backup...");

        if (source != null) {
            source.getSender().sendRichMessage("<yellow>Starting manual background backup sequence...</yellow>");
        }

        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> saveFuture = new CompletableFuture<>();

        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            try {
                plugin.getLogger().info("Pausing world saves and flushing data to disk...");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-off");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all flush");
                saveFuture.complete(null);
            } catch (Throwable t) {
                saveFuture.completeExceptionally(t);
            }
        });

        try {
            saveFuture.join();

            Path serverRoot = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
            Path backupFolder = serverRoot.resolve("backups");

            if (!Files.exists(backupFolder)) {
                Files.createDirectories(backupFolder);
            }

            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            Path zipFilePath = backupFolder.resolve("backup_" + timestamp + ".zip");

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
                Files.walkFileTree(serverRoot, new SimpleFileVisitor<>() {
                    @Override
                    public @NonNull FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs) {
                        if (dir.equals(backupFolder)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().equals("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        }

                        Path targetFile = serverRoot.relativize(file);
                        ZipEntry zipEntry = new ZipEntry(targetFile.toString().replace("\\", "/"));
                        zos.putNextEntry(zipEntry);

                        Files.copy(file, zos);
                        zos.closeEntry();

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NonNull FileVisitResult visitFileFailed(@NonNull Path file, @NonNull IOException exc) {
                        plugin.getLogger().warning("[Backup] Failed to read file: " + file + " - " + exc.getMessage());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            long timeTaken = System.currentTimeMillis() - startTime;
            String completionMessage = String.format("Backup completed successfully in %dms! Saved to: %s", timeTaken, zipFilePath.getFileName());

            plugin.getLogger().info(completionMessage);
            if (source != null) {
                source.getSender().sendRichMessage("<green>" + completionMessage + "</green>");
            }

            cleanupOldBackups(backupFolder);

        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to process backup sequence", e);
            if (source != null) {
                source.getSender().sendRichMessage("<red>Backup failed!</red>");
            }
        } finally {
            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                plugin.getLogger().info("Backup sequence ended. Re-enabling world saves.");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-on");
            });
        }
    }

    private void cleanupOldBackups(Path backupFolder) {
        try (Stream<Path> stream = Files.list(backupFolder)) {
            List<Path> backups = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("backup_") && path.toString().endsWith(".zip"))
                    .sorted((p1, p2) -> p2.getFileName().compareTo(p1.getFileName()))
                    .toList();

            int maxBackups = plugin.getConfig().getInt("backup.max-limit", 4);

            if (backups.size() > maxBackups) {
                for (int i = maxBackups; i < backups.size(); i++) {
                    Path oldBackup = backups.get(i);
                    Files.deleteIfExists(oldBackup);
                    plugin.getLogger().info("Deleted old backup: " + oldBackup.getFileName());
                }
            }

        } catch (IOException e) {
            plugin.getComponentLogger().error("Failed to clean up old backups", e);
        }
    }
}