package org.strayzii.strayCustomMaces.Hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.strayzii.strayCustomMaces.PlaceholderManager;
import org.strayzii.strayCustomMaces.StrayCustomMaces;

import javax.annotation.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final PlaceholderManager manager;

    public PlaceholderAPIHook(StrayCustomMaces plugin) {
        this.manager = plugin.getComponent(PlaceholderManager.class);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "straycustommaces";
    }

    @Override
    public @NotNull String getAuthor() {
        return "StrayZii";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        return manager.process(params, player);
    }
}
