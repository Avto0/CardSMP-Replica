package dev.cardssmp.managers;

import dev.cardssmp.cards.CardType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<CardType, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<CardType, Integer>> hitCounters = new HashMap<>();
    private final Map<UUID, Long> doubleJumpCooldown = new HashMap<>();

    public boolean isOnCooldown(UUID player, CardType type) {
        Map<CardType, Long> map = cooldowns.get(player);
        if (map == null) return false;
        Long expiry = map.get(type);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    public long getRemainingSeconds(UUID player, CardType type) {
        Map<CardType, Long> map = cooldowns.get(player);
        if (map == null) return 0;
        Long expiry = map.get(type);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis() + 999) / 1000);
    }

    public void setCooldown(UUID player, CardType type, int seconds) {
        cooldowns.computeIfAbsent(player, k -> new HashMap<>())
                .put(type, System.currentTimeMillis() + seconds * 1000L);
    }

    public void clearCooldown(UUID player, CardType type) {
        Map<CardType, Long> map = cooldowns.get(player);
        if (map != null) map.remove(type);
    }

    // Hit counters for passives
    public int incrementHits(UUID player, CardType type) {
        Map<CardType, Integer> map = hitCounters.computeIfAbsent(player, k -> new HashMap<>());
        int hits = map.getOrDefault(type, 0) + 1;
        map.put(type, hits);
        return hits;
    }

    public void resetHits(UUID player, CardType type) {
        Map<CardType, Integer> map = hitCounters.get(player);
        if (map != null) map.put(type, 0);
    }

    public int getHits(UUID player, CardType type) {
        Map<CardType, Integer> map = hitCounters.get(player);
        if (map == null) return 0;
        return map.getOrDefault(type, 0);
    }

    // Double jump cooldown for Hop card
    public boolean isDoubleJumpOnCooldown(UUID player, int seconds) {
        Long expiry = doubleJumpCooldown.get(player);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    public void setDoubleJumpCooldown(UUID player, int seconds) {
        doubleJumpCooldown.put(player, System.currentTimeMillis() + seconds * 1000L);
    }
}
