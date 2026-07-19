package dev.spiralsmp.plugin.utils;

import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public enum SoundUtil {
    TICK(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5f, 1.2f),
    SUCCESS(org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, Sound.Source.MASTER, 0.5f, 1.0f),
    ERROR(org.bukkit.Sound.ENTITY_VILLAGER_NO, Sound.Source.MASTER, 0.5f, 1.0f),
    CANCELLED(org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, Sound.Source.MASTER, 1.0f, 0.5f);

    private final Sound sound;

    SoundUtil(org.bukkit.Sound bukkitSound, Sound.Source source, float volume, float pitch) {
        this.sound = Sound.sound(bukkitSound, source, volume, pitch);
    }

    public void play(Player player) {
        player.playSound(this.sound);
    }
}