package org.strayzii.strayCustomMaces;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.strayzii.strayCustomMaces.Hooks.PlaceholderAPIHook;
import org.strayzii.strayCustomMaces.Hooks.StrayWeaponsHook;
import org.strayzii.strayCustomMaces.Listeners.OnItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class StrayCustomMaces extends JavaPlugin {
    private final Map<Class<?>, Object> components = new HashMap<>();
    private PlaceholderAPIHook papiHook;

    public void sendMessage(CommandSender target, String path, String... replacements) {
        String msg = getConfig().getString("messages." + path);
        if (msg == null || msg.isEmpty()) return;

        msg = msg.replace("$prefix", getConfig().getString("messages.prefix", ""));

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        target.sendMessage(MiniMessage.miniMessage().deserialize(msg));
    }

    public void announce(String path, String... replacements) {
        String msg = getConfig().getString("messages." + path);
        if (msg == null || msg.isEmpty()) return;

        msg = msg.replace("$prefix", getConfig().getString("messages.prefix", ""));

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(msg));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerPermissions();

        Plugin strayWeapons = Bukkit.getPluginManager()
                .getPlugin("StrayWeapons");

        if (strayWeapons != null && strayWeapons.isEnabled())
            registerComponent(StrayWeaponsHook.class,
                new StrayWeaponsHook(this));

        registerComponent(MaceManager.class,
                new MaceManager(this));
        registerComponent(PlaceholderManager.class,
                new PlaceholderManager(this));

        papiHook = new PlaceholderAPIHook(this);
        papiHook.register();

        Bukkit.getPluginManager().registerEvents(
                new OnItemEvent(this), this);

        Commands commands = new Commands(this);
        getCommand("straycustommaces").setExecutor(commands);
        getCommand("straycustommaces").setTabCompleter(commands);
    }

    public void registerPermissions() {
        ConfigurationSection a = getConfig().getConfigurationSection("maces");
        if (a == null) return;

        Set<String> sections = a.getKeys(false);

        for (String section : sections) {
            if (section.equalsIgnoreCase("none")) {
                getLogger().warning("Название скина 'none' зарезервировано, пропускаю.");
                continue;
            }
            String permissionName = getConfig().getString("maces." + section + ".permission");
            if (permissionName == null) continue;

            PluginManager pm = getServer().getPluginManager();

            Permission permission = new Permission(
                    permissionName,
                    "The new mace skin permission.",
                    PermissionDefault.OP
            );

            if (pm.getPermission(permissionName) == null) {
                pm.addPermission(permission);
            }
        }
    }

    public <T> void registerComponent(Class<T> type, T instance) {
        components.put(type, instance);
    }

    public <T> @Nullable T getComponent(Class<T> type) {
        Object component = components.get(type);
        return component == null ? null : type.cast(component);
    }

    @Override
    public void onDisable() {
        papiHook.unregister();
    }
}
