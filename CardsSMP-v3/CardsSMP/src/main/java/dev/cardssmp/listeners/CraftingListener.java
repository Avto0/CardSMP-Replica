package dev.cardssmp.listeners;

import dev.cardssmp.CardsSMP;
import dev.cardssmp.cards.CardType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

public class CraftingListener implements org.bukkit.event.Listener {

    private final CardsSMP plugin;

    public CraftingListener(CardsSMP plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {
        reg(CardType.FEATHER,    "ABA","BCB","ABA", 'A',Material.PHANTOM_MEMBRANE,'B',Material.FEATHER,'C',Material.HEAVY_CORE);
        reg(CardType.ALCHEMIST,  "ABA","BCB","ABA", 'A',Material.NETHER_WART,'B',Material.BLAZE_POWDER,'C',Material.BREWING_STAND);
        reg(CardType.RESURRECTION,"ABA","BCB","ABA",'A',Material.TOTEM_OF_UNDYING,'B',Material.NETHERITE_INGOT,'C',Material.NETHER_STAR);
        reg(CardType.HEALTH,     "ABA","BCB","ABA", 'A',Material.GLISTERING_MELON_SLICE,'B',Material.RED_MUSHROOM,'C',Material.GOLDEN_APPLE);
        reg(CardType.VINDICATOR, "ABA","CDC","AEA", 'A',Material.DEEPSLATE_DIAMOND_ORE,'B',Material.IRON_AXE,'C',Material.GOLDEN_AXE,'D',Material.PLAYER_HEAD,'E',Material.NETHERITE_AXE);
        reg(CardType.HOP,        "ABA","BCB","ABA", 'A',Material.FEATHER,'B',Material.RABBIT_FOOT,'C',Material.SLIME_BLOCK);
        reg(CardType.GLIDE,      "ABA","CDC","ABA", 'A',Material.GOLD_BLOCK,'B',Material.ENCHANTED_GOLDEN_APPLE,'C',Material.FEATHER,'D',Material.ELYTRA);
        reg(CardType.PHASE,      "ABA","BCB","ABA", 'A',Material.AMETHYST_SHARD,'B',Material.ENDER_PEARL,'C',Material.NETHER_STAR);
        reg(CardType.SPIDER,     "ABA","BCB","ABA", 'A',Material.COBWEB,'B',Material.MOSSY_COBBLESTONE,'C',Material.FERMENTED_SPIDER_EYE);
        reg(CardType.DRAGON,     "ABA","BCB","ABA", 'A',Material.DRAGON_HEAD,'B',Material.DRAGON_BREATH,'C',Material.DRAGON_EGG);
        reg(CardType.VILLAGER,   "ABA","BCB","ABA", 'A',Material.EMERALD,'B',Material.EMERALD_BLOCK,'C',Material.GOLDEN_APPLE);
        reg(CardType.DISMANTLE,  "ABA","BCB","ABA", 'A',Material.OBSIDIAN,'B',Material.IRON_BLOCK,'C',Material.OMINOUS_TRIAL_KEY);
        reg(CardType.BLOOD,      "ABA","BCB","ABA", 'A',Material.NETHER_WART,'B',Material.REDSTONE_BLOCK,'C',Material.GHAST_TEAR);
        reg(CardType.CONDUCTOR,  "ABA","BCB","ABA", 'A',Material.IRON_INGOT,'B',Material.COPPER_BLOCK,'C',Material.LIGHTNING_ROD);
        reg(CardType.OCEAN,      "ABA","BCB","ABA", 'A',Material.NAUTILUS_SHELL,'B',Material.PRISMARINE_CRYSTALS,'C',Material.HEART_OF_THE_SEA);
        reg(CardType.SPOTLIGHT,  "ABA","BCB","ABA", 'A',Material.SEA_LANTERN,'B',Material.GLOWSTONE,'C',Material.SPYGLASS);
        reg(CardType.FREEZE,     "ABA","BCB","ABA", 'A',Material.BLUE_ICE,'B',Material.PACKED_ICE,'C',Material.NETHERITE_INGOT);
        reg(CardType.SHADOWSTEP, "ABA","CDC","BAB", 'A',Material.LEATHER_BOOTS,'B',Material.DIAMOND_BOOTS,'C',Material.GOLDEN_BOOTS,'D',Material.SOUL_SAND);
        reg(CardType.NETHERITE,  "ABA","BCB","ABA", 'A',Material.NETHERITE_SCRAP,'B',Material.ANCIENT_DEBRIS,'C',Material.NETHERITE_BLOCK);
        reg(CardType.SOLITARE,   "ABA","BCB","ABA", 'A',Material.PLAYER_HEAD,'B',Material.NETHERITE_SCRAP,'C',Material.NETHERITE_SWORD);
    }

    private void reg(CardType type, String r1, String r2, String r3, Object... ingredients) {
        try {
            NamespacedKey key = new NamespacedKey(plugin, "card_" + type.name().toLowerCase());
            ShapedRecipe recipe = new ShapedRecipe(key, plugin.getCardManager().createCard(type));
            recipe.shape(r1, r2, r3);
            for (int i = 0; i < ingredients.length; i += 2) {
                char c = (char) ingredients[i];
                Material m = (Material) ingredients[i + 1];
                recipe.setIngredient(c, m);
            }
            plugin.getServer().addRecipe(recipe);
        } catch (Exception e) {
            plugin.getLogger().warning("Recipe error for " + type.name() + ": " + e.getMessage());
        }
    }
}
