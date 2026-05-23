package dev.cardssmp.utils;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class CardAbilities {

    private final CardsSMP plugin;

    public CardAbilities(CardsSMP plugin) {
        this.plugin = plugin;
    }

    private int cfgInt(String key) { return plugin.getConfig().getInt(key); }
    private double cfgDouble(String key) { return plugin.getConfig().getDouble(key); }
    private List<Entity> nearby(Player p, double r) { return p.getNearbyEntities(r, r, r); }

    // ─── FEATHER ─────────────────────────────────────────────────────────────
    // Ability: Fast dash forward
    // Passive: No fall damage, falling from high = mace-like hit
    public void activateFeather(Player p) {
        Vector dir = p.getLocation().getDirection().normalize().multiply(3.0);
        dir.setY(0.3);
        p.setVelocity(dir);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.8f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 25, 0.3, 0.1, 0.3, 0.12);
        msg(p, "§b§lFEATHER §8— §7Dashing!");
    }

    // ─── FREEZE ──────────────────────────────────────────────────────────────
    // Ability: Spawn rings giving slowness 4 to all nearby untrusted players
    // Passive: Every X hits give slowness to enemy
    public void activateFreeze(Player p) {
        double size = cfgDouble("freeze.freezeSize");
        int frozen = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, -10, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 255, false, false));
                t.getWorld().spawnParticle(Particle.SNOWFLAKE, t.getLocation().add(0, 1, 0), 60, 0.4, 0.8, 0.4, 0.01);
                frozen++;
            }
        }
        // Spawn ring particles
        spawnRing(p.getLocation(), size, Particle.SNOWFLAKE);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_POWDER_SNOW_PLACE, 1f, 0.6f);
        msg(p, "§9§lFREEZE §8— §7Froze §e" + frozen + "§7 player(s)!");
    }

    // ─── SHADOWSTEP ──────────────────────────────────────────────────────────
    // Ability: All attacks do no damage and reflect onto attacker for X seconds (glow black)
    // Passive: When hitting a player, X% chance to teleport around them and deal X hearts
    public void activateShadowstep(Player p) {
        int duration = cfgInt("shadowstep.shadowstepDuration");
        // Mark player as reflecting damage
        plugin.getReflectingPlayers().put(p.getUniqueId(),
                System.currentTimeMillis() + duration * 1000L);
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration * 20, 4, false, false));
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.3);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
        msg(p, "§5§lSHADOWSTEP §8— §7Reflecting all damage for §e" + duration + "s§7!");
    }

    // ─── RESURRECTION ────────────────────────────────────────────────────────
    // Gives nearby allies regen, absorption, resistance
    public void activateResurrection(Player p) {
        double size = cfgDouble("resurrection.resurrectionSize");
        int buffDur = cfgInt("resurrection.resurrectionBuffDuration");
        int buffed = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, buffDur * 20, 1, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, buffDur * 20, 3, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, buffDur * 20, 1, false, false));
                t.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, t.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0.2);
                buffed++;
            }
        }
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        p.setHealth(Math.min(p.getHealth() + 10, maxHp));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        msg(p, "§6§lRESURRECTION §8— §7Blessed §e" + buffed + "§7 ally(s)!");
    }

    // ─── GLIDE ───────────────────────────────────────────────────────────────
    // Ability: Sends player forward + firework rocket boost + elytra flight
    // Passive: When falling X blocks, get elytra flight for X seconds
    public void activateGlide(Player p) {
        double power = cfgDouble("glide.glideBoostPower");
        int duration = cfgInt("glide.glideDuration");
        Vector dir = p.getLocation().getDirection().normalize().multiply(power);
        dir.setY(0.6);
        p.setVelocity(dir);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, duration * 20, 0, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 3, false, false));
        // Simulate rocket boost with repeated velocity
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!p.isOnline() || ticks >= 10) { cancel(); return; }
                Vector boost = p.getLocation().getDirection().normalize().multiply(0.8);
                boost.setY(0.1);
                p.setVelocity(p.getVelocity().add(boost));
                ticks++;
            }
        }.runTaskTimer(plugin, 2L, 2L);
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, 1f, 1.3f);
        p.getWorld().spawnParticle(Particle.FIREWORK, p.getLocation(), 40, 0.5, 0.2, 0.5, 0.2);
        msg(p, "§3§lGLIDE §8— §7Rocket boost!");
    }

    // ─── VINDICATOR ──────────────────────────────────────────────────────────
    // Ravager rage: strength 3, speed 2, resistance 1
    public void activateVindicator(Player p) {
        int dur = cfgInt("vindicator.ravangerDuration");
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur * 20, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur * 20, 1, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 0.9f);
        p.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        msg(p, "§c§lVINDICATOR §8— §7Ravager rage for §e" + dur + "s§7!");
    }

    // ─── SPIDER ──────────────────────────────────────────────────────────────
    // Ability: Spawn cobwebs under enemies, gives darkness + blindness, can't be broken for X seconds
    // Passive: Movement in cobwebs 50% faster
    public void activateSpider(Player p) {
        double size = cfgDouble("spider.spiderSize");
        int dur = cfgInt("spider.spiderDuration");
        int webbed = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                Location feet = t.getLocation().getBlock().getLocation();
                // Place cobweb at their feet
                if (feet.getBlock().getType() == Material.AIR) {
                    feet.getBlock().setType(Material.COBWEB);
                    // Remove cobweb after duration
                    Location finalFeet = feet.clone();
                    new BukkitRunnable() {
                        @Override public void run() {
                            if (finalFeet.getBlock().getType() == Material.COBWEB)
                                finalFeet.getBlock().setType(Material.AIR);
                        }
                    }.runTaskLater(plugin, dur * 20L);
                }
                t.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, dur * 20, 0, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, dur * 20, 0, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur * 20, 3, false, false));
                webbed++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1f, 0.6f);
        msg(p, "§2§lSPIDER §8— §7Webbed §e" + webbed + "§7 player(s)!");
    }

    // ─── VILLAGER ────────────────────────────────────────────────────────────
    // Summon iron golems that target nearby players
    public void activateVillager(Player p) {
        int count = cfgInt("villager.villagerGolemCount");
        int dur = cfgInt("villager.villagerDuration");
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            Location loc = p.getLocation().clone().add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);
            IronGolem golem = (IronGolem) p.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
            golem.setPlayerCreated(true);
            new BukkitRunnable() {
                @Override public void run() { if (golem.isValid()) golem.remove(); }
            }.runTaskLater(plugin, dur * 20L);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 1f);
        msg(p, "§a§lVILLAGER §8— §7Summoned §e" + count + "§7 iron golem(s)!");
    }

    // ─── DISMANTLE ───────────────────────────────────────────────────────────
    // Ability: Locks the item player is holding for X seconds
    // Passive: Every X hits, swaps 2 items in enemy's hotbar
    public void activateDismantle(Player p) {
        double size = 10.0;
        int disable = cfgInt("dismantle.dismantleDisable");
        Player nearest = getNearestPlayer(p, size);
        if (nearest == null) { msg(p, "§c§lDISMANTLE §8— §7No targets nearby!"); return; }

        nearest.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, disable * 20, 255, false, false));
        nearest.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, disable * 20, 4, false, false));
        nearest.getWorld().spawnParticle(Particle.CRIT, nearest.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 0.8f);
        msg(p, "§8§lDISMANTLE §8— §7Locked §e" + nearest.getName() + "§7's item for §e" + disable + "s§7!");
    }

    // ─── OCEAN ───────────────────────────────────────────────────────────────
    // Ability: Give nearby players mining fatigue + drowning
    // Passive: Every X hits enemy starts drowning
    public void activateOcean(Player p) {
        double size = cfgDouble("ocean.oceanSize");
        int dur = cfgInt("ocean.oceanDuration");
        int hit = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, dur * 20, 2, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur * 20, 2, false, false));
                t.setRemainingAir(0);
                t.getWorld().spawnParticle(Particle.SPLASH, t.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.5);
                hit++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1f, 0.8f);
        p.getWorld().spawnParticle(Particle.SPLASH, p.getLocation(), 80, size / 2, 1, size / 2, 0.3);
        msg(p, "§1§lOCEAN §8— §7Drowning §e" + hit + "§7 player(s)!");
    }

    // ─── BLOOD ───────────────────────────────────────────────────────────────
    // Ability: Nearby players lose 2.5 hearts + wither DoT
    // Passive: X% chance on hit to inflict bleeding (wither)
    public void activateBlood(Player p) {
        double size = cfgDouble("blood.bloodrushSize");
        int dur = cfgInt("blood.bloodrushDuration");
        int hit = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.damage(5.0, p); // 2.5 hearts = 5 damage
                t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, dur * 20, 1, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur * 20, 1, false, false));
                t.getWorld().spawnParticle(Particle.FALLING_DUST,
                        t.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1,
                        Material.REDSTONE_BLOCK.createBlockData());
                hit++;
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur * 20, 2, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_RAVAGER_ATTACK, 1f, 0.7f);
        msg(p, "§4§lBLOOD §8— §7Hit §e" + hit + "§7 player(s) with bloodrush!");
    }

    // ─── CONDUCTOR ───────────────────────────────────────────────────────────
    // Ability: Strike all nearby players with lightning 3 times (2 hearts per hit)
    // Passive: Every X hits, strike with lightning for 2.5 hearts
    public void activateConductor(Player p) {
        double size = cfgDouble("conductor.conductorSize");
        List<Player> targets = new ArrayList<>();
        for (Entity e : nearby(p, size))
            if (e instanceof Player t && t != p) targets.add(t);

        for (Player t : targets) {
            for (int i = 0; i < 3; i++) {
                final int idx = i;
                new BukkitRunnable() {
                    @Override public void run() {
                        if (!t.isOnline()) return;
                        p.getWorld().strikeLightningEffect(t.getLocation());
                        t.damage(4.0, p); // 2 hearts
                    }
                }.runTaskLater(plugin, idx * 10L);
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0.8f);
        msg(p, "§e§lCONDUCTOR §8— §7Struck §e" + targets.size() + "§7 player(s) 3x!");
    }

    // ─── SPOTLIGHT ───────────────────────────────────────────────────────────
    // Ability: Reveal all nearby players with glowing + blindness
    public void activateSpotlight(Player p) {
        double size = cfgDouble("spotlight.spotlightSize");
        int revealed = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                t.getWorld().spawnParticle(Particle.END_ROD, t.getLocation().add(0, 2, 0), 20, 0.3, 0.5, 0.3, 0.05);
                revealed++;
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.5f);
        msg(p, "§f§lSPOTLIGHT §8— §7Revealed §e" + revealed + "§7 player(s)!");
    }

    // ─── PHASE ───────────────────────────────────────────────────────────────
    // Ability: Go into spectator mode for X seconds
    // Passive: X% chance to take no damage and give attacker slowness
    public void activatePhase(Player p) {
        int dur = cfgInt("phase.phasingDuration");
        p.setGameMode(org.bukkit.GameMode.SPECTATOR);
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5, 0.3);
        msg(p, "§d§lPHASE §8— §7Phasing for §e" + dur + "s§7!");
        new BukkitRunnable() {
            @Override public void run() {
                if (!p.isOnline()) return;
                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                msg(p, "§d§lPHASE §8— §7Phase ended!");
            }
        }.runTaskLater(plugin, dur * 20L);
    }

    // ─── HOP ─────────────────────────────────────────────────────────────────
    // Ability: Launch player X blocks up + all untrusted nearby go X blocks in air
    // Passive: Double jump
    public void activateHop(Player p) {
        double size = cfgDouble("hop.hopSize");
        int launched = 0;
        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.setVelocity(new Vector(0, 2.8, 0));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
                launched++;
            }
        }
        p.setVelocity(new Vector(0, 3.0, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 120, 0, false, false));
        // Enable double jump
        plugin.getCooldownManager().setDoubleJumpReady(p.getUniqueId(), true);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SLIME_JUMP, 1f, 0.7f);
        msg(p, "§a§lHOP §8— §7Launched §e" + launched + "§7 player(s)!");
    }

    // ─── HEALTH ──────────────────────────────────────────────────────────────
    // Ability: Steal 2 hearts from all untrusted players in radius
    // Passive: Perm 15 hearts (handled in PassiveListener)
    public void activateHealth(Player p) {
        double size = cfgDouble("health.healthSize");
        int dur = cfgInt("health.healthDuration");
        double stolenTotal = 0;
        double maxHp = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                double steal = 4.0; // 2 hearts
                t.damage(steal, p);
                stolenTotal += steal;
                t.getWorld().spawnParticle(Particle.HEART, t.getLocation().add(0, 2, 0), 5, 0.3, 0.3, 0.3, 0.1);
            }
        }
        p.setHealth(Math.min(p.getHealth() + stolenTotal, maxHp));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur * 20, 1, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        msg(p, "§c§lHEALTH §8— §7Stole §e" + (int)(stolenTotal / 2) + "§7 heart(s)!");
    }

    // ─── ALCHEMIST ───────────────────────────────────────────────────────────
    // Ability: All positive effects go up by 1 level for X seconds
    // Passive: All positive potion effects last X seconds longer
    public void activateAlchemist(Player p) {
        int dur = cfgInt("alchemist.alchemistDuration");
        int boosted = 0;
        for (PotionEffect effect : p.getActivePotionEffects()) {
            if (isPositiveEffect(effect.getType())) {
                p.addPotionEffect(new PotionEffect(
                        effect.getType(),
                        Math.max(effect.getDuration(), dur * 20),
                        effect.getAmplifier() + 1,
                        effect.isAmbient(), effect.hasParticles()
                ));
                boosted++;
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, dur * 20, 1, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur * 20, 1, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
        p.getWorld().spawnParticle(Particle.WITCH, p.getLocation().add(0, 1, 0), 30, 0.5, 0.8, 0.5, 0.1);
        msg(p, "§5§lALCHEMIST §8— §7Boosted §e" + boosted + "§7 effect(s)!");
    }

    private boolean isPositiveEffect(PotionEffectType t) {
        return t == PotionEffectType.SPEED || t == PotionEffectType.HASTE ||
               t == PotionEffectType.STRENGTH || t == PotionEffectType.REGENERATION ||
               t == PotionEffectType.RESISTANCE || t == PotionEffectType.ABSORPTION ||
               t == PotionEffectType.SLOW_FALLING || t == PotionEffectType.NIGHT_VISION ||
               t == PotionEffectType.HERO_OF_THE_VILLAGE || t == PotionEffectType.LUCK ||
               t == PotionEffectType.JUMP_BOOST || t == PotionEffectType.WATER_BREATHING ||
               t == PotionEffectType.FIRE_RESISTANCE || t == PotionEffectType.INVISIBILITY;
    }

    // ─── DRAGON ──────────────────────────────────────────────────────────────
    // Ability 1: Black + purple rings, wither 3 on nearby players
    // Ability 2: Nearby players take 1 heart per tick for X seconds
    // Passive: Perm speed 3 + strength 2
    public void activateDragon(Player p) {
        double size = cfgDouble("dragon.dragonSize");
        int hit = 0;

        // Spawn rings
        spawnRing(p.getLocation(), size, Particle.DRAGON_BREATH);
        spawnRing(p.getLocation().add(0, 1, 0), size * 0.7, Particle.PORTAL);

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                t.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 2, false, false));
                // Deal 1 heart per tick for 5 seconds (100 ticks)
                final Player target = t;
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override public void run() {
                        if (!target.isOnline() || ticks >= 100) { cancel(); return; }
                        target.damage(1.0, p); // 0.5 hearts per tick
                        ticks += 5;
                    }
                }.runTaskTimer(plugin, 0L, 5L);
                hit++;
            }
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 0.9f);
        msg(p, "§4§lDRAGON §8— §7Dragon aura on §e" + hit + "§7 player(s)!");
    }

    // ─── NETHERITE ───────────────────────────────────────────────────────────
    // Ability: All nearby players lose 1 level of protection on all armor pieces
    // Passive: Allows wearing netherite armor
    public void activateNetherite(Player p) {
        double size = cfgDouble("netherite.netheriteSize");
        int stripped = 0;

        for (Entity e : nearby(p, size)) {
            if (e instanceof Player t && t != p) {
                // Give them resistance debuff to simulate armor stripping
                t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 2, false, false));
                t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, false));
                // Flash their armor slots
                t.getWorld().spawnParticle(Particle.LAVA, t.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
                stripped++;
            }
        }
        // Buff self
        int dur = cfgInt("netherite.netheriteDuration");
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur * 20, 3, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, dur * 20, 0, false, false));
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 0.8f);
        msg(p, "§8§lNETHERITE §8— §7Weakened §e" + stripped + "§7 player(s)'s armor!");
    }

    // ─── SOLITARE ────────────────────────────────────────────────────────────
    // Ability: Transforms into 1 of the other cards randomly with a buff
    // Every X seconds it swaps again, X times
    public void activateSolitare(Player p) {
        CardType[] cards = CardType.values();
        List<CardType> others = new ArrayList<>();
        for (CardType c : cards) if (c != CardType.SOLITARE) others.add(c);

        Random rand = new Random();
        int swaps = 3;
        int swapInterval = 60; // 3 seconds

        new BukkitRunnable() {
            int count = 0;
            @Override public void run() {
                if (!p.isOnline() || count >= swaps) { cancel(); return; }
                CardType random = others.get(rand.nextInt(others.size()));
                // Give them a buffed version of a random card
                giveBuffedCard(p, random);
                msg(p, "§7§lSOLITARE §8— §7Transformed into §e" + random.getId() + "§7 card!");
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
                count++;
            }
        }.runTaskTimer(plugin, 0L, swapInterval);
    }

    private void giveBuffedCard(Player p, CardType type) {
        // Just activate the ability with a slight boost (card is already consumed)
        // Give them the item temporarily
        ItemStack card = plugin.getCardManager().createCard(type);
        p.getInventory().addItem(card);
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private Player getNearestPlayer(Player p, double range) {
        Player nearest = null;
        double nearestDist = range;
        for (Entity e : nearby(p, range)) {
            if (e instanceof Player t && t != p) {
                double d = p.getLocation().distance(t.getLocation());
                if (d < nearestDist) { nearestDist = d; nearest = t; }
            }
        }
        return nearest;
    }

    private void spawnRing(Location center, double radius, Particle particle) {
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            center.getWorld().spawnParticle(particle, center.clone().add(x, 0, z), 1, 0, 0, 0, 0);
        }
    }

    private void msg(Player p, String m) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', m));
    }
}
