package dev.spiralsmp.plugin.commands.teleport.tpa;

import dev.spiralsmp.plugin.commands.base.CommandInfo;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandInfo(
        name = "tpcancel",
        description = "Cancels TP request."
)
public class TpCancelCommand extends TpaResponseCommand {
    private final TpaManager tpaManager;

    public TpCancelCommand(TpaManager tpaManager) {
        super("receiverName");
        this.tpaManager = tpaManager;
    }

    @Override
    protected void handleLatest(Player player) {
        tpaManager.cancelLatest(player);
    }

    @Override
    protected void handleSpecific(Player player, String targetName) {
        tpaManager.cancelSpecific(player, targetName);
    }

    @Override
    protected Iterable<String> suggestionNames(UUID playerId) {
        return tpaManager.getOutgoingRequesteeNames(playerId);
    }
}