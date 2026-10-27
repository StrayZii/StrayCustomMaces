package org.strayzii.strayCustomMaces;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.strayzii.strayCustomMaces.Hooks.StrayWeaponsHook;
import org.strayzii.strayWeapons.StrayWeapons;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MaceManager {
    private final StrayCustomMaces plugin;

    public MaceManager(StrayCustomMaces plugin) {
        this.plugin = plugin;
    }

    public boolean updateMace(ItemStack item, Player p) {
        String skin = skinInUse(p);
        NamespacedKey model = skin.equals("none")
                ? null
                : new NamespacedKey(plugin, skin);

        StrayWeaponsHook hook = plugin.getComponent(StrayWeaponsHook.class);
        if (hook != null && hook.isLegendary(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey current = meta.getItemModel();
        if (model == null && (current == null || !current.getNamespace().equals(plugin.getName().toLowerCase()))) return false;
        if (Objects.equals(current, model)) return false;

        meta.setItemModel(model);
        item.setItemMeta(meta);
        return true;
    }

    public void updateInventory(Player p) {
        boolean changed = false;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || item.getType() != Material.MACE) continue;
            if (updateMace(item, p)) changed = true;
        }

        if (changed) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (p.isOnline()) p.updateInventory();
            });
        }
    }

    public CompletableFuture<Void> giveSkin(Player p, String skin) {
        String permission = plugin.getConfig().getString("maces." + skin + ".permission");
        if (permission == null || permission.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        return LuckPermsProvider.get().getUserManager()
                .modifyUser(p.getUniqueId(), user ->
                        user.data().add(Node.builder(permission).value(true).build()));
    }

    public boolean hasSkin(Player p, String skin) {
        String permission = plugin.getConfig()
                .getString("maces." + skin + ".permission");

        return permission != null
                && !permission.isBlank()
                && p.hasPermission(permission);
    }

    public String skinInUse(Player p) {
        String skin = "none";

        if (plugin.getConfig().contains("players." + p.getName())) {
            String skinName = plugin.getConfig().getString("players." + p.getName(), "none");

            if (skinName != null && !skinName.equalsIgnoreCase("none")) {
                if (plugin.getConfig().contains("maces." + skinName)) {
                    String permission = plugin.getConfig().getString("maces." + skinName + ".permission");
                    if (permission != null) {
                        skin = p.hasPermission(permission) ? skinName : "none";
                    }
                }
            }
        }

        return skin;
    }
}
