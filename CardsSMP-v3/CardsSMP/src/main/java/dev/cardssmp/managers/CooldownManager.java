package dev.cardssmp.managers;

import dev.cardssmp.cards.CardType;

import java.util.*;

public class CooldownManager {

    private final Map<UUID, Map<CardType, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<CardType, Integer>> hitCounters = new HashMap<>();
    private final Map<UUID, Long> doubleJumpCooldowns = new HashMap<>();
    private final Set<UUID> doubleJumpReady = new HashSet<>();

    public boolean isOnCooldown(UUID p, CardType t) {
        Map<CardType, Long> m = cooldowns.get(p);
        if (m == null) return false;
        Long exp = m.get(t);
        return exp != null && System.currentTimeMillis() < exp;
    }

    public long getRemainingSeconds(UUID p, CardType t) {
        Map<CardType, Long> m = cooldowns.get(p);
        if (m == null) return 0;
        Long exp = m.get(t);
        if (exp == null) return 0;
        return Math.max(0, (exp - System.currentTimeMillis() + 999) / 1000);
    }

    public void setCooldown(UUID p, CardType t, int seconds) {
        cooldowns.computeIfAbsent(p, k -> new HashMap<>())
                .put(t, System.currentTimeMillis() + seconds * 1000L);
    }

    public int incrementHits(UUID p, CardType t) {
        int hits = hitCounters.computeIfAbsent(p, k -> new HashMap<>())
                .getOrDefault(t, 0) + 1;
        hitCounters.get(p).put(t, hits);
        return hits;
    }

    public void resetHits(UUID p, CardType t) {
        Map<CardType, Integer> m = hitCounters.get(p);
        if (m != null) m.put(t, 0);
    }

    // Double jump for Hop card
    public boolean hasDoubleJumpReady(UUID p) { return doubleJumpReady.contains(p); }
    public void setDoubleJumpReady(UUID p, boolean ready) {
        if (ready) doubleJumpReady.add(p);
        else doubleJumpReady.remove(p);
    }

    public boolean isDoubleJumpOnCooldown(UUID p) {
        Long exp = doubleJumpCooldowns.get(p);
        return exp != null && System.currentTimeMillis() < exp;
    }

    public void setDoubleJumpCooldown(UUID p, int seconds) {
        doubleJumpCooldowns.put(p, System.currentTimeMillis() + seconds * 1000L);
    }

    public void clearPlayer(UUID p) {
        cooldowns.remove(p);
        hitCounters.remove(p);
        doubleJumpReady.remove(p);
    }
}
