package org.confluence.mod.common.data.gen.language;

import org.confluence.mod.common.data.TranslationConsumer;

/// Plain English descriptions for items whose behaviour is not obvious from the name.
///
/// A description is stored under `item.confluence.<id>.desc` (one line) or `.desc.0`, `.desc.1`, ...
/// and is shown by the client when Shift is held, see `ItemDescriptionTooltip`.
/// Only English is written; the same text is used for the Chinese file.
public final class ItemDescriptionLanguageSubProvider {
    private ItemDescriptionLanguageSubProvider() {}

    public static void addTranslations(TranslationConsumer language) {
        Describer d = new Describer(language);

        // Throwables
        d.item("bomb", "Explodes shortly after landing, hurting enemies and breaking nearby blocks.");
        d.item("bouncy_bomb", "A bomb that bounces off surfaces before it explodes.");
        d.item("sticky_bomb", "Sticks to the first block it hits, then explodes.");
        d.item("bomb_fish", "Sticks to the first block it hits, then explodes.");
        d.item("smoke_bomb", "Only releases a cloud of smoke. It does not explode or break blocks.");
        d.item("scarab_bomb", "Sets off a line of blasts in the direction it was thrown, tunnelling through blocks softer than obsidian.");
        d.item("dynamite", "A bigger explosive than the bomb. Breaks blocks and hurts enemies in a wide radius.");
        d.item("bouncy_dynamite", "Dynamite that bounces off surfaces before it explodes.");
        d.item("sticky_dynamite", "Dynamite that sticks to the first block it hits, then explodes.");
        d.item("grenade", "Hurts enemies in a small radius but does not break blocks.");
        d.item("bouncy_grenade", "A grenade that bounces off surfaces before it explodes. Does not break blocks.");
        d.item("sticky_grenade", "A grenade that sticks to the first block it hits. Does not break blocks.");
        d.item("beenade", "A grenade that releases bees when it explodes. Does not break blocks.");
        d.item("dirt_bomb", "Fills the area around it with dirt.");
        d.item("sticky_dirt_bomb", "Sticks to the first block it hits, then fills the area around it with dirt.");
        d.item("dry_bomb", "Removes the liquid around it before exploding. The blast does not break blocks.");
        d.item("wet_bomb", "Fills the area around it with water.");
        d.item("lava_bomb", "Fills the area around it with lava.");
        d.item("honey_bomb", "Fills the area around it with honey.");
        d.item("holy_water", "When thrown, turns nearby blocks back to their pure form.");
        d.item("unholy_water", "When thrown, turns nearby blocks into the Corruption.");
        d.item("blood_water", "When thrown, turns nearby blocks into the Crimson.");

        // Placement wands
        d.item("living_wood_wand", "Place it to build Living Wood logs, using any other log from your off hand or inventory.");
        d.item("leaf_wand", "Place it to build Living Wood leaves, using any other leaves from your off hand or inventory.");
        d.item("living_mahogany_wand", "Place it to build Living Mahogany logs, using any other log from your off hand or inventory.");
        d.item("rich_mahogany_leaf_wand", "Place it to build Rich Mahogany leaves, using any other leaves from your off hand or inventory.");
        d.item("hive_wand", "Place it to build natural Jungle Hive blocks, using Jungle Hive blocks from your off hand or inventory.");

        // Tools and utilities
        d.item("bottomless_water_bucket", "A bucket with an endless supply of water.");
        d.item("bottomless_lava_bucket", "A bucket with an endless supply of lava.");
        d.item("bottomless_honey_bucket", "A bucket with an endless supply of honey.");
        d.item("bottomless_shimmer_bucket", "A bucket with an endless supply of shimmer.");
        for (String colour : new String[]{"red", "green", "blue", "yellow"}) {
            d.item(colour + "_wrench", "Use it on one network device and then on another to connect them with a " + colour + " wire.", "Greatly extends your block reach while held.");
        }
        d.item("rope_coil", "Throw it to unroll a length of rope where it lands.");
        d.item("vine_rope_coil", "Throw it to unroll a length of vine rope where it lands.");
        d.item("silk_rope_coil", "Throw it to unroll a length of silk rope where it lands.");
        d.item("web_rope_coil", "Throw it to unroll a length of web rope where it lands.");
        d.item("magic_sand_dropper", "Use it on a block to add a decorative falling-sand effect there. Used up when placed.");
        d.item("magic_water_dropper", "Use it on a block to add a decorative dripping-water effect there. Used up when placed.");
        d.item("magic_lava_dropper", "Use it on a block to add a decorative dripping-lava effect there. Used up when placed.");
        d.item("magic_honey_dropper", "Use it on a block to add a decorative dripping-honey effect there. Used up when placed.");
        d.item("purification_powder", "Throw it to turn nearby blocks back to their pure form.");
        d.item("vile_powder", "Throw it to turn nearby blocks into the Corruption.");
        d.item("vicious_powder", "Throw it to turn nearby blocks into the Crimson.");
        d.item("cobalt_garden_shears", "Shears with extra reach. They can also strip logs like an axe.");
        d.item("palladium_garden_shears", "Shears with extra reach. They can also strip logs like an axe.");

        // Weapons and tools with special behaviour
        d.item("axe_of_regrowth", "Use it on dirt to turn it into grass.");
        d.item("molten_fury", "Fires hellfire arrows.");
        d.item("the_bees_knees", "Fires bee arrows, three at a time when loaded with ordinary arrows.");
        d.item("falcon_blade", "Each hit adds to its damage, up to +50%. The bonus fades quickly if you stop hitting.");
        d.item("exotic_scimitar", "Each hit adds to its damage, up to +50%. The bonus fades quickly if you stop hitting.");
        d.item("purple_clubberfish", "Has a 50% chance to confuse enemies it hits.");
        d.item("blood_butcherer", "Inflicts Blood Butchered on every hit.");
        d.item("tentacle_mace", "Inflicts Tentacle Spikes on every hit.");
        d.item("waffles_iron", "Has a one in three chance to inflict Hellfire on hit.");
        // Phaseblades and whips
        for (String colour : new String[]{"red", "orange", "yellow", "green", "blue", "purple", "white", "pink"}) {
            d.item(colour + "_phaseblade", "Only active while held in your main hand. Use it to throw the blade, and use it again to recall it.");
            d.item(colour + "_phasesaber", "Only active while held in your main hand. Use it to throw the blade, and use it again to recall it.");
        }
        for (String whip : new String[]{"slub", "ruby", "amber", "topaz", "jade", "diamond", "sapphire", "amethyst"}) {
            d.item(whip + "_whip", "Use it to lash enemies in a long arc. Scales with summon damage and tags enemies for your minions.");
        }

        d.item("scrying_orb", "Multiplayer only. Use it to spectate another player, and use it again to cycle to the next. Sneak to stop.");
        d.item("skeletron_hand", "A grappling hook. Several hooks can be held at the same time.");
        d.item("ivy_whip", "A grappling hook. Several hooks can be held at the same time.");
        d.item("fertilizer", "Use it on a sapling or crop to grow it instantly. Used up when it works.");
        for (String ring : new String[]{"fertile_singularity", "perplexed_cat_medal", "pulsar"}) {
            d.item(ring, "Carry it in your inventory to gain Speed while monsters are within 8 blocks.");
        }
    }

    private record Describer(TranslationConsumer language) {
        /// @param id    registry path of the item, without namespace
        /// @param lines one string per tooltip line
        void item(String id, String... lines) {
            add("item.confluence." + id, lines);
        }

        void block(String id, String... lines) {
            add("block.confluence." + id, lines);
        }

        private void add(String base, String[] lines) {
            if (lines.length == 1) {
                language.add(base + ".desc", lines[0], lines[0]);
                return;
            }
            for (int i = 0; i < lines.length; i++) {
                language.add(base + ".desc." + i, lines[i], lines[i]);
            }
        }
    }
}
