package org.confluence.mod.common.data.gen.language;

import org.confluence.mod.common.data.TranslationConsumer;

/// Plain English descriptions of mob effects, stored under `effect.confluence.<id>.description`.
/// JEED (Just Enough Effect Descriptions) shows them in the effect page of JEI.
/// Only English is written; the same text is used for the Chinese file.
public final class EffectDescriptionLanguageSubProvider {
    private EffectDescriptionLanguageSubProvider() {}

    public static void addTranslations(TranslationConsumer language) {
        // Buffs: combat
        d(language, "iron_skin", "Raises your defense by 4.");
        d(language, "the_bast_defense", "Raises your defense by 5.");
        d(language, "endurance", "Stops you from being knocked back.");
        d(language, "rage", "Raises your critical hit chance by 10%.");
        d(language, "wrath", "Raises all your damage by 10%.");
        d(language, "magic_power", "Raises your magic damage by 20%.");
        d(language, "clairvoyance", "Slightly raises your magic damage and critical hit chance.");
        d(language, "archery", "Arrows you shoot fly faster and deal 10% more damage.");
        d(language, "titan", "Your attacks knock enemies back further.");
        d(language, "sharpened", "Your melee attacks ignore part of an enemy's armor.");
        d(language, "thorns", "Whoever hits you takes the same damage back.");
        d(language, "life_force", "Raises your maximum health by 20%.");
        d(language, "inferno", "Sets enemies within 5 blocks of you on fire.");
        d(language, "obsidian_skin", "Makes you immune to lava and fire damage.");
        d(language, "titanium_barrier", "Titanium shards circle you and protect you from damage.");
        d(language, "holy_protection", "Meant to let you dodge the next hit. Nothing in the pack gives it yet.");
        d(language, "dryads_blessing", "Raises your defense by 8, slowly heals you and sends part of the damage you take back at the attacker.");
        d(language, "tipsy", "Melee hits are stronger and faster, but your defense is lower.");
        d(language, "exquisitely_stuffed", "A good meal boosts many stats a little: speed, defense, critical chance, all damage, attack speed and mining speed. Better food gives more.");
        d(language, "delicious", "Good effects you receive last twice as strong, bad ones half as strong.");
        d(language, "weapon_imbue_fire", "Your melee attacks set enemies on fire. Only one flask can be active at a time and it stays after death.");
        d(language, "weapon_imbue_gold", "Your melee attacks make enemies drop more money. Only one flask can be active at a time and it stays after death.");
        d(language, "jungles_fury", "Your whips swing 12% faster.");
        d(language, "ammo_box", "Each shot has a 20% chance not to use up ammo.");
        d(language, "enemy_banner", "You deal more damage to, and take less from, the enemies shown on the banner you are standing near.");

        // Buffs: summons
        d(language, "summoning", "You can have one more minion at the same time.");
        d(language, "bewitched", "You can have one more minion at the same time.");
        d(language, "star_harvest", "You can have one more minion at the same time.");

        // Buffs: life and food
        d(language, "cozy_fire", "Slowly heals you.");
        d(language, "heart_lantern", "Heals you a little faster than Cozy Fire.");
        d(language, "aromatic_satiation", "Refills your hunger bar bit by bit. Once it is full it adds saturation instead.");
        d(language, "hunger_delayed", "Your hunger bar drops more slowly.");
        d(language, "heart_reach", "Hearts are picked up from much further away.");

        // Buffs: mana
        d(language, "mana_regeneration", "Your mana comes back faster.");
        d(language, "star_in_a_bottle", "Your mana comes back faster. Gained by standing near a Star in a Bottle.");

        // Buffs: movement and exploration
        d(language, "water_walking", "Lets you walk on water and other liquids.");
        d(language, "flipper", "Lets you move through water almost as fast as on land.");
        d(language, "builder", "Lets you place and break blocks from further away.");
        d(language, "fishing", "Makes fishing better by raising your fishing power.");
        d(language, "crate", "Crates are more likely to be fished up.");
        d(language, "luck", "Raises your luck, which improves loot. Higher levels give more luck.");
        d(language, "garden_gnome_luck", "A garden gnome nearby gives you a little extra luck.");
        d(language, "shine", "You give off light.");
        d(language, "spelunker", "Highlights ores and treasure near you. Press the key shown on the effect for details.");
        d(language, "hunter", "Highlights enemies near you.");
        d(language, "danger_sense", "Highlights traps and other hazards near you. Press the key shown on the effect for details.");

        // Spawn rate
        d(language, "battle", "More enemies spawn around you, and faster.");
        d(language, "water_candle", "More enemies spawn around you, and faster.");
        d(language, "calm", "Fewer enemies spawn around you, and slower.");
        d(language, "peace_candle", "Fewer enemies spawn around you, and slower.");
        d(language, "happy", "You move faster, and fewer enemies spawn around you.");
        d(language, "shadow_candle", "Takes away the safety of towns: enemies can spawn in them again.");

        // Soulcerer
        d(language, "enhanced_soul", "Raises your soul damage by 12%.");
        d(language, "go_over", "Raises your soul damage by 5%.");
        d(language, "blood_rage", "Raises your melee and ranged damage by 200% for 16 seconds.");
        d(language, "soul_plunder", "Raises your melee and ranged damage by 25%.");
        d(language, "soul_drain", "Soul skills cost no soul for 80 seconds. When it ends you cannot cast soul skills for 160 seconds.");
        d(language, "soul_drain_lock", "You cannot cast soul skills. Left behind when Soul Drain ends, and lasts 160 seconds.");
        d(language, "star_call", "Every 2.5 seconds a falling star hits each of the nearest enemies in range, up to 2, for 11 damage.");
        d(language, "star_link", "When a soul damage buff ends you keep half of its bonus as magic damage, for half as long as the original buff lasted.");
        d(language, "life_return", "Remembers the health you lose. When it ends, 70% of that health comes back as soul.");
        d(language, "soul_buffer", "For 8 seconds, fall damage heals you by the same amount instead of hurting you.");
        d(language, "soul_mark", "Put on an enemy for 10 seconds. Each skill hit on it gives you 1 soul, and killing it gives you 2.");
        d(language, "boiling_blood", "Your next melee hit deals 4 times its normal damage.");
        d(language, "soul_assault", "Keeps turning your soul into bullets while it lasts.");
        d(language, "soul_surge", "Casts Spirit Surge for free every 3 seconds.");

        // Polar bow
        d(language, "polar_mark", "A marked enemy burns the creatures standing next to it. At level 2 it also shoots sunbolts.");
        d(language, "polar_ring", "Gained by hitting a level 2 marked enemy again and again. Meant to make Polar bow arrows apply the mark by themselves.");

        // Damage over time
        d(language, "acid_venom", "You lose 1 health every second.");
        d(language, "cursed_inferno", "You lose 2 health every second.");
        d(language, "frostbite", "You lose 2 health every second and cannot heal naturally.");
        d(language, "frost_burn", "You lose health every second and get colder, and cannot heal naturally. Higher levels hurt more.");
        d(language, "shadowflame", "You lose 3 health every second and cannot heal naturally.");
        d(language, "hellfire", "You lose health every second. When it starts it also blasts nearby enemies.");
        d(language, "blood_butchered", "You lose health every couple of seconds. Higher levels hurt more.");
        d(language, "tentacle_spikes", "You lose health every couple of seconds. Higher levels hurt more.");
        d(language, "electrified", "You take damage every second, much more if you are moving. Standing still hurts less.");

        // Debuffs: healing and defense
        d(language, "bleeding", "You cannot heal naturally.");
        d(language, "dryads_bane", "You cannot heal naturally.");
        d(language, "potion_sickness", "You cannot use another healing item until it wears off.");
        d(language, "ichor", "Lowers your defense by 15.");
        d(language, "broken_armor", "Cuts your defense in half.");
        d(language, "withered_armor", "Cuts your defense in half.");
        d(language, "mana_sickness", "Lowers your magic damage. It starts strong and fades as the time runs out.");

        // Debuffs: control
        d(language, "silenced", "You cannot use items that need mana.");
        d(language, "cursed", "You cannot use or attack with any item.");
        d(language, "frozen", "You cannot move or use items.");
        d(language, "stoned", "You are turned to stone. You cannot move or use items, and you fall faster.");
        d(language, "stun", "You cannot move, jump or attack.");
        d(language, "webbed", "You are stuck in place and cannot jump.");
        d(language, "choking", "You move 30% slower. Drink some water.");
        d(language, "horrified", "The Wall of Flesh has marked you. If you fall behind it or get too far away it drags you in with its tongue.");
        d(language, "the_tongue", "The Wall of Flesh pulls you towards its mouth.");
        d(language, "crimson_storm", "Put on you by the Hill of Flesh. You cannot escape its storm while it lasts.");

        // Debuffs: other
        d(language, "midas", "Enemies with it drop more money when they die.");
        d(language, "stinky", "Lowers your luck a little. Washes off in water.");
        d(language, "love", "Animals that get it are ready to breed.");
        d(language, "demonic_toughts", "A foul influence from the Corruption. Getting it again while it is active spawns an Eater of Souls.");
        d(language, "void_strike", "The target takes more damage, and this grows during the first 5 seconds.");
        d(language, "dimensional_overlap", "Hurts you while you are too deep in the Void Sea. The deeper you are, the more it hurts.");

        // Neutral and cosmetic
        d(language, "shimmer", "You are ethereal after touching Shimmer and fall slowly. It ends when you are out of the blocks.");
        d(language, "sparkle_slime", "You are covered in sparkling slime. Just for looks.");
    }

    private static void d(TranslationConsumer language, String id, String text) {
        language.add("effect.confluence." + id + ".description", text, text);
    }
}
