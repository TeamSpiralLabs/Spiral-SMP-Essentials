package dev.spiralsmp.plugin.commands.general;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.spiralsmp.plugin.Main;
import dev.spiralsmp.plugin.commands.base.CommandInfo;
import dev.spiralsmp.plugin.commands.base.SpiralCommand;
import dev.spiralsmp.plugin.registry.CommandEntry;
import dev.spiralsmp.plugin.registry.HelpRegistry;
import dev.spiralsmp.plugin.utils.SoundUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@CommandInfo(
        name = "spiralsmp",
        description = "Core command for Spiral SMP Essentials."
)
public class CoreCommand implements SpiralCommand {
    private static final int COMMANDS_PER_PAGE = 6;
    private final Main plugin;

    public CoreCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(getInfo().name())
                .executes(ctx -> showHelp(ctx.getSource(), 1))

                // info
                .then(Commands.literal("info")
                        .executes(ctx -> showInfo(ctx.getSource()))
                )

                // help
                .then(Commands.literal("help")
                        .executes(ctx -> showHelp(ctx.getSource(), 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> showHelp(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                        )
                )

                // reload
                .then(Commands.literal("reload")
                        .requires(ctx -> ctx.getSender().hasPermission("spiralsmp.admin"))
                        .executes(ctx -> reloadConfig(ctx.getSource()))
                )

                // config
                .then(Commands.literal("config")
                        .requires(ctx -> ctx.getSender().hasPermission("spiralsmp.admin"))
                        .executes(ctx -> listModules(ctx.getSource()))

                        // config list
                        .then(Commands.literal("list")
                                .executes(ctx -> listModules(ctx.getSource()))
                        )

                        // config module <name> <state>
                        .then(Commands.literal("module")
                                .then(Commands.argument("moduleName", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            // Dynamic Tab-Completion logic
                                            String remaining = builder.getRemaining().toLowerCase();
                                            for (String module : getModuleNames()) {
                                                if (module.toLowerCase().startsWith(remaining)) {
                                                    builder.suggest(module);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("state", BoolArgumentType.bool())
                                                .executes(ctx -> toggleModule(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "moduleName"),
                                                        BoolArgumentType.getBool(ctx, "state")
                                                ))
                                        )
                                )
                        )
                );
    }

    private List<String> getModuleNames() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("modules");
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
        }
        return new ArrayList<>();
    }

    private int listModules(CommandSourceStack source) {
        source.getSender().sendMessage(
                Component.text("━━━ ").color(NamedTextColor.DARK_GRAY)
                        .append(Component.text("Spiral SMP Modules").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(" ━━━").color(NamedTextColor.DARK_GRAY))
        );

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("modules");
        if (section == null) {
            source.getSender().sendMessage(Component.text("No modules found in config.yml.").color(NamedTextColor.RED));
            return 0;
        }

        for (String key : section.getKeys(false)) {
            boolean isEnabled = plugin.getConfig().getBoolean("modules." + key);
            source.getSender().sendMessage(
                    Component.text("• ").color(NamedTextColor.DARK_GRAY)
                            .append(Component.text(key).color(NamedTextColor.YELLOW))
                            .append(Component.text(" : ").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(isEnabled ? "Enabled" : "Disabled").color(isEnabled ? NamedTextColor.GREEN : NamedTextColor.RED))
            );
        }

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }

    private int reloadConfig(CommandSourceStack source) {
        plugin.reloadConfig();
        source.getSender().sendMessage(
                Component.text("Spiral SMP configuration reloaded successfully from config.yml!").color(NamedTextColor.GREEN)
        );
        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }

    private int showInfo(CommandSourceStack source) {
        PluginMeta meta = plugin.getPluginMeta();

        source.getSender().sendMessage(
                Component.text("━━━ ").color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(meta.getName()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(" ━━━").color(NamedTextColor.DARK_GRAY))
        );
        source.getSender().sendMessage(
                Component.text("Version: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(meta.getVersion()).color(NamedTextColor.WHITE))
        );

        String desc = meta.getDescription();
        source.getSender().sendMessage(
                Component.text("Description: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(desc != null ? desc : "No description.").color(NamedTextColor.WHITE))
        );

        source.getSender().sendMessage(
                Component.text("Authors: ").color(NamedTextColor.YELLOW)
                        .append(Component.text(String.join(", ", meta.getAuthors())).color(NamedTextColor.WHITE))
        );

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }

    private int showHelp(CommandSourceStack source, int page) {
        List<CommandEntry> visible = HelpRegistry.getInstance().getAll().stream()
                .filter(entry -> entry.permission().isEmpty() || source.getSender().hasPermission(entry.permission()))
                .sorted(Comparator.comparing(CommandEntry::isAlias)
                        .thenComparing(CommandEntry::command)).toList();

        int totalPages = Math.max(1, (int) Math.ceil((double) visible.size() / COMMANDS_PER_PAGE));

        if (page > totalPages) {
            source.getSender().sendMessage(
                    Component.text("Page " + page + " does not exist. There are only " + totalPages + " page(s).").color(NamedTextColor.RED)
            );
            if (source.getSender() instanceof Player player) SoundUtil.ERROR.play(player);
            return 0;
        }

        String serverName = plugin.getConfig().getString("server-name", "Spiral SMP");
        int from = (page - 1) * COMMANDS_PER_PAGE;
        int to = Math.min(from + COMMANDS_PER_PAGE, visible.size());

        source.getSender().sendMessage(
                Component.text("━━━ ").color(NamedTextColor.DARK_GRAY)
                        .append(Component.text(serverName).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(" (Page " + page + "/" + totalPages + ")").color(NamedTextColor.GRAY))
                        .append(Component.text(" ━━━ ").color(NamedTextColor.DARK_GRAY))
        );

        for (CommandEntry entry : visible.subList(from, to)) {
            source.getSender().sendMessage(
                    Component.text(entry.command()).color(NamedTextColor.YELLOW)
                            .append(Component.text(" — ").color(NamedTextColor.DARK_GRAY))
                            .append(Component.text(entry.description()).color(NamedTextColor.WHITE))
            );
        }

        if (page < totalPages) {
            source.getSender().sendMessage(
                    Component.text("Use /spiralsmp help " + (page + 1) + " for the next page.").color(NamedTextColor.DARK_GRAY)
            );
        }

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }

    private int toggleModule(CommandSourceStack source, String moduleName, boolean state) {
        String path = "modules." + moduleName;

        if (!plugin.getConfig().contains(path)) {
            source.getSender().sendMessage(
                    Component.text("Module '" + moduleName + "' does not exist in config.yml!").color(NamedTextColor.RED)
            );
            if (source.getSender() instanceof Player player) SoundUtil.ERROR.play(player);
            return 0;
        }

        plugin.getConfig().set(path, state);
        plugin.saveConfig();

        source.getSender().sendMessage(
                Component.text("Successfully set module '").color(NamedTextColor.GREEN)
                        .append(Component.text(moduleName).color(NamedTextColor.YELLOW))
                        .append(Component.text("' to ").color(NamedTextColor.GREEN))
                        .append(Component.text(state).color(state ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .append(Component.text(". You must restart the server for this to take effect.").color(NamedTextColor.GRAY))
        );

        if (source.getSender() instanceof Player player) SoundUtil.TICK.play(player);
        return 1;
    }
}