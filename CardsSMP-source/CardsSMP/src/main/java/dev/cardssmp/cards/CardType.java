package dev.cardssmp.cards;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum CardType {

    FEATHER(
            "Feather Card",
            ChatColor.WHITE + "✦ " + ChatColor.AQUA + "Feather",
            ChatColor.GRAY + "Launch yourself into the sky and glide gracefully.",
            Material.FEATHER,
            ChatColor.AQUA
    ),
    FREEZE(
            "Freeze Card",
            ChatColor.WHITE + "✦ " + ChatColor.BLUE + "Freeze",
            ChatColor.GRAY + "Freeze all nearby enemies in place.",
            Material.PACKED_ICE,
            ChatColor.BLUE
    ),
    SHADOWSTEP(
            "Shadowstep Card",
            ChatColor.WHITE + "✦ " + ChatColor.DARK_PURPLE + "Shadowstep",
            ChatColor.GRAY + "Teleport behind the nearest player.",
            Material.ENDER_PEARL,
            ChatColor.DARK_PURPLE
    ),
    RESURRECTION(
            "Resurrection Card",
            ChatColor.WHITE + "✦ " + ChatColor.GOLD + "Resurrection",
            ChatColor.GRAY + "Instantly restore a burst of health.",
            Material.TOTEM_OF_UNDYING,
            ChatColor.GOLD
    ),
    VINDICATOR(
            "Vindicator Card",
            ChatColor.WHITE + "✦ " + ChatColor.RED + "Vindicator",
            ChatColor.GRAY + "Enter a rage, boosting your melee damage.",
            Material.IRON_AXE,
            ChatColor.RED
    ),
    DRAGON(
            "Dragon Card",
            ChatColor.WHITE + "✦ " + ChatColor.DARK_RED + "Dragon",
            ChatColor.GRAY + "Unleash a volley of dragon fireballs.",
            Material.DRAGON_BREATH,
            ChatColor.DARK_RED
    ),
    HEART(
            "Heart Card",
            ChatColor.WHITE + "✦ " + ChatColor.LIGHT_PURPLE + "Heart",
            ChatColor.GRAY + "Heal yourself and nearby allies.",
            Material.NETHER_STAR,
            ChatColor.LIGHT_PURPLE
    ),
    PHANTOM(
            "Phantom Card",
            ChatColor.WHITE + "✦ " + ChatColor.DARK_GRAY + "Phantom",
            ChatColor.GRAY + "Turn invisible and gain speed briefly.",
            Material.PHANTOM_MEMBRANE,
            ChatColor.DARK_GRAY
    ),
    THUNDER(
            "Thunder Card",
            ChatColor.WHITE + "✦ " + ChatColor.YELLOW + "Thunder",
            ChatColor.GRAY + "Strike nearby enemies with lightning.",
            Material.LIGHTNING_ROD,
            ChatColor.YELLOW
    ),
    VOID(
            "Void Card",
            ChatColor.WHITE + "✦ " + ChatColor.BLACK + "Void",
            ChatColor.GRAY + "Pull all nearby players toward you.",
            Material.CRYING_OBSIDIAN,
            ChatColor.DARK_PURPLE
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final Material material;
    private final ChatColor color;

    CardType(String id, String displayName, String description, Material material, ChatColor color) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.material = material;
        this.color = color;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getMaterial() { return material; }
    public ChatColor getColor() { return color; }

    public String getConfigKey() {
        return name().toLowerCase();
    }

    public static CardType fromString(String name) {
        for (CardType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.id.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
