package org.strayzii.strayCustomMaces;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Commands implements TabExecutor {
    private final StrayCustomMaces plugin;

    public Commands(StrayCustomMaces plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 0) {
            plugin.sendMessage(commandSender, "usage");
            return true;
        }

        String subCommand = strings[0];

        MaceManager manager = plugin.getComponent(MaceManager.class);
        if (manager == null) return true;

        switch (subCommand.toLowerCase()) {
            case "reload":
                if (!commandSender.hasPermission("straycustommaces.command.reload")) {
                    plugin.sendMessage(commandSender, "no-permission");
                    return true;
                }
                plugin.reloadConfig();
                plugin.registerPermissions();
                plugin.getComponent(PlaceholderManager.class).reload();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    manager.updateInventory(player);
                }

                plugin.sendMessage(commandSender, "reload");
                break;
            case "set":
                if (!commandSender.hasPermission("straycustommaces.command.set")) {
                    plugin.sendMessage(commandSender, "no-permission");
                    return true;
                }

                if (strings.length == 2) {
                    if (!(commandSender instanceof Player player)) {
                        plugin.sendMessage(commandSender, "no-console");
                        return true;
                    }

                    String skin = strings[1].toLowerCase(java.util.Locale.ROOT);

                    if (skin.equals("none")) {
                        plugin.getConfig().set("players." + player.getName(), null);
                        plugin.saveConfig();
                        manager.updateInventory(player);
                        plugin.sendMessage(player, "reset");
                        return true;
                    }

                    if (!plugin.getConfig().contains("maces." + skin + ".permission")) {
                        plugin.sendMessage(commandSender, "wrong-skin-name");
                        return true;
                    }
                    if (!manager.hasSkin(player, skin)) {
                        plugin.sendMessage(commandSender, "doesnt-have-skin");
                        return true;
                    }

                    plugin.getConfig().set("players." + player.getName(), skin);
                    plugin.saveConfig();
                    manager.updateInventory(player);
                    plugin.sendMessage(player, "set", "$skin", skin);

                } else if (strings.length == 3) {
                    if (!commandSender.hasPermission("straycustommaces.command.set.others")) {
                        plugin.sendMessage(commandSender, "no-permission");
                        return true;
                    }

                    String skin = strings[1].toLowerCase(java.util.Locale.ROOT);
                    String playerName = strings[2];

                    Player target = Bukkit.getPlayerExact(playerName);
                    if (target == null) {
                        plugin.sendMessage(commandSender, "player-not-online", "$player", playerName);
                        return true;
                    }

                    if (skin.equals("none")) {
                        plugin.getConfig().set("players." + target.getName(), null);
                        plugin.saveConfig();
                        manager.updateInventory(target);
                        plugin.sendMessage(commandSender, "reset-others", "$player", target.getName());
                        return true;
                    }

                    if (!plugin.getConfig().contains("maces." + skin + ".permission")) {
                        plugin.sendMessage(commandSender, "wrong-skin-name");
                        return true;
                    }

                    plugin.getConfig().set("players." + target.getName(), skin);
                    plugin.saveConfig();

                    if (manager.hasSkin(target, skin)) {
                        manager.updateInventory(target);
                    } else {
                        manager.giveSkin(target, skin)
                                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                                    if (target.isOnline()) manager.updateInventory(target);
                                }))
                                .exceptionally(ex -> {
                                    plugin.getLogger().warning("Не удалось выдать скин " + skin + ": " + ex.getMessage());
                                    return null;
                                });
                    }

                    plugin.sendMessage(commandSender, "set-others", "$skin", skin, "$player", target.getName());

                } else {
                    plugin.sendMessage(commandSender, "usage");
                }
                break;
            default:
                plugin.sendMessage(commandSender, "usage");
                return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String @NotNull [] args
    ) {
        MaceManager manager = plugin.getComponent(MaceManager.class);
        if (manager == null) return List.of();

        if (args.length == 1) {
            List<String> commands = new java.util.ArrayList<>();

            if (sender.hasPermission("straycustommaces.command.reload")) {
                commands.add("reload");
            }

            if (sender.hasPermission("straycustommaces.command.set")) {
                commands.add("set");
            }

            return filterAndSort(commands, args[0]);
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("straycustommaces.command.set")) {
                return List.of();
            }

            // /<command> set <skin>
            if (args.length == 2) {
                if (!(sender instanceof Player player)) {
                    return List.of();
                }

                return getAvailableSkins(manager, player, args[1]);
            }

            // /<command> set <skin> <player>
            if (args.length == 3) {
                if (!sender.hasPermission("straycustommaces.command.set.others")) {
                    return List.of();
                }

                String skin = args[1].toLowerCase(java.util.Locale.ROOT);
                boolean reset = skin.equals("none");

                if (!reset && !plugin.getConfig().contains("maces." + skin + ".permission")) {
                    return List.of();
                }

                String prefix = args[2].toLowerCase(java.util.Locale.ROOT);
                return Bukkit.getOnlinePlayers().stream()
                        .filter(player -> reset || !manager.hasSkin(player, skin))
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(prefix))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }
        }

        return List.of();
    }

    private List<String> getAvailableSkins(MaceManager manager, Player player, String prefix) {
        var section = plugin.getConfig().getConfigurationSection("maces");
        if (section == null) return List.of();

        String p = prefix.toLowerCase(java.util.Locale.ROOT);
        List<String> result = new java.util.ArrayList<>(section.getKeys(false).stream()
                .filter(skin -> manager.hasSkin(player, skin))
                .toList());
        result.add("none");

        return result.stream()
                .filter(s -> s.toLowerCase(java.util.Locale.ROOT).startsWith(p))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> filterAndSort(
            List<String> values,
            String input
    ) {
        String prefix = input.toLowerCase(java.util.Locale.ROOT);

        return values.stream()
                .filter(value -> value.toLowerCase(java.util.Locale.ROOT)
                        .startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
