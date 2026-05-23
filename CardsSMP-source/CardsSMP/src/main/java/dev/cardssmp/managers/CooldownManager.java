package dev.cardssmp.managers;

import dev.cardssmp.cards.CardType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    // Map<PlayerUUID, Map<CardType, expiry time in millis>>
    private final Map<UUID, Map<CardType, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID player, CardType type) {
        Map<CardType, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns == null) return false;
        Long expiry = playerCooldowns.get(type);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    public long getRemainingSeconds(UUID player, CardType type) {
        Map<CardType, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns == null) return 0;
        Long expiry = playerCooldowns.get(type);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, (remaining + 999) / 1000); // round up
    }

    public void setCooldown(UUID player, CardType type, int seconds) {
        cooldowns.computeIfAbsent(player, k -> new HashMap<>())
                .put(type, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void clearCooldown(UUID player, CardType type) {
        Map<CardType, Long> playerCooldowns = cooldowns.get(player);
        if (playerCooldowns != null) {
            playerCooldowns.remove(type);
        }
    }

    public void clearAllCooldowns(UUID player) {
        cooldowns.remove(player);
    }
}
