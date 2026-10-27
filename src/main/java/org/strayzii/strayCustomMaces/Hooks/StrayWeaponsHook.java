package org.strayzii.strayCustomMaces.Hooks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.strayzii.strayCustomMaces.StrayCustomMaces;
import org.strayzii.strayWeapons.StrayWeapons;

import javax.annotation.Nullable;

public class StrayWeaponsHook {
    private final StrayCustomMaces plugin;
    public @Nullable StrayWeapons weapons;

    public StrayWeaponsHook(StrayCustomMaces plugin) {
        this.plugin = plugin;

        Plugin dependency = Bukkit.getPluginManager().getPlugin("StrayWeapons");
        if (dependency == null || !dependency.isEnabled()) {
            plugin.getLogger().warning("Missing dependency: StrayWeapons!");
            return;
        }
        this.weapons = (StrayWeapons) dependency;
    }

    public boolean isLegendary(ItemStack item) {
        return weapons != null && weapons.getWeaponManager().isWeapon(item) != null;
    }
}
