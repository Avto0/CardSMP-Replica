package dev.cardssmp.cards;

import org.bukkit.Material;

public enum CardType {
    FEATHER("Feather", "§b§lFEATHER CARD", "§7Fast dash forward.", Material.FEATHER),
    FREEZE("Freeze", "§9§lFREEZE CARD", "§7Freeze nearby enemies.", Material.PACKED_ICE),
    SHADOWSTEP("Shadowstep", "§5§lSHADOWSTEP CARD", "§7Reflect all attacks for a duration.", Material.ENDER_PEARL),
    RESURRECTION("Resurrection", "§6§lRESURRECTION CARD", "§7Buff nearby allies.", Material.TOTEM_OF_UNDYING),
    GLIDE("Glide", "§3§lGLIDE CARD", "§7Boost + elytra glide.", Material.ELYTRA),
    VINDICATOR("Vindicator", "§c§lVINDICATOR CARD", "§7Ravager rage.", Material.IRON_AXE),
    SPIDER("Spider", "§2§lSPIDER CARD", "§7Spawn cobwebs under enemies.", Material.COBWEB),
    VILLAGER("Villager", "§a§lVILLAGER CARD", "§7Summon iron golems.", Material.EMERALD),
    DISMANTLE("Dismantle", "§8§lDISMANTLE CARD", "§7Lock item in enemy's hand.", Material.ANVIL),
    OCEAN("Ocean", "§1§lOCEAN CARD", "§7Drown nearby enemies.", Material.HEART_OF_THE_SEA),
    BLOOD("Blood", "§4§lBLOOD CARD", "§7Wither + damage nearby enemies.", Material.REDSTONE_BLOCK),
    CONDUCTOR("Conductor", "§e§lCONDUCTOR CARD", "§7Strike all nearby enemies with lightning.", Material.LIGHTNING_ROD),
    SPOTLIGHT("Spotlight", "§f§lSPOTLIGHT CARD", "§7Reveal all nearby enemies.", Material.GLOWSTONE),
    PHASE("Phase", "§d§lPHASE CARD", "§7Go into spectator mode briefly.", Material.AMETHYST_SHARD),
    HOP("Hop", "§a§lHOP CARD", "§7Launch everyone up.", Material.RABBIT_FOOT),
    HEALTH("Health", "§c§lHEALTH CARD", "§7Steal hearts from nearby enemies.", Material.GLISTERING_MELON_SLICE),
    ALCHEMIST("Alchemist", "§5§lALCHEMIST CARD", "§7Boost all potion effects.", Material.BLAZE_POWDER),
    DRAGON("Dragon", "§4§lDRAGON CARD", "§7Wither + damage aura.", Material.DRAGON_BREATH),
    NETHERITE("Netherite", "§8§lNETHERITE CARD", "§7Strip armor from nearby enemies.", Material.NETHERITE_INGOT),
    SOLITARE("Solitare", "§7§lSOLITARE CARD", "§7Transforms into a random card with a buff.", Material.NETHERITE_SWORD);

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
        for (CardType t : values())
            if (t.name().equalsIgnoreCase(name) || t.id.equalsIgnoreCase(name)) return t;
        return null;
    }
}
