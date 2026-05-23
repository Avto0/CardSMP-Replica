package dev.cardssmp.managers;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class CardManager {

    private final CardsSMP plugin;
    private final NamespacedKey cardKey;

    public CardManager(CardsSMP plugin) {
        this.plugin = plugin;
        this.cardKey = new NamespacedKey(plugin, "card_type");
    }

    public ItemStack createCard(CardType type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        // Display name
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(type.getDisplayName().replace(ChatColor.COLOR_CHAR, '&')));

        // Lore
        List<Component> lore = Arrays.asList(
                Component.empty(),
                LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(type.getDescription().replace(ChatColor.COLOR_CHAR, '&')),
                Component.empty(),
                Component.text("» Right-click to activate «")
                        .color(NamedTextColor.DARK_GRAY),
                Component.empty()
        );
        meta.lore(lore);

        // Glint
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setUnbreakable(true);

        // NBT tag to identify the card
        meta.getPersistentDataContainer().set(cardKey, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    public CardType getCardType(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;

        String value = item.getItemMeta().getPersistentDataContainer()
                .get(cardKey, PersistentDataType.STRING);
        if (value == null) return null;

        try {
            return CardType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isCard(ItemStack item) {
        return getCardType(item) != null;
    }

    public boolean isCardEnabled(CardType type) {
        return plugin.getConfig().getBoolean("cards." + type.getConfigKey() + ".enabled", true);
    }

    public int getCardCount() {
        return CardType.values().length;
    }

    public NamespacedKey getCardKey() {
        return cardKey;
    }
}
