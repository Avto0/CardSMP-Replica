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

public class CardInteractListener implements Listener {

    private final CardsSMP plugin;

    public CardInteractListener(CardsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = event.getPlayer();
        CardType type = plugin.getCardManager().getCardType(p.getInventory().getItemInMainHand());
        if (type == null) return;

        event.setCancelled(true);

        if (plugin.getCooldownManager().isOnCooldown(p.getUniqueId(), type)) {
            long rem = plugin.getCooldownManager().getRemainingSeconds(p.getUniqueId(), type);
            p.sendMessage(ChatColor.RED + "Card on cooldown! " + ChatColor.GRAY + "(" + rem + "s)");
            return;
        }

        // Consume card first
        plugin.getCardManager().consumeCard(p);

        // Activate ability
        CardAbilities abilities = new CardAbilities(plugin);
        switch (type) {
            case FEATHER -> abilities.activateFeather(p);
            case FREEZE -> abilities.activateFreeze(p);
            case SHADOWSTEP -> abilities.activateShadowstep(p);
            case RESURRECTION -> abilities.activateResurrection(p);
            case GLIDE -> abilities.activateGlide(p);
            case VINDICATOR -> abilities.activateVindicator(p);
            case SPIDER -> abilities.activateSpider(p);
            case VILLAGER -> abilities.activateVillager(p);
            case DISMANTLE -> abilities.activateDismantle(p);
            case OCEAN -> abilities.activateOcean(p);
            case BLOOD -> abilities.activateBlood(p);
            case CONDUCTOR -> abilities.activateConductor(p);
            case SPOTLIGHT -> abilities.activateSpotlight(p);
            case PHASE -> abilities.activatePhase(p);
            case HOP -> abilities.activateHop(p);
            case HEALTH -> abilities.activateHealth(p);
            case ALCHEMIST -> abilities.activateAlchemist(p);
            case DRAGON -> abilities.activateDragon(p);
            case NETHERITE -> abilities.activateNetherite(p);
            case SOLITARE -> abilities.activateSolitare(p);
        }

        // Set cooldown
        plugin.getCooldownManager().setCooldown(p.getUniqueId(), type, getCooldown(type));
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
