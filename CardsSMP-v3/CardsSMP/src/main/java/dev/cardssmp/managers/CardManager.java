package dev.cardssmp.managers;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(type.getDisplayName()));

        List<Component> lore = Arrays.asList(
                Component.empty(),
                LegacyComponentSerializer.legacySection().deserialize(type.getDescription()),
                Component.empty(),
                LegacyComponentSerializer.legacySection().deserialize("§8» Right-click to activate «"),
                LegacyComponentSerializer.legacySection().deserialize("§8§o(Single use)"),
                Component.empty()
        );
        meta.lore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(cardKey, PersistentDataType.STRING, type.name());

        item.setItemMeta(meta);
        return item;
    }

    public CardType getCardType(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;
        String val = item.getItemMeta().getPersistentDataContainer().get(cardKey, PersistentDataType.STRING);
        if (val == null) return null;
        try { return CardType.valueOf(val); } catch (IllegalArgumentException e) { return null; }
    }

    public boolean isCard(ItemStack item) { return getCardType(item) != null; }

    // Consume the card from player's hand (single use)
    public void consumeCard(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }

    public NamespacedKey getCardKey() { return cardKey; }
}
