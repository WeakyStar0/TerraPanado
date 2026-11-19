package org.confluence.mod.common.data.gen.language;

import org.confluence.mod.common.data.TranslationConsumer;

/// Text for the NPC shop recipe viewer page and for the shop conditions shown on it.
/// Only English is written; the same text is used for the Chinese file.
public final class NPCShopLanguageSubProvider {
    private NPCShopLanguageSubProvider() {}

    public static void addTranslations(TranslationConsumer language) {
        tr(language, "title.confluence.npc_shop", "NPC Shop");
        tr(language, "jei.confluence.npc_shop.sold_by", "Sold by %s");
        tr(language, "jei.confluence.npc_shop.price", "Price: %s");
        tr(language, "jei.confluence.npc_shop.price_mood", "The price changes with the NPC's mood.");
        tr(language, "jei.confluence.npc_shop.requires", "Requires: %s");
        tr(language, "jei.confluence.npc_shop.random_stock", "Only some of this NPC's goods are offered on each visit.");

        tr(language, "trade_condition.confluence.unknown", "A special condition");
        tr(language, "trade_condition.confluence.hardmode", "Hardmode");
        tr(language, "trade_condition.confluence.artisan_loaf_unused", "Artisan Loaf not eaten yet");
        tr(language, "trade_condition.confluence.any_boss_defeated", "Any boss defeated");
        tr(language, "trade_condition.confluence.boss_defeated.named", "%s defeated");
        tr(language, "trade_condition.confluence.biome", "In certain biomes");
        tr(language, "trade_condition.confluence.time", "At certain times of day");
        tr(language, "trade_condition.confluence.kill_entity.named", "You have killed %s");
        tr(language, "trade_condition.confluence.mood", "Depends on the NPC's mood");
        tr(language, "trade_condition.confluence.npc_nearby.named", "%s lives nearby");
        tr(language, "trade_condition.confluence.bestiary.count", "Bestiary has %s or more entries");
        tr(language, "trade_condition.confluence.date", "Only around certain dates");
        tr(language, "trade_condition.confluence.position_height", "At certain heights");
        tr(language, "trade_condition.confluence.fluid", "While in a certain fluid");
        tr(language, "trade_condition.confluence.attack_target", "While the NPC has a target");
        tr(language, "trade_condition.confluence.npc_item_in_hand", "While the NPC holds a certain item");
        tr(language, "trade_condition.confluence.weather", "In certain weather");
        tr(language, "trade_condition.confluence.dimension", "In a certain dimension");
        tr(language, "trade_condition.confluence.game_event.named", "During %s");
        tr(language, "trade_condition.confluence.graveyard", "In a graveyard");
        tr(language, "trade_condition.confluence.moon_phase", "During certain moon phases");
        tr(language, "trade_condition.confluence.world_flag", "In a world with a special seed");
        tr(language, "trade_condition.confluence.not.of", "Not: %s");
    }

    private static void tr(TranslationConsumer language, String key, String text) {
        language.add(key, text, text);
    }
}
