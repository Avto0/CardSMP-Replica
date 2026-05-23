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

        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), type)) {
            long rem = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), type);
            player.sendMessage(ChatColor.RED + "Card on cooldown! " + ChatColor.GRAY + "(" + rem + "s remaining)");
            return;
        }

        activateCard(player, type);

        int cooldown = getCooldown(type);
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), type, cooldown);
    }

    private void activateCard(Player player, CardType type) {
        CardAbilities a = new CardAbilities(plugin);
        switch (type) {
            case FEATHER -> a.activateFeather(player);
            case FREEZE -> a.activateFreeze(player);
            case SHADOWSTEP -> a.activateShadowstep(player);
            case RESURRECTION -> a.activateResurrection(player);
            case GLIDE -> a.activateGlide(player);
            case VINDICATOR -> a.activateVindicator(player);
            case SPIDER -> a.activateSpider(player);
            case VILLAGER -> a.activateVillager(player);
            case DISMANTLE -> a.activateDismantle(player);
            case OCEAN -> a.activateOcean(player);
            case BLOOD -> a.activateBlood(player);
            case CONDUCTOR -> a.activateConductor(player);
            case SPOTLIGHT -> a.activateSpotlight(player);
            case PHASE -> a.activatePhase(player);
            case HOP -> a.activateHop(player);
            case HEALTH -> a.activateHealth(player);
            case ALCHEMIST -> a.activateAlchemist(player);
            case DRAGON -> a.activateDragon(player);
            case NETHERITE -> a.activateNetherite(player);
            case SOLITARE -> a.activateSolitare(player);
        }
    }

    private int getCooldown(CardType type) {
        return switch (type) {
            case FEATHER -> plugin.getConfig().getInt("feather.dashCooldown");
            case FREEZE -> plugin.getConfig().getInt("freeze.freezeCooldown");
            case SHADOWSTEP -> plugin.getConfig().getInt("shadowstep.shadowstepCooldown");
            case RESURRECTION -> plugin.getConfig().getInt("resurrection.resurrectionCooldown");
            case GLIDE -> plugin.getConfig().getInt("glide.glideCooldown");
            case VINDICATOR -> plugin.getConfig().getInt("vindicator.ravangerCooldown");
            case SPIDER -> plugin.getConfig().getInt("spider.spiderCooldown");
            case VILLAGER -> plugin.getConfig().getInt("villager.villagerCooldown");
            case DISMANTLE -> plugin.getConfig().getInt("dismantle.dismantleCooldown");
            case OCEAN -> plugin.getConfig().getInt("ocean.oceanCooldown");
            case BLOOD -> plugin.getConfig().getInt("blood.bloodrushCooldown");
            case CONDUCTOR -> plugin.getConfig().getInt("conductor.conductorCooldown");
            case SPOTLIGHT -> plugin.getConfig().getInt("spotlight.spotlightCooldown");
            case PHASE -> plugin.getConfig().getInt("phase.phasingCooldown");
            case HOP -> plugin.getConfig().getInt("hop.hopCooldown");
            case HEALTH -> plugin.getConfig().getInt("health.healthCooldown");
            case ALCHEMIST -> plugin.getConfig().getInt("alchemist.alchemistCooldown");
            case DRAGON -> plugin.getConfig().getInt("dragon.dragonCooldown");
            case NETHERITE -> plugin.getConfig().getInt("netherite.netheriteCooldown");
            case SOLITARE -> plugin.getConfig().getInt("solitare.solitareCooldown");
        };
    }
}
