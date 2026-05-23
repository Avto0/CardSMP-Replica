package dev.cardssmp.cards;

import org.bukkit.Material;

public enum CardType {
    FEATHER("Feather", "§b✦ §fFeather Card", "§7Dash forward powerfully.", Material.FEATHER),
    FREEZE("Freeze", "§9✦ §fFreeze Card", "§7Freeze nearby enemies solid.", Material.PACKED_ICE),
    SHADOWSTEP("Shadowstep", "§5✦ §fShadowstep Card", "§7Teleport behind a target and strike.", Material.ENDER_PEARL),
    RESURRECTION("Resurrection", "§6✦ §fResurrection Card", "§7Grant nearby players revival buffs.", Material.TOTEM_OF_UNDYING),
    GLIDE("Glide", "§3✦ §fGlide Card", "§7Boost forward and glide through the air.", Material.ELYTRA),
    VINDICATOR("Vindicator", "§c✦ §fVindicator Card", "§7Enter a ravager rage.", Material.IRON_AXE),
    SPIDER("Spider", "§2✦ §fSpider Card", "§7Web nearby enemies.", Material.COBWEB),
    VILLAGER("Villager", "§a✦ §fVillager Card", "§7Summon iron golems to fight for you.", Material.EMERALD),
    DISMANTLE("Dismantle", "§8✦ §fDismantle Card", "§7Lock the item in your target's hand.", Material.ANVIL),
    OCEAN("Ocean", "§1✦ §fOcean Card", "§7Drown nearby enemies.", Material.HEART_OF_THE_SEA),
    BLOOD("Blood", "§4✦ §fBlood Card", "§7Rush nearby enemies and cause bleeding.", Material.REDSTONE_BLOCK),
    CONDUCTOR("Conductor", "§e✦ §fConductor Card", "§7Strike nearby enemies with lightning.", Material.LIGHTNING_ROD),
    SPOTLIGHT("Spotlight", "§f✦ §fSpotlight Card", "§7Reveal and glow all nearby enemies.", Material.GLOWSTONE),
    PHASE("Phase", "§d✦ §fPhase Card", "§7Phase through the world temporarily.", Material.AMETHYST_SHARD),
    HOP("Hop", "§a✦ §fHop Card", "§7Launch all nearby players into the air.", Material.RABBIT_FOOT),
    HEALTH("Health", "§c✦ §fHealth Card", "§7Heal yourself and nearby allies.", Material.GLISTERING_MELON_SLICE),
    ALCHEMIST("Alchemist", "§5✦ §fAlchemist Card", "§7Extend all active potion effects.", Material.BREWING_STAND),
    DRAGON("Dragon", "§4✦ §fDragon Card", "§7Unleash dragon fire on nearby enemies.", Material.DRAGON_BREATH),
    NETHERITE("Netherite", "§8✦ §fNetherite Card", "§7Become an unstoppable tank.", Material.NETHERITE_INGOT),
    SOLITARE("Solitare", "§7✦ §fSolitare Card", "§7Deal massive damage to nearby enemies.", Material.NETHERITE_SWORD);

    private final String id;
    private final String displayName;
    private final String description;
    private final Material material;

    CardType(String id, String displayName, String description, Material material) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.material = material;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getMaterial() { return material; }
    public String getConfigKey() { return name().toLowerCase(); }

    public static CardType fromString(String name) {
        for (CardType t : values()) {
            if (t.name().equalsIgnoreCase(name) || t.id.equalsIgnoreCase(name)) return t;
        }
        return null;
    }
}
