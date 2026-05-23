package dev.cardssmp.commands;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CardsCommand implements CommandExecutor {

    private final CardsSMP plugin;

    public CardsCommand(CardsSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "━━━━━━━━ " + ChatColor.LIGHT_PURPLE + "Cards SMP" + ChatColor.DARK_PURPLE + " ━━━━━━━━");
        for (CardType type : CardType.values()) {
            sender.sendMessage(" " + type.getDisplayName() + ChatColor.DARK_GRAY + " — " + type.getDescription());
        }
        sender.sendMessage(ChatColor.DARK_PURPLE + "━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.GRAY + "Right-click to activate. Cards are single use!");
        return true;
    }
}
