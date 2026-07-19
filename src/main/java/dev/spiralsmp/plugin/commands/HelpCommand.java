package dev.spiralsmp.plugin.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.registry.CommandEntry;
import dev.spiralsmp.plugin.registry.HelpRegistry;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "help",
        description = "SpiralSMP command list.",
        aliases = {"shelp", "spiralhelp"}
)
public class HelpCommand implements SpiralCommand {
    private static final int COMMANDS_PER_PAGE = 6;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> showHelp(ctx.getSource(), 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(ctx -> showHelp(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "page")
                        ))
                );
    }

    private static int showHelp(CommandSourceStack source, int page) {
        List<CommandEntry> visible = HelpRegistry.getInstance().getAll().stream()
                .filter(entry -> entry.permission().isEmpty() || source.getSender().hasPermission(entry.permission()))
                .sorted(Comparator.comparing(CommandEntry::isAlias)
                        .thenComparing(CommandEntry::command)).toList();

        int totalPages = Math.max(1, (int) Math.ceil((double) visible.size() / COMMANDS_PER_PAGE));

        if (page > totalPages) {
            source.getSender().sendMessage(
                    Component.text("Page " + page + " does not exist. There are only " + totalPages + " page(s).").color(NamedTextColor.RED)
            );
            if (source.getSender() instanceof Player player) {
                SoundUtil.ERROR.play(player);
            }
            return 0;
        }

        int from = (page - 1) * COMMANDS_PER_PAGE;
        int to = Math.min(from + COMMANDS_PER_PAGE, visible.size());
        // header
        source.getSender().sendMessage(
                Component.text("━━━ ").color(NamedTextColor.DARK_GRAY)
                        .append(Component.text("Spiral SMP").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(" (Page " + page + "/" + totalPages + ")").color(NamedTextColor.GRAY))
                        .append(Component.text(" ━━━ ").color(NamedTextColor.DARK_GRAY))
        );
        // content
        for (CommandEntry entry : visible.subList(from, to)) {
            source.getSender().sendMessage(
                    Component.text(entry.command()).color(NamedTextColor.YELLOW)
                            .append(Component.text(" — ").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(entry.description()).color(NamedTextColor.WHITE))
            );
        }
        // footer
        if (page < totalPages) {
            source.getSender().sendMessage(
                    Component.text("Use /help " + (page + 1) + " for the next page.").color(NamedTextColor.DARK_GRAY)
            );
        }

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}