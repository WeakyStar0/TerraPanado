package org.confluence.mod.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.confluence.lib.util.LibUtils;
import org.confluence.mod.common.ModCommonConfigs;
import org.confluence.mod.common.data.saved.KillBoard;
import org.confluence.mod.common.gameevent.GoblinArmyGameEvent;
import org.confluence.mod.common.gameevent.PirateInvasionGameEvent;
import org.confluence.mod.common.init.ModLootTables;
import org.confluence.mod.common.init.entity.BossEntities;
import org.confluence.mod.common.init.item.ModItems;
import org.confluence.mod.common.item.common.CoinItem;
import org.confluence.mod.util.OverworldUtils;

import java.util.List;
import java.util.function.Consumer;

/// Coins dropped by pots. The amount depends on depth, difficulty and how many bosses and events were defeated,
/// and is split into copper, silver, gold and platinum coins.
public class PotMoneyLootEntry extends LootPoolSingletonContainer {
    public static final MapCodec<PotMoneyLootEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.FLOAT.fieldOf("money_ratio").forGetter(entry -> entry.moneyRatio)
    ).and(singletonFields(instance)).apply(instance, PotMoneyLootEntry::new));

    private final float moneyRatio;

    private PotMoneyLootEntry(float moneyRatio, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.moneyRatio = moneyRatio;
    }

    public static LootPoolSingletonContainer.Builder<?> money(float moneyRatio) {
        return simpleBuilder((weight, quality, conditions, functions) -> new PotMoneyLootEntry(moneyRatio, weight, quality, conditions, functions));
    }

    @Override
    public LootPoolEntryType getType() {
        return ModLootTables.EntryTypes.POT_MONEY.get();
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> output, LootContext context) {
        if (!ModCommonConfigs.ENEMY_DROPS_MONEY.get()) return;
        RandomSource random = context.getRandom();
        double y = context.getParam(LootContextParams.ORIGIN).y;
        float roll = random.nextFloat();
        float ratio = 1.0F;
        if (OverworldUtils.inCave(y)) {
            ratio = 1.25F;
        } else if (OverworldUtils.inUnderground(y)) {
            ratio = 0.75F;
        } else if (OverworldUtils.inSurface(y)) {
            ratio = 0.5F;
        } else if (roll < 0.05F) {
            ratio = Mth.nextFloat(random, 1.5F, 2.0F);
        } else if (roll < 0.0625F) {
            ratio = Mth.nextFloat(random, 1.4F, 1.8F);
        } else if (roll < 0.0833F) {
            ratio = Mth.nextFloat(random, 1.2F, 1.4F);
        } else if (roll < 0.125F) {
            ratio = Mth.nextFloat(random, 1.1F, 1.2F);
        } else if (roll < 0.25F) {
            ratio = Mth.nextFloat(random, 1.05F, 1.1F);
        }
        if (LibUtils.isAtLeastExpert(context.getLevel())) {
            ratio *= 2.5F;
            roll = random.nextFloat();
            if (roll < 0.25F) {
                ratio *= 1.25F;
            } else if (roll < 0.5F) {
                ratio *= 1.25F;
            } else {
                ratio *= 1.75F;
            }
        }
        int defeated = KillBoard.INSTANCE.countDefeated(
                BossEntities.EYE_OF_CTHULHU.get(),
                BossEntities.EATER_OF_WORLDS.get(),
                BossEntities.BRAIN_OF_CTHULHU.get(),
                BossEntities.QUEEN_BEE.get(),
                BossEntities.SKELETRON.get(),
                BossEntities.RETINAZER.get(),
                BossEntities.THE_DESTROYER.get(),
                BossEntities.SKELETRON_PRIME.get(),
                BossEntities.PLANTERA.get()
        ) + KillBoard.INSTANCE.countDefeated(
                GoblinArmyGameEvent.KEY,
                PirateInvasionGameEvent.KEY
        );
        for (int i = 0; i < defeated; i++) {
            ratio *= 1.1F;
        }
        ratio *= moneyRatio;
        int amount = (int) Math.ceil(random.nextInt(80, 358) * ratio);
        int copper = amount % CoinItem.UPGRADES_COUNT;
        int i = (amount - copper) / CoinItem.UPGRADES_COUNT;
        int silver = i % CoinItem.UPGRADES_COUNT;
        int j = (i - silver) / CoinItem.UPGRADES_COUNT;
        int gold = j % CoinItem.UPGRADES_COUNT;
        int platinum = (j - gold) / CoinItem.UPGRADES_COUNT;
        accept(output, ModItems.COPPER_COIN.get(), copper);
        accept(output, ModItems.SILVER_COIN.get(), silver);
        accept(output, ModItems.GOLD_COIN.get(), gold);
        accept(output, ModItems.PLATINUM_COIN.get(), platinum);
    }

    private static void accept(Consumer<ItemStack> output, net.minecraft.world.item.Item item, int count) {
        if (count > 0) output.accept(new ItemStack(item, count));
    }
}
