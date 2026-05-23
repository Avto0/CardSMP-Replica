package dev.cardssmp.utils;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class CardAbilities {

    private final CardsSMP plugin;

    public CardAbilities(CardsSMP plugin) {
        this.plugin = plugin;
    }

    private int getCfgInt(CardType type, String key, int def) {
        return plugin.getConfig().getInt("cards." + type.getConfigKey() + "." + key, def);
    }

    private double getCfgDouble(CardType type, String key, double def) {
        return plugin.getConfig().getDouble("cards." + type.getConfigKey() + "." + key, def);
    }

    // ─── FEATHER ─────────────────────────────────────────────────
    public void activateFeather(Player player) {
        double power = getCfgDouble(CardType.FEATHER, "launch-power", 2.5);
        int glideDuration = getCfgInt(CardType.FEATHER, "glide-duration", 5);

        Vector vel = player.getLocation().getDirection().clone();
        vel.setY(Math.abs(vel.getY()) + 1.0);
        vel.multiply(power);
        player.setVelocity(vel);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                glideDuration * 20, 0, false, false));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 20, 0.3, 0.3, 0.3, 0.05);
        msg(player, "&b✦ &fFeather &7— You soar into the sky!");
    }

    // ─── FREEZE ──────────────────────────────────────────────────
    public void activateFreeze(Player player) {
        double radius = getCfgDouble(CardType.FREEZE, "radius", 5.0);
        int duration = getCfgInt(CardType.FREEZE, "duration", 4);

        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        int count = 0;
        for (Entity e : nearby) {
            if (e instanceof Player target && target != player) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                        duration * 20, 10, false, false));
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,
                        duration * 20, 128, false, false)); // prevents jumping
                target.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        target.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.01);
                count++;
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_PLACE, 1f, 0.8f);
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 80, radius / 2, 1, radius / 2, 0.05);
        msg(player, "&b✦ &fFreeze &7— Froze &e" + count + "&7 player(s)!");
    }

    // ─── SHADOWSTEP ──────────────────────────────────────────────
    public void activateShadowstep(Player player) {
        double range = getCfgDouble(CardType.SHADOWSTEP, "teleport-range", 20.0);

        Player nearest = null;
        double nearestDist = range;

        for (Entity e : player.getNearbyEntities(range, range, range)) {
            if (e instanceof Player target && target != player) {
                double dist = player.getLocation().distance(target.getLocation());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = target;
                }
            }
        }

        if (nearest == null) {
            msg(player, "&c✦ &fShadowstep &7— No players in range!");
            return;
        }

        // Teleport behind target
        Location behindTarget = nearest.getLocation().clone();
        Vector behind = nearest.getLocation().getDirection().multiply(-1.5);
        behindTarget.add(behind);
        behindTarget.setYaw(nearest.getLocation().getYaw() + 180);

        Location from = player.getLocation().clone();
        player.teleport(behindTarget);

        player.getWorld().spawnParticle(Particle.PORTAL, from, 30, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().spawnParticle(Particle.PORTAL, behindTarget, 30, 0.3, 0.5, 0.3, 0.1);
        player.getWorld().playSound(behindTarget, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, false, false));

        msg(player, "&5✦ &fShadowstep &7— Teleported behind &e" + nearest.getName() + "&7!");
    }

    // ─── RESURRECTION ────────────────────────────────────────────
    public void activateResurrection(Player player) {
        double heal = getCfgDouble(CardType.RESURRECTION, "health-restore", 10.0);
        double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double newHp = Math.min(player.getHealth() + heal, maxHp);
        player.setHealth(newHp);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0),
                60, 0.5, 0.8, 0.5, 0.2);
        msg(player, "&6✦ &fResurrection &7— Restored &c" + heal + "❤ &7health!");
    }

    // ─── VINDICATOR ──────────────────────────────────────────────
    public void activateVindicator(Player player) {
        int rageDuration = getCfgInt(CardType.VINDICATOR, "rage-duration", 6);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, rageDuration * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, rageDuration * 20, 1, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, rageDuration * 20, 0, false, false));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 1f);
        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 1, 0),
                20, 0.5, 0.5, 0.5, 0.1);
        msg(player, "&c✦ &fVindicator &7— Rage activated for &e" + rageDuration + "s&7!");
    }

    // ─── DRAGON ──────────────────────────────────────────────────
    public void activateDragon(Player player) {
        int count = 3;
        double power = getCfgDouble(CardType.DRAGON, "fireball-power", 2.0);

        for (int i = 0; i < count; i++) {
            final int idx = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    Vector dir = player.getLocation().getDirection().clone();
                    // slight spread
                    double spread = 0.15;
                    dir.add(new Vector(
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread
                    )).normalize().multiply(power);

                    DragonFireball fireball = player.getWorld().spawn(
                            player.getEyeLocation().add(dir.clone().normalize()),
                            DragonFireball.class
                    );
                    fireball.setShooter(player);
                    fireball.setVelocity(dir);
                }
            }.runTaskLater(plugin, idx * 4L);
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1f);
        msg(player, "&4✦ &fDragon &7— Unleashed dragon fire!");
    }

    // ─── HEART ───────────────────────────────────────────────────
    public void activateHeart(Player player) {
        double healAmount = getCfgDouble(CardType.HEART, "heal-amount", 8.0);
        double aoeRadius = getCfgDouble(CardType.HEART, "aoe-heal-radius", 5.0);

        // Heal self
        double maxHp = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(player.getHealth() + healAmount / 2, maxHp));

        // Heal allies nearby
        int healed = 0;
        for (Entity e : player.getNearbyEntities(aoeRadius, aoeRadius, aoeRadius)) {
            if (e instanceof Player ally && ally != player) {
                double allyMax = ally.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                ally.setHealth(Math.min(ally.getHealth() + healAmount, allyMax));
                ally.getWorld().spawnParticle(Particle.HEART,
                        ally.getLocation().add(0, 2, 0), 8, 0.3, 0.3, 0.3, 0.1);
                healed++;
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0),
                20, 0.8, 0.8, 0.8, 0.15);
        msg(player, "&d✦ &fHeart &7— Healed self and &e" + healed + "&7 allies!");
    }

    // ─── PHANTOM ─────────────────────────────────────────────────
    public void activatePhantom(Player player) {
        int duration = getCfgInt(CardType.PHANTOM, "invisibility-duration", 6);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1, false, false));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 1f, 1.2f);
        player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation().add(0, 1, 0),
                30, 0.3, 0.5, 0.3, 0.1);
        msg(player, "&8✦ &fPhantom &7— You vanish into shadow!");
    }

    // ─── THUNDER ─────────────────────────────────────────────────
    public void activateThunder(Player player) {
        double radius = 8.0;
        int maxStrikes = getCfgInt(CardType.THUNDER, "lightning-count", 3);

        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        int strikes = 0;

        for (Entity e : nearby) {
            if (e instanceof Player target && target != player && strikes < maxStrikes) {
                player.getWorld().strikeLightning(target.getLocation());
                strikes++;
            }
        }

        if (strikes == 0) {
            player.getWorld().strikeLightning(player.getLocation().add(
                    (Math.random() - 0.5) * 6, 0, (Math.random() - 0.5) * 6));
            msg(player, "&e✦ &fThunder &7— No nearby targets, struck the ground!");
        } else {
            msg(player, "&e✦ &fThunder &7— Struck &e" + strikes + "&7 player(s) with lightning!");
        }

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0.9f);
    }

    // ─── VOID ────────────────────────────────────────────────────
    public void activateVoid(Player player) {
        double radius = getCfgDouble(CardType.VOID, "pull-radius", 8.0);
        double strength = getCfgDouble(CardType.VOID, "pull-strength", 1.5);

        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        int pulled = 0;

        for (Entity e : nearby) {
            if (e instanceof Player target && target != player) {
                Vector dir = player.getLocation().toVector()
                        .subtract(target.getLocation().toVector())
                        .normalize()
                        .multiply(strength);
                dir.setY(0.3);
                target.setVelocity(dir);
                pulled++;
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1f, 0.5f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 80, 2, 1, 2, 0.5);
        msg(player, "&0✦ &fVoid &7— Pulled &e" + pulled + "&7 player(s) toward you!");
    }

    private void msg(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
