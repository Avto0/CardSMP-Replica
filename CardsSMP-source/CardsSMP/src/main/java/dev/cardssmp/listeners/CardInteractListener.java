package dev.cardssmp.listeners;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import dev.cardssmp.utils.CardAbilities;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CardInteractListener implements Listener {

    private final CardsSMP plugin;
    private final CardAbilities abilities;

    public CardInteractListener(CardsSMP plugin) {
        this.plugin = plugin;
        this.abilities = new CardAbilities(plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        CardType type = plugin.getCardManager().getCardType(held);
        if (type == null) return;

        event.setCancelled(true);

        if (!plugin.getCardManager().isCardEnabled(type)) {
            player.sendMessage(format(plugin.getConfig().getString("messages.prefix", "&8[&5Cards&8] &r") +
                    plugin.getConfig().getString("messages.card-disabled", "&cThis card is disabled.")));
            return;
        }

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), type)) {
            long remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), type);
            String msg = plugin.getConfig().getString("messages.on-cooldown", "&cOn cooldown! &7(%time%s remaining)")
                    .replace("%time%", String.valueOf(remaining));
            player.sendMessage(format(plugin.getConfig().getString("messages.prefix", "&8[&5Cards&8] &r") + msg));
            return;
        }

        // Activate the card
        activateCard(player, type);

        // Set cooldown
        int cooldown = plugin.getConfig().getInt("cards." + type.getConfigKey() + ".cooldown", 20);
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), type, cooldown);
    }

    private void activateCard(Player player, CardType type) {
        switch (type) {
            case FEATHER -> abilities.activateFeather(player);
            case FREEZE -> abilities.activateFreeze(player);
            case SHADOWSTEP -> abilities.activateShadowstep(player);
            case RESURRECTION -> abilities.activateResurrection(player);
            case VINDICATOR -> abilities.activateVindicator(player);
            case DRAGON -> abilities.activateDragon(player);
            case HEART -> abilities.activateHeart(player);
            case PHANTOM -> abilities.activatePhantom(player);
            case THUNDER -> abilities.activateThunder(player);
            case VOID -> abilities.activateVoid(player);
        }
    }

    private String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
