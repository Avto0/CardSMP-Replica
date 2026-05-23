package dev.cardssmp;

import dev.cardssmp.commands.CardsCommand;
import dev.cardssmp.commands.GiveCardCommand;
import dev.cardssmp.listeners.CardInteractListener;
import dev.cardssmp.listeners.PassiveListener;
import dev.cardssmp.listeners.CraftingListener;
import dev.cardssmp.managers.CardManager;
import dev.cardssmp.managers.CooldownManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CardsSMP extends JavaPlugin {

    private static CardsSMP instance;
    private CardManager cardManager;
    private CooldownManager cooldownManager;

    // For Shadowstep reflect: UUID -> expiry time
    private final Map<UUID, Long> reflectingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.cooldownManager = new CooldownManager();
        this.cardManager = new CardManager(this);

        getServer().getPluginManager().registerEvents(new CardInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);

        getCommand("givecard").setExecutor(new GiveCardCommand(this));
        getCommand("cards").setExecutor(new CardsCommand(this));

        // Action bar updater
        int interval = getConfig().getInt("actionBarUpdateInterval", 20);
        new BukkitRunnable() {
            @Override public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateActionBar(p);
                }
            }
        }.runTaskTimer(this, 0L, interval);

        getLogger().info("CardsSMP v3 enabled!");
    }

    private void updateActionBar(Player p) {
        dev.cardssmp.cards.CardType held = cardManager.getCardType(p.getInventory().getItemInMainHand());
        dev.cardssmp.cards.CardType offhand = cardManager.getCardType(p.getInventory().getItemInOffHand());

        if (held == null && offhand == null) return;

        StringBuilder bar = new StringBuilder();

        if (held != null) {
            long cd = cooldownManager.getRemainingSeconds(p.getUniqueId(), held);
            if (cd > 0) {
                bar.append("§c").append(held.getId()).append(" §7(§e").append(cd).append("s§7)");
            } else {
                bar.append("§a").append(held.getId()).append(" §7[READY]");
            }
        }

        if (offhand != null) {
            if (bar.length() > 0) bar.append("  §8|  ");
            long cd = cooldownManager.getRemainingSeconds(p.getUniqueId(), offhand);
            if (cd > 0) {
                bar.append("§c").append(offhand.getId()).append(" §7(§e").append(cd).append("s§7)");
            } else {
                bar.append("§a").append(offhand.getId()).append(" §7[READY]");
            }
        }

        p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(bar.toString()));
    }

    @Override
    public void onDisable() {
        getLogger().info("CardsSMP disabled.");
    }

    public static CardsSMP getInstance() { return instance; }
    public CardManager getCardManager() { return cardManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public Map<UUID, Long> getReflectingPlayers() { return reflectingPlayers; }
}
