package dev.cardssmp.listeners;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import dev.cardssmp.utils.CardAbilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PassiveListener implements Listener {

    private final CardsSMP plugin;
    private final CardAbilities abilities;

    public PassiveListener(CardsSMP plugin) {
        this.plugin = plugin;
        this.abilities = new CardAbilities(plugin);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        CardType heldCard = plugin.getCardManager().getCardType(
                attacker.getInventory().getItemInMainHand());

        if (heldCard == null) return;

        // Freeze passive: every X hits, auto-freeze
        if (heldCard == CardType.FREEZE) {
            int needed = plugin.getConfig().getInt("freeze.freezeHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.FREEZE);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.FREEZE);
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 255, false, false));
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.JUMP_BOOST, 60, -10, false, false));
                attacker.sendMessage("§9✦ §fPassive Freeze triggered!");
            }
        }

        // Dismantle passive: every X hits, disable target's item
        if (heldCard == CardType.DISMANTLE) {
            int needed = plugin.getConfig().getInt("dismantle.dismantleHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.DISMANTLE);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.DISMANTLE);
                int dur = plugin.getConfig().getInt("dismantle.dismantleDisable");
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.MINING_FATIGUE, dur * 20, 10, false, false));
                attacker.sendMessage("§8✦ §fPassive Dismantle triggered on §e" + victim.getName() + "§7!");
            }
        }

        // Ocean passive: every X hits, drain air
        if (heldCard == CardType.OCEAN) {
            int needed = plugin.getConfig().getInt("ocean.oceanHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.OCEAN);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.OCEAN);
                victim.setRemainingAir(0);
                attacker.sendMessage("§1✦ §fPassive Ocean triggered on §e" + victim.getName() + "§7!");
            }
        }

        // Conductor passive: every X hits, lightning strike
        if (heldCard == CardType.CONDUCTOR) {
            int needed = plugin.getConfig().getInt("conductor.conductorHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.CONDUCTOR);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.CONDUCTOR);
                victim.getWorld().strikeLightningEffect(victim.getLocation());
                victim.damage(3.0, attacker);
                attacker.sendMessage("§e✦ §fPassive Conductor triggered on §e" + victim.getName() + "§7!");
            }
        }

        // Blood passive: chance to cause bleeding (wither) on hit
        if (heldCard == CardType.BLOOD) {
            int chance = plugin.getConfig().getInt("blood.bleedingChance");
            if (Math.random() * 100 < chance) {
                victim.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.WITHER, 60, 0, false, false));
            }
        }
    }
}
