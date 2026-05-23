package dev.cardssmp.commands;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GiveCardCommand implements CommandExecutor, TabCompleter {

    private final CardsSMP plugin;

    public GiveCardCommand(CardsSMP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("cardssmp.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givecard <player> <card>");
            sender.sendMessage(ChatColor.GRAY + "Cards: " + Arrays.stream(CardType.values())
                    .map(t -> t.name().toLowerCase())
                    .collect(Collectors.joining(", ")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
            return true;
        }

        CardType type = CardType.fromString(args[1]);
        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Unknown card: " + args[1]);
            sender.sendMessage(ChatColor.GRAY + "Cards: " + Arrays.stream(CardType.values())
                    .map(t -> t.name().toLowerCase())
                    .collect(Collectors.joining(", ")));
            return true;
        }

        ItemStack card = plugin.getCardManager().createCard(type);
        target.getInventory().addItem(card);

        sender.sendMessage(ChatColor.GREEN + "Gave " + type.name().toLowerCase() + " card to " + target.getName() + "!");
        target.sendMessage(ChatColor.LIGHT_PURPLE + "You received a " + type.getDisplayName() + ChatColor.LIGHT_PURPLE + " card!");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 2) {
            Arrays.stream(CardType.values())
                    .map(t -> t.name().toLowerCase())
                    .forEach(completions::add);
        }
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
