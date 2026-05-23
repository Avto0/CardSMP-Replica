package dev.cardssmp.utils;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class CardAbilities {

    private final CardsSMP plugin;

    public CardAbilities(CardsSMP plugin) {
        this.plugin = plugin;
    }

    private int cfgInt(String key) {
        return plugin.getConfig().getInt(key);
    }

    private double cfgDouble(String key) {
        return plugin.getConfig().getDouble(key);
    }

    private List<Entity> nearby(Player p, double r) {
        return p.getNearbyEntities(r, r, r);
    }

    // ─── FEATHER ─────────────────────────────────────────────────────────────
    // Dash: launches player forward powerfully
    public void activateFeather(Player p) {
        double power = cfgDouble("feather.dashPower");
        Vector dir = p.getLocation().getDirection().normalize().multiply(power);
        dir.setY(0.4);
        p.setVelocity(dir);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.5f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 30, 0.3, 0.1, 0.3, 0.1);
        msg(p, "§b✦ §fFeather §8— §7Dash!");
    }

    // ─── FREEZE ──────────────────────────────────────────────────────────────
    // Freezes all nearby players (slowness 255 + jump suppress)
    public void activateFreeze(Player p) {
        double size = cfgDouble("freeze.freezeSize");
        int frozen = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 255, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, -10, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 255, false, false));
                t.getWorld().spawnParticle(Particle.SNOWFLAKE, t.getLocation().add(0, 1, 0), 50, 0.4, 0.8, 0.4, 0.01);
                frozen++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_POWDER_SNOW_PLACE, 1f, 0.6f);
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation(), 100, size / 2, 1, size / 2, 0.05);
        msg(p, "§9✦ §fFreeze §8— §7Froze §e" + frozen + "§7 player(s)!");
    }

    // ─── SHADOWSTEP ──────────────────────────────────────────────────────────
    // Teleport behind nearest player and deal damage
    public void activateShadowstep(Player p) {
        double damage = cfgDouble("shadowstep.teleportDamage");
        int duration = cfgInt("shadowstep.shadowstepDuration");

        Player nearest = null;
        double nearestDist = 50.0;
        for (Entity e : nearby(p, 30)) {
            if (e instanceof Player t && t != p) {
                double d = p.getLocation().distance(t.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = t; }
            }
        }
        if (nearest == null) { msg(p, "§c✦ §fShadowstep §8— §7No targets nearby!"); return; }

        Location behind = nearest.getLocation().clone();
        Vector dir = nearest.getLocation().getDirection().normalize().multiply(-1.8);
        behind.add(dir);
        behind.setYaw(nearest.getLocation().getYaw() + 180);

        Location from = p.getLocation().clone();
        p.teleport(behind);
        nearest.damage(damage, p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 2, false, false));

        p.getWorld().spawnParticle(Particle.PORTAL, from, 40, 0.3, 0.5, 0.3, 0.15);
        p.getWorld().spawnParticle(Particle.PORTAL, behind, 40, 0.3, 0.5, 0.3, 0.15);
        p.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
        msg(p, "§5✦ §fShadowstep §8— §7Teleported behind §e" + nearest.getName() + "§7!");
    }

    // ─── RESURRECTION ────────────────────────────────────────────────────────
    // Give nearby players totem-like buffs (regen, absorption, resistance)
    public void activateResurrection(Player p) {
        double size = cfgDouble("resurrection.resurrectionSize");
        int duration = cfgInt("resurrection.resurrectionDuration");
        int buffDuration = cfgInt("resurrection.resurrectionBuffDuration");
        int buffed = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, buffDuration * 20, 1, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, buffDuration * 20, 3, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 1, false, false));
                t.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, t.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.2);
                buffed++;
            }
        }
        // Self
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(p.getHealth() + 10, maxHp));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        msg(p, "§6✦ §fResurrection §8— §7Blessed §e" + buffed + "§7 player(s)!");
    }

    // ─── GLIDE ───────────────────────────────────────────────────────────────
    // Boost forward and give slow falling for glide duration
    public void activateGlide(Player p) {
        double power = cfgDouble("glide.glideBoostPower");
        int glideDuration = cfgInt("glide.glideDuration");
        int boostDuration = cfgInt("glide.glideBoostDuration");

        Vector dir = p.getLocation().getDirection().clone().normalize().multiply(power);
        dir.setY(0.5);
        p.setVelocity(dir);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, glideDuration * 20, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration * 20, 2, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 30, 0.3, 0.1, 0.3, 0.08);
        msg(p, "§3✦ §fGlide §8— §7Soaring!");
    }

    // ─── VINDICATOR ──────────────────────────────────────────────────────────
    // Ravager-like rage: strength 2, speed 2, resistance 1
    public void activateVindicator(Player p) {
        int duration = cfgInt("vindicator.ravangerDuration");
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 0, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 0.9f);
        p.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        msg(p, "§c✦ §fVindicator §8— §7Ravager rage for §e" + duration + "s§7!");
    }

    // ─── SPIDER ──────────────────────────────────────────────────────────────
    // Web all nearby players in place
    public void activateSpider(Player p) {
        double size = cfgDouble("spider.spiderSize");
        int duration = cfgInt("spider.spiderDuration");
        int webbed = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                // Slow them heavily to simulate web
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 10, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration * 20, -10, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration * 20, 1, false, false));
                // Spawn cobweb-like particles
                
                webbed++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1f, 0.7f);
        msg(p, "§2✦ §fSpider §8— §7Webbed §e" + webbed + "§7 player(s)!");
    }

    // ─── VILLAGER ────────────────────────────────────────────────────────────
    // Summon iron golems that target nearby players
    public void activateVillager(Player p) {
        int golemCount = cfgInt("villager.villagerGolemCount");
        int duration = cfgInt("villager.villagerDuration");
        int heroAmp = cfgInt("villager.heroOfVillAmplifier");

        p.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, duration * 20, heroAmp, false, false));

        for (int i = 0; i < golemCount; i++) {
            double angle = (2 * Math.PI / golemCount) * i;
            Location spawnLoc = p.getLocation().clone().add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);
            IronGolem golem = (IronGolem) p.getWorld().spawnEntity(spawnLoc, EntityType.IRON_GOLEM);
            golem.setPlayerCreated(true);

            // Remove golem after duration
            new BukkitRunnable() {
                @Override public void run() {
                    if (golem.isValid()) golem.remove();
                }
            }.runTaskLater(plugin, duration * 20L);
        }

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1f);
        msg(p, "§a✦ §fVillager §8— §7Summoned §e" + golemCount + "§7 iron golem(s)!");
    }

    // ─── DISMANTLE ───────────────────────────────────────────────────────────
    // Give target mining fatigue + weakness (locks item in hand effectively)
    public void activateDismantle(Player p) {
        double size = 10.0;
        int disableDuration = cfgInt("dismantle.dismantleDisable");

        Player nearest = null;
        double nearestDist = size;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                double d = p.getLocation().distance(t.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = t; }
            }
        }
        if (nearest == null) { msg(p, "§c✦ §fDismantle §8— §7No targets nearby!"); return; }

        nearest.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, disableDuration * 20, 10, false, false));
        nearest.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, disableDuration * 20, 5, false, false));
        nearest.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, disableDuration * 20, 3, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 0.8f);
        nearest.getWorld().spawnParticle(Particle.CRIT, nearest.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        msg(p, "§8✦ §fDismantle §8— §7Dismantled §e" + nearest.getName() + "§7's weapon!");
    }

    // ─── OCEAN ───────────────────────────────────────────────────────────────
    // Drown nearby players + deal damage
    public void activateOcean(Player p) {
        double size = cfgDouble("ocean.oceanSize");
        double damage = cfgDouble("ocean.oceanDamage");
        int duration = cfgInt("ocean.oceanDuration");
        int drownDuration = cfgInt("ocean.drownDuration");
        int hit = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.damage(damage, p);
                t.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, -1, 0, false, false)); // ironically removes it
                t.setRemainingAir(0);
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, drownDuration * 20, 2, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration * 20, 0, false, false));
                t.getWorld().spawnParticle(Particle.SPLASH, t.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.5);
                // Remove water breathing after
                new BukkitRunnable() {
                    @Override public void run() {
                        t.removePotionEffect(PotionEffectType.WATER_BREATHING);
                    }
                }.runTaskLater(plugin, drownDuration * 20L);
                hit++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1f, 0.8f);
        p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation(), 80, size / 2, 1, size / 2, 0.3);
        msg(p, "§1✦ §fOcean §8— §7Drowned §e" + hit + "§7 player(s)!");
    }

    // ─── BLOOD ───────────────────────────────────────────────────────────────
    // Rush all nearby enemies and cause bleeding (wither effect)
    public void activateBlood(Player p) {
        double size = cfgDouble("blood.bloodrushSize");
        int duration = cfgInt("blood.bloodrushDuration");
        int hit = 0;

        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 3, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, 1, false, false));

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.damage(3.0, p);
                t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration * 20, 1, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 1, false, false));
                t.getWorld().spawnParticle(Particle.FALLING_DUST,
                        t.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1,
                        Material.REDSTONE_BLOCK.createBlockData());
                hit++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1f, 0.7f);
        msg(p, "§4✦ §fBlood §8— §7Bloodrushed §e" + hit + "§7 player(s)!");
    }

    // ─── CONDUCTOR ───────────────────────────────────────────────────────────
    // Strike all nearby players with lightning
    public void activateConductor(Player p) {
        double size = cfgDouble("conductor.conductorSize");
        int struck = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                p.getWorld().strikeLightning(t.getLocation());
                struck++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0.8f);
        msg(p, "§e✦ §fConductor §8— §7Struck §e" + struck + "§7 player(s) with lightning!");
    }

    // ─── SPOTLIGHT ───────────────────────────────────────────────────────────
    // Reveal all nearby players with glowing
    public void activateSpotlight(Player p) {
        double size = cfgDouble("spotlight.spotlightSize");
        double glowSize = cfgDouble("spotlight.glowingSize");
        int revealed = 0;

        for (Entity e : nearby(p, Math.max(size, glowSize))) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false));
                t.getWorld().spawnParticle(Particle.END_ROD, t.getLocation().add(0, 2, 0), 20, 0.3, 0.5, 0.3, 0.05);
                revealed++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.5f);
        msg(p, "§f✦ §fSpotlight §8— §7Revealed §e" + revealed + "§7 player(s)!");
    }

    // ─── PHASE ───────────────────────────────────────────────────────────────
    // Invisibility + resistance + speed for phasing duration
    public void activatePhase(Player p) {
        int duration = cfgInt("phase.phasingDuration");
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 4, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 2, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.3);
        msg(p, "§d✦ §fPhase §8— §7Phasing for §e" + duration + "s§7!");
    }

    // ─── HOP ─────────────────────────────────────────────────────────────────
    // Launch all nearby players into the air
    public void activateHop(Player p) {
        double size = cfgDouble("hop.hopSize");
        int launched = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.setVelocity(new Vector(0, 2.5, 0));
                t.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, t.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                launched++;
            }
        }
        p.setVelocity(new Vector(0, 2.5, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SLIME_JUMP, 1f, 0.8f);
        msg(p, "§a✦ §fHop §8— §7Launched §e" + launched + "§7 player(s)!");
    }

    // ─── HEALTH ──────────────────────────────────────────────────────────────
    // Heal self and nearby allies
    public void activateHealth(Player p) {
        double size = cfgDouble("health.healthSize");
        int duration = cfgInt("health.healthDuration");
        int healed = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 2, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration * 20, 2, false, false));
                t.getWorld().spawnParticle(Particle.HEART, t.getLocation().add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0.1);
                healed++;
            }
        }
        // Also heal self
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(p.getHealth() + 8, maxHp));

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        msg(p, "§c✦ §fHealth §8— §7Healed §e" + healed + "§7 player(s)!");
    }

    // ─── ALCHEMIST ───────────────────────────────────────────────────────────
    // Extend all active potion effects by plusSeconds
    public void activateAlchemist(Player p) {
        int plusTicks = cfgInt("alchemist.plusSeconds") * 20;
        int duration = cfgInt("alchemist.alchemistDuration");
        int extended = 0;

        Collection<PotionEffect> effects = p.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            p.addPotionEffect(new PotionEffect(
                    effect.getType(),
                    effect.getDuration() + plusTicks,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles()
            ));
            extended++;
        }
        // Give haste + night vision bonus
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration * 20, 0, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
        p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.1);
        msg(p, "§5✦ §fAlchemist §8— §7Extended §e" + extended + "§7 effect(s)!");
    }

    // ─── DRAGON ──────────────────────────────────────────────────────────────
    // Deal massive damage to all nearby players + passive speed/strength
    public void activateDragon(Player p) {
        double size = cfgDouble("dragon.dragonSize");
        double damage = cfgDouble("dragon.dragonDamage");
        int speedAmp = cfgInt("dragon.speedAmplifier");
        int strAmp = cfgInt("dragon.strengthAmplifier");
        int hit = 0;

        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, speedAmp, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, strAmp, false, false));

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                // Fire 3 dragon fireballs at each target
                final Player target = t;
                for (int i = 0; i < 3; i++) {
                    final int idx = i;
                    new BukkitRunnable() {
                        @Override public void run() {
                            if (!p.isOnline() || !target.isOnline()) return;
                            Vector dir = target.getLocation().toVector()
                                    .subtract(p.getEyeLocation().toVector()).normalize().multiply(2.0);
                            DragonFireball fb = p.getWorld().spawn(p.getEyeLocation(), DragonFireball.class);
                            fb.setShooter(p);
                            fb.setVelocity(dir);
                        }
                    }.runTaskLater(plugin, idx * 5L);
                }
                hit++;
            }
        }

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.9f);
        p.getWorld().spawnParticle(Particle.DRAGON_BREATH, p.getLocation().add(0, 1, 0), 60, 1, 1, 1, 0.15);
        msg(p, "§4✦ §fDragon §8— §7Dragon fire on §e" + hit + "§7 target(s)!");
    }

    // ─── NETHERITE ───────────────────────────────────────────────────────────
    // Become an unstoppable tank with resistance 4 + regen + strength
    public void activateNetherite(Player p) {
        int duration = cfgInt("netherite.netheriteDuration");
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 4, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration * 20, 0, false, false));
        p.setHealth(maxHp);

        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 0.8f);
        p.getWorld().spawnParticle(Particle.LAVA, p.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.1);
        msg(p, "§8✦ §fNetherite §8— §7Unstoppable for §e" + duration + "s§7!");
    }

    // ─── SOLITARE ────────────────────────────────────────────────────────────
    // Deal massive single-target damage
    public void activateSolitare(Player p) {
        double size = cfgDouble("solitare.solitareSize");
        double damage = cfgDouble("solitare.solitareDamage");

        Player nearest = null;
        double nearestDist = size;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                double d = p.getLocation().distance(t.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = t; }
            }
        }
        if (nearest == null) { msg(p, "§c✦ §fSolitare §8— §7No targets nearby!"); return; }

        nearest.damage(damage, p);
        nearest.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, false));
        nearest.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2, false, false));

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1f, 1.2f);
        nearest.getWorld().spawnParticle(Particle.CRIT, nearest.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.3);
        msg(p, "§7✦ §fSolitare §8— §7Dealt §e" + damage + "§7 damage to §e" + nearest.getName() + "§7!");
    }

    private void msg(Player p, String m) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
    }
}
