# Spiral-SMP-Essentials

Make your Minecraft SMP's fun and easier while still keeping the vanilla feel.

## Features

- **Combat Logging:** Prevents players from running escape commands (like `/spawn` or `/tpa`) while in combat.
- **Command Warmups & Cooldowns:** Fully configurable delays to commands.
- **Automated Backups:** Zip your server's data at configurable intervals with a maximum retention limit.
- **World Management:** Restrict access to the Nether and The End with simple toggles.
- **Administration Tools:** `/warn`, `/mute`, `/unmute`, `/tempban`.
- **Teleportation:** `/tpa`, `/home`, `/hub`,`/rtp`.

---

## Requirements

- **Minecraft / Server Software:** Paper 26.1+
- **Java:** Java 25
- _Note: Fully compatible with Geyser and Floodgate._

---

## Commands & Permissions

### Player Commands

| Command           | Description                           |
| :---------------- | :------------------------------------ |
| `/spiralsmp help` | View all available commands.          |
| `/spiralsmp info` | View plugin version and details.      |
| `/tpa <player>`   | Send a teleport request.              |
| `/tpaccept`       | Accept a teleport request.            |
| `/tpdeny`         | Deny a teleport request.              |
| `/tpcancel`       | Cancel an outgoing request.           |
| `/home`           | Teleport to your home.                |
| `/hub`            | Teleport to the server hub.           |
| `/rtp`            | Randomly teleport to a safe location. |

### Admin Commands

| Command                                        | Description                               | Permission               |
| :--------------------------------------------- | :---------------------------------------- | :----------------------- |
| `/spiralsmp reload`                            | Reload the `config.yml` into memory.      | `spiralsmp.admin`        |
| `/spiralsmp config module <name> <true/false>` | Enable or disable modules in-game.        | `spiralsmp.admin`        |
| `/warn <player> <reason>`                      | Send a massive warning title to a player. | `spiralsmp.warn.admin`   |
| `/mute <player> [minutes]`                     | Mute a player permanently or temporarily. | `spiralsmp.mute.admin`   |
| `/unmute <player>`                             | Remove a player's mute.                   | `spiralsmp.unmute.admin` |
| `/tempban <player> <minutes> [reason]`         | Temporarily ban a player.                 | `spiralsmp.ban.admin`    |
| `/backup`                                      | Force a manual server backup.             | `spiralsmp.admin`        |

---

## Configuration (`config.yml`)

The plugin generates a default `config.yml` on the first run.

```yaml
# Modules (Enable / Disable commands or systems)
modules:
  backups: true
  tpa: true
  home: true
  hub: true
  combat-log: true
  tablist: true
  end-blocker: true
  nether-blocker: true
  rtp: true

# General Settings
server-name: "Spiral SMP"

# Command Settings
command-cooldowns:
  home: 5
  hub: 5
  rtp: 120

command-warmups:
  home: 5
  hub: 5
  rtp: 5

combat-cooldown-seconds: 10

# RTP Settings
rtp:
  min-radius: 500
  max-radius: 5000

# TPA Settings
tpa-expiration-seconds: 120

# Tablist Settings
tablist-update-interval-seconds: 5

# Backup Settings
backup:
  interval-minutes: 240
  max-limit: 4
```
