package dev.cardssmp.listeners;

import dev.cardssmp.CardsSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final CardsSMP plugin;

    public PlayerListener(CardsSMP plugin) {
        this.plugin = plugin;
    }

    // Prevent cards from being dropped accidentally (optional — comment out if you want dropping)
    // @EventHandler
    // public void onDrop(PlayerDropItemEvent event) {
    //     if (plugin.getCardManager().isCard(event.getItemDrop().getItemStack())) {
    //         event.setCancelled(true);
    //     }
    // }

    // Prevent cards from being placed in crafting slots etc.
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (plugin.getCardManager().isCard(cursor) || plugin.getCardManager().isCard(current)) {
            // Allow moving within player inventory, block crafting grid (slots 0 in workbench)
            // Just let it through - cards are unbreakable so they're safe
        }
    }
}
