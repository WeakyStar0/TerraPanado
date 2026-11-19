package org.confluence.mod.common.data.gen.loot;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.confluence.mod.common.init.block.ModBlocks;
import org.confluence.mod.common.init.block.PotBlocks;
import org.confluence.mod.common.init.block.TorchBlocks;
import org.confluence.mod.common.init.item.*;
import org.confluence.mod.common.loot.DifficultyChanceLootItemCondition;
import org.confluence.mod.common.loot.PotMoneyLootEntry;
import org.confluence.mod.common.loot.WorldStateLootItemCondition;
import org.confluence.mod.common.loot.WorldStateLootItemCondition.Check;

import static org.confluence.mod.common.init.item.PotionItems.*;

/// What a pot drops when broken. The entity summons (Coin Portal, Powered Creeper) stay in code and run first.
/// The outer alternatives list is tried top to bottom and the first branch that passes wins, which matches the
/// old step-by-step code: key, potion, wormhole, then one of seven equally likely drops with fallbacks.
public final class PotLootTables {
    private PotLootTables() {}

    public static LootTable.Builder build(Block pot, float moneyRatio) {
        AlternativesEntry.Builder sequence = AlternativesEntry.alternatives(
                // 1 in 35, only inside the dungeon
                LootItem.lootTableItem(ToolItems.GOLDEN_DUNGEON_KEY).when(chance(1 / 35F)).when(world(Check.IN_DUNGEON)),
                // 1 in 45 (2 in 45 on Expert) for a potion that fits the depth. The underworld table had one empty
                // slot in 14 that carried on to the next step, so its chance is 13/14 of that.
                potion(Check.IN_UNDERWORLD, 13F / 14F, weighted(
                        w(SPELUNKER_POTION, 1), w(FEATHERFALL_POTION, 1), w(MANA_REGENERATION_POTION, 1), w(OBSIDIAN_SKIN_POTION, 1),
                        w(MAGIC_POWER_POTION, 1), w(INVISIBILITY_POTION, 1), w(HUNTER_POTION, 1), w(GRAVITATION_POTION, 1),
                        w(THORNS_POTION, 1), w(WATER_WALKING_POTION, 1), w(BATTLE_POTION, 1), w(HEART_REACH_POTION, 1), w(TITAN_POTION, 1))),
                potion(Check.IN_CAVE_LAYER, 1F, weighted(
                        w(SPELUNKER_POTION, 1), w(FEATHERFALL_POTION, 1), w(NIGHT_OWL_POTION, 1), w(WATER_WALKING_POTION, 2), w(ARCHERY_POTION, 1),
                        w(GRAVITATION_POTION, 1), w(HUNTER_POTION, 1), w(INVISIBILITY_POTION, 1), w(THORNS_POTION, 1), w(MINING_POTION, 1),
                        w(HEART_REACH_POTION, 1), w(FLIPPER_POTION, 1), w(DANGERSENSE_POTION, 1), w(RECALL_POTION, 1))),
                potion(Check.IN_UNDERGROUND_LAYER, 1F, weighted(
                        w(REGENERATION_POTION, 1), w(SHINE_POTION, 1), w(SWIFTNESS_POTION, 1), w(ARCHERY_POTION, 1), w(GILLS_POTION, 1),
                        w(HUNTER_POTION, 1), w(MINING_POTION, 1), w(DANGERSENSE_POTION, 1), w(RECALL_POTION, 3))),
                potion(Check.IN_SURFACE_LAYER, 1F, weighted(
                        w(IRON_SKIN_POTION, 1), w(SHINE_POTION, 1), w(NIGHT_OWL_POTION, 1), w(SWIFTNESS_POTION, 1), w(MINING_POTION, 1),
                        w(CALMING_POTION, 1), w(BUILDER_POTION, 1), w(RECALL_POTION, 3))),
                // more than one player: 1 in 3
                LootItem.lootTableItem(WORMHOLE_POTION).when(world(Check.MULTIPLAYER)).when(chance(1 / 3F)),
                // otherwise one of seven equally likely drops, each with its own fallbacks
                inline(LootPool.lootPool()
                        .add(inline(heart(pot, moneyRatio)))
                        .add(inline(torch(pot)))
                        .add(inline(ammo()))
                        .add(inline(heal()))
                        .add(inline(bomb(pot, moneyRatio)))
                        .add(inline(rope(moneyRatio)))
                        .add(inline(PotMoneyLootEntry.money(moneyRatio))))
        );
        return LootTable.lootTable().withPool(LootPool.lootPool().add(sequence));
    }

    // ---- branches ----

    private static LootPoolEntryContainer.Builder<?> potion(Check layer, float factor, LootPoolEntryContainer.Builder<?> table) {
        float normal = factor / 45F;
        float expert = factor * 2F / 45F;
        LootItemCondition.Builder chance = () -> new DifficultyChanceLootItemCondition(normal, expert);
        return table.when(chance).when(world(layer));
    }

    /// Nearest player hurt: hearts. Else nearest player short on torches: torches. Else coins.
    private static LootPoolEntryContainer.Builder<?> heart(Block pot, float moneyRatio) {
        LootItemFunction.Builder[] count = {
                SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.5F), true).when(world(Check.EXPERT)),
                SetItemCountFunction.setCount(UniformGenerator.between(0, 1), true).when(InvertedLootItemCondition.invert(world(Check.EXPERT)))
        };
        AlternativesEntry.Builder hearts = AlternativesEntry.alternatives(
                applyAll(LootItem.lootTableItem(ModItems.CANDY_APPLE).when(world(Check.HALLOWEEN)), count),
                applyAll(LootItem.lootTableItem(ModItems.CANDY_CANE).when(world(Check.CHRISTMAS)), count),
                applyAll(LootItem.lootTableItem(ModItems.HEART), count)
        );
        return AlternativesEntry.alternatives(
                hearts.when(world(Check.NEAREST_PLAYER_HURT)),
                torch(pot).when(world(Check.NEAREST_PLAYER_LOW_ON_TORCHES)),
                PotMoneyLootEntry.money(moneyRatio)
        );
    }

    private static AlternativesEntry.Builder torch(Block pot) {
        boolean tundra = pot == PotBlocks.TUNDRA_POT.get();
        LootItemFunction.Builder count = SetItemCountFunction.setCount(tundra ? UniformGenerator.between(2, 6) : UniformGenerator.between(4, 12));
        ItemLike glow = tundra ? ConsumableItems.STICKY_GLOWSTICK : ConsumableItems.GLOWSTICK;
        ItemLike land;
        if (tundra) land = TorchBlocks.ICE_TORCH.asItem();
        else if (pot == PotBlocks.CRIMSON_POT.get()) land = TorchBlocks.CRIMSON_TORCH.asItem();
        else if (pot == PotBlocks.JUNGLE_POT.get()) land = TorchBlocks.JUNGLE_TORCH.asItem();
        else if (pot == PotBlocks.CORRUPTION_POT.get()) land = TorchBlocks.CORRUPT_TORCH.asItem();
        else if (pot == PotBlocks.UNDERGROUND_DESERT_POT.get()) land = TorchBlocks.DESERT_TORCH.asItem();
        else land = Items.TORCH;
        return AlternativesEntry.alternatives(
                LootItem.lootTableItem(glow).when(world(Check.IN_WATER)).apply(count),
                LootItem.lootTableItem(land).apply(count)
        );
    }

    private static AlternativesEntry.Builder ammo() {
        LootItemFunction.Builder count = SetItemCountFunction.setCount(UniformGenerator.between(10, 20));
        LootItemCondition.Builder hardmode = world(Check.HARDMODE);
        return AlternativesEntry.alternatives(
                LootItem.lootTableItem(ConsumableItems.GRENADE).when(hardmode).when(chance(0.5F)).apply(count),
                LootItem.lootTableItem(ConsumableItems.SHURIKEN).when(InvertedLootItemCondition.invert(hardmode)).when(chance(0.5F)).apply(count),
                LootItem.lootTableItem(ArrowItems.HELLFIRE_ARROW).when(world(Check.IN_UNDERWORLD)).apply(count),
                LootItem.lootTableItem(ArrowItems.UNHOLY_ARROW).when(hardmode).when(chance(0.5F)).apply(count),
                LootItem.lootTableItem(GunItems.SILVER_BULLET).when(hardmode).when(chance(0.5F)).apply(count),
                LootItem.lootTableItem(GunItems.TUNGSTEN_BULLET).when(hardmode).apply(count),
                LootItem.lootTableItem(Items.ARROW).apply(count)
        );
    }

    private static AlternativesEntry.Builder heal() {
        LootItemFunction.Builder extra = SetItemCountFunction.setCount(ConstantValue.exactly(1), true)
                .when(AllOfCondition.allOf(world(Check.EXPERT), chance(1 / 3F)));
        return AlternativesEntry.alternatives(
                LootItem.lootTableItem(PotionItems.HEALING_POTION).when(underworldOrHardmode()).apply(extra),
                LootItem.lootTableItem(PotionItems.LESSER_HEALING_POTION).apply(extra)
        );
    }

    private static LootPoolEntryContainer.Builder<?> bomb(Block pot, float moneyRatio) {
        LootItemFunction.Builder[] count = {
                SetItemCountFunction.setCount(UniformGenerator.between(1, 4)).when(world(Check.EXPERT)),
                SetItemCountFunction.setCount(UniformGenerator.between(1, 7)).when(InvertedLootItemCondition.invert(world(Check.EXPERT)))
        };
        if (pot == PotBlocks.UNDERGROUND_DESERT_POT.get()) {
            return applyAll(LootItem.lootTableItem(ConsumableItems.SCARAB_BOMB), count);
        }
        return AlternativesEntry.alternatives(
                applyAll(LootItem.lootTableItem(ConsumableItems.BOMB).when(world(Check.IN_OVERWORLD)), count),
                rope(moneyRatio)
        );
    }

    private static AlternativesEntry.Builder rope(float moneyRatio) {
        return AlternativesEntry.alternatives(
                PotMoneyLootEntry.money(moneyRatio).when(underworldOrHardmode()),
                LootItem.lootTableItem(ModBlocks.ROPE.asItem()).apply(SetItemCountFunction.setCount(UniformGenerator.between(20, 40)))
        );
    }

    // ---- helpers ----

    private static LootPoolSingletonContainer.Builder<?> applyAll(LootPoolSingletonContainer.Builder<?> entry, LootItemFunction.Builder... functions) {
        for (LootItemFunction.Builder function : functions) entry.apply(function);
        return entry;
    }

    private static LootItemCondition.Builder underworldOrHardmode() {
        return AnyOfCondition.anyOf(world(Check.IN_UNDERWORLD), world(Check.HARDMODE));
    }

    private static LootPoolSingletonContainer.Builder<?> inline(LootPoolEntryContainer.Builder<?> entry) {
        return inline(LootPool.lootPool().add(entry));
    }

    private static LootPoolSingletonContainer.Builder<?> inline(LootPool.Builder pool) {
        return NestedLootTable.inlineLootTable(LootTable.lootTable().withPool(pool).build());
    }

    private record W(ItemLike item, int weight) {}

    private static W w(ItemLike item, int weight) {
        return new W(item, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> weighted(W... entries) {
        LootPool.Builder pool = LootPool.lootPool();
        for (W entry : entries) pool.add(LootItem.lootTableItem(entry.item()).setWeight(entry.weight()));
        return inline(pool);
    }

    private static LootItemCondition.Builder chance(float probability) {
        return LootItemRandomChanceCondition.randomChance(probability);
    }

    private static LootItemCondition.Builder world(Check check) {
        return WorldStateLootItemCondition.builder(check);
    }
}
