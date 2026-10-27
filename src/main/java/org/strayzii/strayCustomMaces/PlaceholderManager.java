package org.strayzii.strayCustomMaces;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderManager {
    private final StrayCustomMaces plugin;
    private volatile Map<String, Placeholder> placeholders = Map.of();

    public PlaceholderManager(StrayCustomMaces plugin) {
        this.plugin = plugin;

        registerPlaceholders();
    }

    public void reload() {
        registerPlaceholders();
    }

    public void registerPlaceholders() {
        var section = plugin.getConfig().getConfigurationSection("maces");
        if (section == null) {
            placeholders = Map.of();
            return;
        }

        Map<String, Placeholder> map = new HashMap<>();
        for (String id : section.getKeys(false)) {
            map.put(id, p -> {
                String permission = plugin.getConfig().getString("maces." + id + ".permission");
                return permission != null && p.hasPermission(permission) ? "yes" : "no";
            });
        }
        placeholders = map;
    }

    public synchronized void registerPlaceholder(String identifier, Placeholder placeholder) {
        Map<String, Placeholder> copy = new HashMap<>(placeholders);
        copy.put(identifier, placeholder);
        placeholders = copy;
    }

    public synchronized void unregisterPlaceholder(String identifier) {
        Map<String, Placeholder> copy = new HashMap<>(placeholders);
        copy.remove(identifier);
        placeholders = copy;
    }

    public @Nullable String process(String id, Player p) {
        Placeholder placeholder = placeholders.get(id);
        if (placeholder != null) {
            return placeholder.run(p);
        }
        return null;
    }
}
