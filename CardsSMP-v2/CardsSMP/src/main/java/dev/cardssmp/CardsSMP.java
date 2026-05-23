package dev.cardssmp;

import dev.cardssmp.commands.CardsCommand;
import dev.cardssmp.commands.GiveCardCommand;
import dev.cardssmp.listeners.CardInteractListener;
import dev.cardssmp.listeners.PassiveListener;
import dev.cardssmp.managers.CardManager;
import dev.cardssmp.managers.CooldownManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CardsSMP extends JavaPlugin {

    private static CardsSMP instance;
    private CardManager cardManager;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.cooldownManager = new CooldownManager();
        this.cardManager = new CardManager(this);

        getServer().getPluginManager().registerEvents(new CardInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PassiveListener(this), this);

        getCommand("givecard").setExecutor(new GiveCardCommand(this));
        getCommand("cards").setExecutor(new CardsCommand(this));

        getLogger().info("CardsSMP v2 enabled — " + cardManager.getClass().getSimpleName() + " ready!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CardsSMP disabled.");
    }

    public static CardsSMP getInstance() { return instance; }
    public CardManager getCardManager() { return cardManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
}
