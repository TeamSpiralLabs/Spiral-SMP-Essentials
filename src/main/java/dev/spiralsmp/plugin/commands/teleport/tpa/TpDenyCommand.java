package dev.spiralsmp.plugin.commands.teleport.tpa;

import dev.spiralsmp.plugin.commands.base.CommandInfo;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandInfo(
        name = "tpdeny",
        description = "Deny TP request."
)
public class TpDenyCommand extends TpaResponseCommand {
    private final TpaManager tpaManager;

    public TpDenyCommand(TpaManager tpaManager) {
        super("senderName");
        this.tpaManager = tpaManager;
    }

    @Override
    protected void handleLatest(Player player) {
        tpaManager.denyLatest(player);
    }

    @Override
    protected void handleSpecific(Player player, String targetName) {
        tpaManager.denySpecific(player, targetName);
    }

    @Override
    protected Iterable<String> suggestionNames(UUID playerId) {
        return tpaManager.getIncomingRequesterNames(playerId);
    }
}