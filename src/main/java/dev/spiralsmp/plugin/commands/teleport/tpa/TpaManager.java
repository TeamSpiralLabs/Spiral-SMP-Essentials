package dev.spiralsmp.plugin.commands.teleport.tpa;

import dev.spiralsmp.plugin.managers.CombatManager;
import dev.spiralsmp.plugin.utils.MessageUtil;
import dev.spiralsmp.plugin.utils.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class TpaManager {
    private final Plugin plugin;
    private final Map<UUID, Map<UUID, TpaRequest>> incoming = new HashMap<>();
    private final Map<UUID, Map<UUID, TpaRequest>> outgoing = new HashMap<>();

    public TpaManager(Plugin plugin) {
        this.plugin = plugin;
    }

    // sending request
    public void sendRequest(Player sender, Player receiver) {
        UUID senderId = sender.getUniqueId();
        UUID receiverId = receiver.getUniqueId();

        if (senderId.equals(receiverId)) {
            sendError(sender, "You cannot send a teleport request to yourself");
            return;
        }

        if (hasIncomingFrom(receiverId, senderId)) {
            sendError(sender, "You already have a pending teleport request to " + receiver.getName());
            return;
        }

        TpaRequest request = new TpaRequest(senderId, receiverId, System.currentTimeMillis());
        index(request);

        long expirationSeconds = plugin.getConfig().getLong("tpa-expiration-seconds", 120L);
        long expirationTicks = expirationSeconds * 20L;

        MessageUtil.sendSuccessBar(sender, "Teleport request sent to " + receiver.getName());
        SoundUtil.SUCCESS.play(sender);

        receiver.sendMessage(buildRequestPrompt(sender, expirationSeconds));
        SoundUtil.TICK.play(receiver);

        request.setExpirationTask(plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> expireRequest(request), expirationTicks));
    }

    private Component buildRequestPrompt(Player sender, long expirationSeconds) {
        return Component.text(sender.getName(), NamedTextColor.AQUA)
                .append(Component.text(" wants to teleport to you. ", NamedTextColor.WHITE))
                .append(Component.text("(Expires in " + expirationSeconds + "s)", NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Type ", NamedTextColor.GRAY))
                .append(Component.text("/tpaccept", NamedTextColor.GREEN))
                .append(Component.text(" to accept or ", NamedTextColor.GRAY))
                .append(Component.text("/tpdeny", NamedTextColor.RED))
                .append(Component.text(" to deny.", NamedTextColor.GRAY));
    }

    private void expireRequest(TpaRequest request) {
        if (!removeIfStillActive(request)) return;

        Player sender = plugin.getServer().getPlayer(request.sender());
        Player receiver = plugin.getServer().getPlayer(request.receiver());

        if (sender != null) {
            String receiverName = receiver != null ? receiver.getName() : "them";
            MessageUtil.sendErrorBar(sender, "Your teleport request to " + receiverName + " has expired");
            SoundUtil.CANCELLED.play(sender);
        }
        if (receiver != null) {
            String senderName = sender != null ? sender.getName() : "They";
            MessageUtil.sendActionBar(receiver, Component.text("The teleport request from " + senderName + " has expired").color(NamedTextColor.GRAY));
        }
    }

    // accept request
    public void acceptLatest(Player receiver) {
        withLatestIncoming(receiver,
                request -> acceptRequest(request, receiver),
                () -> sendError(receiver, "You have no pending teleport requests"));
    }

    public void acceptSpecific(Player receiver, String senderName) {
        withIncoming(receiver, senderName, request -> acceptRequest(request, receiver));
    }

    private void acceptRequest(TpaRequest request, Player receiver) {
        Player sender = plugin.getServer().getPlayer(request.sender());
        removeRequest(request);

        if (sender == null) {
            sendError(receiver, "The player who sent this request is no longer online");
            return;
        }

        CombatManager combatManager = CombatManager.getInstance();
        if (combatManager != null && combatManager.isInCombat(sender)) {
            sendError(receiver, sender.getName() + " is currently in combat and cannot teleport right now");
            sendError(sender, "You cannot teleport while in combat");
            return;
        }

        sender.teleportAsync(receiver.getLocation()).thenAccept(success -> {
            if (success) {
                MessageUtil.sendSuccessBar(sender, "Teleported to " + receiver.getName());
                SoundUtil.SUCCESS.play(sender);

                MessageUtil.sendSuccessBar(receiver, sender.getName() + " has teleported to you");
                SoundUtil.SUCCESS.play(receiver);
            } else {
                sendError(sender, "Teleportation failed");
            }
        });
    }

    // deny request
    public void denyLatest(Player receiver) {
        withLatestIncoming(receiver,
                request -> denyRequest(request, receiver),
                () -> sendError(receiver, "You have no pending teleport requests"));
    }

    public void denySpecific(Player receiver, String senderName) {
        withIncoming(receiver, senderName, request -> denyRequest(request, receiver));
    }

    private void denyRequest(TpaRequest request, Player receiver) {
        Player sender = plugin.getServer().getPlayer(request.sender());
        removeRequest(request);

        MessageUtil.sendActionBar(receiver, Component.text("You denied the teleport request from " + nameOf(sender, "that player") + "").color(NamedTextColor.YELLOW));
        SoundUtil.CANCELLED.play(receiver);

        if (sender != null) {
            MessageUtil.sendErrorBar(sender, receiver.getName() + " denied your teleport request");
            SoundUtil.ERROR.play(sender);
        }
    }

    // cancel request
    public void cancelLatest(Player sender) {
        withLatestOutgoing(sender,
                request -> cancelRequest(request, sender),
                () -> sendError(sender, "You have no pending teleport requests to cancel"));
    }

    public void cancelSpecific(Player sender, String receiverName) {
        withOutgoing(sender, receiverName, request -> cancelRequest(request, sender));
    }

    private void cancelRequest(TpaRequest request, Player sender) {
        Player receiver = plugin.getServer().getPlayer(request.receiver());
        removeRequest(request);

        MessageUtil.sendActionBar(sender, Component.text("You cancelled your teleport request to " + nameOf(receiver, "that player")).color(NamedTextColor.YELLOW));
        SoundUtil.CANCELLED.play(sender);

        if (receiver != null) {
            MessageUtil.sendActionBar(receiver, Component.text(sender.getName() + " cancelled their teleport request to you").color(NamedTextColor.GRAY));
        }
    }

    // tab completion lookups
    public Iterable<String> getIncomingRequesterNames(UUID receiverId) {
        return onlineNamesOf(incoming.get(receiverId));
    }

    public Iterable<String> getOutgoingRequesteeNames(UUID senderId) {
        return onlineNamesOf(outgoing.get(senderId));
    }

    private Iterable<String> onlineNamesOf(Map<UUID, TpaRequest> pending) {
        if (pending == null || pending.isEmpty()) return List.of();
        return pending.keySet().stream()
                .map(plugin.getServer()::getPlayer)
                .filter(Objects::nonNull)
                .map(Player::getName)
                .toList();
    }

    // cleanup if disconnected
    public void cleanupOnQuit(Player quitter) {
        UUID quitId = quitter.getUniqueId();

        Map<UUID, TpaRequest> theirIncoming = incoming.remove(quitId);
        if (theirIncoming != null) {
            for (TpaRequest request : theirIncoming.values()) {
                request.cancelExpiration();
                unindexOutgoing(request);
                Player sender = plugin.getServer().getPlayer(request.sender());
                if (sender != null) {
                    MessageUtil.sendErrorBar(sender, quitter.getName() + " left the game. Your teleport request to them was cancelled");
                    SoundUtil.CANCELLED.play(sender);
                }
            }
        }

        Map<UUID, TpaRequest> theirOutgoing = outgoing.remove(quitId);
        if (theirOutgoing != null) {
            for (TpaRequest request : theirOutgoing.values()) {
                request.cancelExpiration();
                unindexIncoming(request);
                Player target = plugin.getServer().getPlayer(request.receiver());
                if (target != null) {
                    MessageUtil.sendActionBar(target, Component.text("The teleport request from " + quitter.getName() + " was cancelled because they left the game").color(NamedTextColor.GRAY));
                }
            }
        }
    }

    private boolean hasIncomingFrom(UUID receiverId, UUID senderId) {
        Map<UUID, TpaRequest> pending = incoming.get(receiverId);
        return pending != null && pending.containsKey(senderId);
    }

    private void index(TpaRequest request) {
        incoming.computeIfAbsent(request.receiver(), id -> new HashMap<>()).put(request.sender(), request);
        outgoing.computeIfAbsent(request.sender(), id -> new HashMap<>()).put(request.receiver(), request);
    }

    private void removeRequest(TpaRequest request) {
        request.cancelExpiration();
        unindexIncoming(request);
        unindexOutgoing(request);
    }

    private boolean removeIfStillActive(TpaRequest request) {
        Map<UUID, TpaRequest> pending = incoming.get(request.receiver());
        if (pending == null || pending.get(request.sender()) != request) return false;
        removeRequest(request);
        return true;
    }

    private void unindexIncoming(TpaRequest request) {
        Map<UUID, TpaRequest> pending = incoming.get(request.receiver());
        if (pending != null) {
            pending.remove(request.sender());
            if (pending.isEmpty()) incoming.remove(request.receiver());
        }
    }

    private void unindexOutgoing(TpaRequest request) {
        Map<UUID, TpaRequest> pending = outgoing.get(request.sender());
        if (pending != null) {
            pending.remove(request.receiver());
            if (pending.isEmpty()) outgoing.remove(request.sender());
        }
    }

    private void withIncoming(Player receiver, String senderName, Consumer<TpaRequest> action) {
        Player sender = plugin.getServer().getPlayer(senderName);
        if (sender == null) {
            sendError(receiver, "That player is no longer online");
            return;
        }
        Map<UUID, TpaRequest> pending = incoming.get(receiver.getUniqueId());
        TpaRequest request = pending != null ? pending.get(sender.getUniqueId()) : null;
        if (request == null) {
            sendError(receiver, "You don't have an active request from " + senderName);
            return;
        }
        action.accept(request);
    }

    private void withOutgoing(Player sender, String receiverName, Consumer<TpaRequest> action) {
        Player receiver = plugin.getServer().getPlayer(receiverName);
        if (receiver == null) {
            sendError(sender, "That player is no longer online");
            return;
        }
        Map<UUID, TpaRequest> pending = outgoing.get(sender.getUniqueId());
        TpaRequest request = pending != null ? pending.get(receiver.getUniqueId()) : null;
        if (request == null) {
            sendError(sender, "You do not have a pending request sent to " + receiverName);
            return;
        }
        action.accept(request);
    }

    private void withLatestIncoming(Player receiver, Consumer<TpaRequest> action, Runnable ifEmpty) {
        Map<UUID, TpaRequest> pending = incoming.get(receiver.getUniqueId());
        if (pending == null || pending.isEmpty()) {
            ifEmpty.run();
            return;
        }
        pending.values().stream().max(Comparator.comparingLong(TpaRequest::timestamp)).ifPresent(action);
    }

    private void withLatestOutgoing(Player sender, Consumer<TpaRequest> action, Runnable ifEmpty) {
        Map<UUID, TpaRequest> pending = outgoing.get(sender.getUniqueId());
        if (pending == null || pending.isEmpty()) {
            ifEmpty.run();
            return;
        }
        pending.values().stream().max(Comparator.comparingLong(TpaRequest::timestamp)).ifPresent(action);
    }

    private static String nameOf(Player player, String fallback) {
        return player != null ? player.getName() : fallback;
    }

    private void sendError(Player player, String message) {
        MessageUtil.sendErrorBar(player, message);
        SoundUtil.ERROR.play(player);
    }
}