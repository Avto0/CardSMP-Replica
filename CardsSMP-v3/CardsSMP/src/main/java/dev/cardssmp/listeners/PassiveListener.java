package dev.cardssmp.listeners;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PassiveListener implements Listener {

    private final CardsSMP plugin;

    public PassiveListener(CardsSMP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        CardType heldCard = plugin.getCardManager().getCardType(attacker.getInventory().getItemInMainHand());

        // ── SHADOWSTEP reflect ──
        UUID victimId = victim.getUniqueId();
        Long reflectExpiry = plugin.getReflectingPlayers().get(victimId);
        if (reflectExpiry != null && System.currentTimeMillis() < reflectExpiry) {
            event.setCancelled(true);
            attacker.damage(event.getDamage());
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, false));
            victim.sendMessage("§5§lSHADOWSTEP §8— §7Reflected §e" + (int)(event.getDamage()/2) + "§7 hearts!");
            return;
        }

        if (heldCard == null) return;

        // ── FREEZE passive ──
        if (heldCard == CardType.FREEZE) {
            int needed = plugin.getConfig().getInt("freeze.freezeHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.FREEZE);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.FREEZE);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, false));
                attacker.sendMessage("§9✦ §fFreeze passive triggered!");
            }
        }

        // ── DISMANTLE passive: swap 2 hotbar items ──
        if (heldCard == CardType.DISMANTLE) {
            int needed = plugin.getConfig().getInt("dismantle.dismantleHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.DISMANTLE);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.DISMANTLE);
                swapHotbarItems(victim);
                attacker.sendMessage("§8✦ §fDismantle passive — swapped §e" + victim.getName() + "§7's items!");
            }
        }

        // ── OCEAN passive: every X hits, drain air ──
        if (heldCard == CardType.OCEAN) {
            int needed = plugin.getConfig().getInt("ocean.oceanHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.OCEAN);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.OCEAN);
                int drownDur = plugin.getConfig().getInt("ocean.drownDuration");
                victim.setRemainingAir(0);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, drownDur * 20, 1, false, false));
                attacker.sendMessage("§1✦ §fOcean passive triggered on §e" + victim.getName() + "§7!");
            }
        }

        // ── CONDUCTOR passive: every X hits, lightning ──
        if (heldCard == CardType.CONDUCTOR) {
            int needed = plugin.getConfig().getInt("conductor.conductorHits");
            int hits = plugin.getCooldownManager().incrementHits(attacker.getUniqueId(), CardType.CONDUCTOR);
            if (hits >= needed) {
                plugin.getCooldownManager().resetHits(attacker.getUniqueId(), CardType.CONDUCTOR);
                victim.getWorld().strikeLightningEffect(victim.getLocation());
                victim.damage(5.0, attacker); // 2.5 hearts
                attacker.sendMessage("§e✦ §fConductor passive triggered on §e" + victim.getName() + "§7!");
            }
        }

        // ── BLOOD passive: X% chance bleed ──
        if (heldCard == CardType.BLOOD) {
            int chance = plugin.getConfig().getInt("blood.bleedingChance");
            if (Math.random() * 100 < chance) {
                int dur = plugin.getConfig().getInt("blood.bloodrushDuration");
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, dur * 20, 0, false, false));
            }
        }

        // ── SHADOWSTEP passive: X% chance teleport behind + damage ──
        if (heldCard == CardType.SHADOWSTEP) {
            int chance = plugin.getConfig().getInt("shadowstep.shadowstepChance");
            if (Math.random() * 100 < chance) {
                double damage = plugin.getConfig().getDouble("shadowstep.teleportDamage");
                // Teleport behind victim
                org.bukkit.Location behind = victim.getLocation().clone();
                org.bukkit.util.Vector dir = victim.getLocation().getDirection().normalize().multiply(-1.5);
                behind.add(dir);
                attacker.teleport(behind);
                victim.damage(damage * 2, attacker);
                attacker.sendMessage("§5✦ §fShadowstep passive triggered!");
            }
        }

        // ── SOLITARE passive: X% chance blindness + darkness ──
        if (heldCard == CardType.SOLITARE) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 1, false, false));
        }

        // ── PHASE passive: X% chance take no damage + attacker slowness ──
        if (heldCard != CardType.PHASE) {
            CardType victimCard = plugin.getCardManager().getCardType(victim.getInventory().getItemInMainHand());
            if (victimCard == CardType.PHASE) {
                int chance = plugin.getConfig().getInt("phase.phasingChance");
                if (Math.random() * 100 < chance) {
                    event.setCancelled(true);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, false));
                    victim.sendMessage("§d✦ §fPhase passive dodged the attack!");
                }
            }
        }

        // ── HEALTH passive: perm 15 hearts (handled on join/equip) ──
        // ── FEATHER passive: no fall damage (handled in EntityDamageEvent) ──
        // ── ALCHEMIST passive: effects last longer (handled when effects applied) ──
    }

    // Double jump for Hop card
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;

        CardType heldCard = plugin.getCardManager().getCardType(p.getInventory().getItemInMainHand());
        if (heldCard != CardType.HOP) return;

        if (!plugin.getCooldownManager().hasDoubleJumpReady(p.getUniqueId())) return;
        if (plugin.getCooldownManager().isDoubleJumpOnCooldown(p.getUniqueId())) return;

        event.setCancelled(true);
        p.setAllowFlight(false);
        p.setVelocity(new Vector(p.getVelocity().getX(), 1.5, p.getVelocity().getZ()));
        plugin.getCooldownManager().setDoubleJumpCooldown(p.getUniqueId(),
                plugin.getConfig().getInt("hop.doubleJumpCooldown"));
        p.sendMessage("§a✦ §fDouble jump!");
    }

    // Allow double jump when in air with Hop card
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        CardType heldCard = plugin.getCardManager().getCardType(p.getInventory().getItemInMainHand());
        if (heldCard != CardType.HOP) return;
        if (!p.isOnGround() && plugin.getCooldownManager().hasDoubleJumpReady(p.getUniqueId())) {
            p.setAllowFlight(true);
        }
        if (p.isOnGround()) {
            p.setAllowFlight(false);
        }
    }

    private void swapHotbarItems(Player p) {
        int slot1 = (int)(Math.random() * 9);
        int slot2 = (int)(Math.random() * 9);
        while (slot2 == slot1) slot2 = (int)(Math.random() * 9);
        org.bukkit.inventory.ItemStack item1 = p.getInventory().getItem(slot1);
        org.bukkit.inventory.ItemStack item2 = p.getInventory().getItem(slot2);
        p.getInventory().setItem(slot1, item2);
        p.getInventory().setItem(slot2, item1);
    }
}
