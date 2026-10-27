package org.strayzii.strayCustomMaces.Listeners;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.strayzii.strayCustomMaces.MaceManager;
import org.strayzii.strayCustomMaces.StrayCustomMaces;

public class OnItemEvent implements Listener {
    private final StrayCustomMaces plugin;

    public OnItemEvent(StrayCustomMaces plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSlotChange(PlayerInventorySlotChangeEvent e) {
        if (e.getNewItemStack().getType() != Material.MACE && e.getOldItemStack().getType() != Material.MACE) return;

        plugin.getComponent(MaceManager.class).updateInventory(e.getPlayer());
    }
}
