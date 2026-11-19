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

        // Grass seeds
        d.item("grass_seed", "Use it on Dirt, or on Corrupt, Crimson or Hallow Grass Blocks, to turn the block into a normal Grass Block. Used up when it works.");
        d.item("ash_grass_seed", "Use it on an Ash Block to turn it into an Ash Grass Block. Used up when it works.");
        d.item("jungle_grass_seed", "Use it on Mud to turn it into a Jungle Grass Block. Used up when it works.");
        d.item("mushroom_grass_seed", "Use it on Mud to turn it into a Mushroom Grass Block. Used up when it works.");

        // Mushrooms
        d.item("life_mushroom", "Use it to restore 3 hearts of health and some hunger. Causes Potion Sickness for 30 seconds, during which it cannot be used.", "It can also be planted on grass, and Bone Meal grows it into a Life Mushroom tree.");
        d.item("vile_mushroom", "Can only be planted on Corrupt Grass Blocks.");
        d.item("vicious_mushroom", "Can only be planted on Crimson Grass Blocks.");
        d.item("glowing_mushroom", "Can only be planted on Mushroom Grass Blocks. It gives off light, and Bone Meal grows it into a Glowing Mushroom tree.");

        // Storage
        d.item("chester_staff", "Use it to summon Chester, a companion that opens your personal storage. Sneak and use it to send Chester away. Using it again moves Chester to you.");
        d.item("money_trough", "Use it to summon a Flying Piggy Bank that opens your personal storage. Sneak and use it to send it away. Using it again moves it to you.");
        d.item("void_bag", "Use it to open your Void Vault, a personal storage you can reach from anywhere while carrying the bag.");

        // Treasure bags and right-click loot
        for (String boss : new String[]{"king_slime", "eye_of_cthulhu", "eater_of_worlds", "brain_of_cthulhu", "queen_bee", "skeletron", "deerclops", "the_twins"}) {
            d.item(boss + "_treasure_bag", "Use it to open it and receive loot. Sneak and use it to open the whole stack at once. The loot depends on the world difficulty.");
        }
        d.item("clam", "Use it to open it and receive pearls or a shucked oyster. Sneak and use it to open the whole stack at once.");
        d.item("pine_cone", "Use it to open it and receive pine nuts. Sneak and use it to open the whole stack at once.");
        d.item("herb_bag", "Use it to open it and receive 2 to 5 herbs or herb seeds. Sneak and use it to open the whole stack at once.");
        d.item("can_of_worms", "Use it to open it and receive worms or enchanted nightcrawlers. Sneak and use it to open the whole stack at once.");
        d.item("red_envelope", "Use it to open it and receive coins. Sneak and use it to open the whole stack at once.");
        d.item("sugar_tangerine", "Use it to open it and receive peeled sugar tangerines. Sneak and use it to open the whole stack at once.");
        d.item("deluxe_package", "Use it to open it and receive prepared food and drinks. Sneak and use it to open the whole stack at once.");
        d.item("christmas_gift", "Use it to open it and receive festive treats, candy blocks or winter gear. Sneak and use it to open the whole stack at once.");

        // Tools and misc
        d.item("honey_bucket", "Place it to pour out honey, or hold use to drink it for the Honey effect for 45 seconds. Drinking also cures Poison and leaves an empty bucket.");
        d.item("bottle", "Use it on water or honey to fill it. Use it on a block to place a Brewing Vial, or sneak and use it to place a Brewing Vial instead of filling it.");
        d.item("eyedropper", "Use it on a painted block face to copy that colour into a Paint item. The Paint is placed in your off hand, replacing what is there.");
        d.item("staff_of_regrowth", "Use it on Dirt to turn it into Grass. Right-click a crop to harvest it, replant it and get extra drops, which Fortune improves.");
        d.item("crimson_rod", "Summons a blood cloud that rains on enemies. Aim at an enemy to make the cloud target it. Only one cloud can exist at a time.");
        d.item("nimbus_rod", "Summons a rain cloud that rains on enemies. Aim at an enemy to make the cloud target it. Up to two clouds can exist at a time.");
        d.item("golden_shower", "Hold to spray golden ichor that falls in an arc and inflicts Ichor on enemies. Uses mana while you hold it.");
        d.item("spiky_ball", "Throw it to bounce along the ground and hurt enemies it touches. It is used up when thrown.");

        // Mounts
        d.item("slimy_saddle", "Summons a slime mount that jumps very high. Landing on top of an enemy hurts it and bounces you back up.");
        d.item("honeyed_goggles", "Summons a bee mount that can fly. Its flight energy is limited and it slows down as it runs out.");
        d.item("blessed_apple", "Summons a unicorn mount that runs fast and hurts enemies it charges into.");
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
