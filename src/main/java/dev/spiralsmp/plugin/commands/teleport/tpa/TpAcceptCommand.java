package dev.spiralsmp.plugin.commands.teleport.tpa;

import dev.spiralsmp.plugin.commands.base.CommandInfo;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandInfo(
        name = "tpaccept",
        description = "Accept TP request.",
        requiresCombatCheck = true
)
public class TpAcceptCommand extends TpaResponseCommand {
    private final TpaManager tpaManager;

    public TpAcceptCommand(TpaManager tpaManager) {
        super("senderName");
        this.tpaManager = tpaManager;
    }

    @Override
    protected void handleLatest(Player player) {
        tpaManager.acceptLatest(player);
    }

    @Override
    protected void handleSpecific(Player player, String targetName) {
        tpaManager.acceptSpecific(player, targetName);
    }

    @Override
    protected Iterable<String> suggestionNames(UUID playerId) {
        return tpaManager.getIncomingRequesterNames(playerId);
    }
}