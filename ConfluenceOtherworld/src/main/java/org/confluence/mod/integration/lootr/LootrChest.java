package org.confluence.mod.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.BuiltInLootrTypes;
import noobanidus.mods.lootr.common.api.ILootrType;
import noobanidus.mods.lootr.common.api.advancement.IContainerTrigger;
import noobanidus.mods.lootr.common.api.data.LootrBlockType;
import noobanidus.mods.lootr.common.api.data.SimpleLootrInstance;
import noobanidus.mods.lootr.common.api.data.blockentity.ILootrBlockEntity;
import noobanidus.mods.lootr.common.api.registry.LootrRegistry;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Makes a Confluence chest block entity a Lootr container (same contract as Lootr's own chest block entity).
 * Every implementor extends {@link BaseChestBlock.BEntity}.
 */
public interface LootrChest extends ILootrBlockEntity {
    SimpleLootrInstance lootrInstance();

    private RandomizableContainerBlockEntity self() {
        return (RandomizableContainerBlockEntity) this;
    }

    @Deprecated
    @Override
    default LootrBlockType getInfoBlockType() {
        return LootrBlockType.CHEST;
    }

    @Override
    default ILootrType getInfoNewType() {
        return BuiltInLootrTypes.CHEST;
    }

    @Override
    default void markChanged() {
        self().setChanged();
        markDataChanged();
    }

    @NotNull
    @Override
    default UUID getInfoUUID() {
        return lootrInstance().getInfoUUID();
    }

    @Override
    default String getInfoKey() {
        return lootrInstance().getInfoKey();
    }

    @Override
    default boolean hasBeenOpened() {
        return lootrInstance().hasBeenOpened();
    }

    @Override
    default void setHasBeenOpened(boolean value) {
        lootrInstance().setHasBeenOpened(value);
    }

    @Override
    default boolean isPhysicallyOpen() {
        return ((ChestBlockEntity) this).getOpenNess(1.0F) > 0.0F;
    }

    @Override
    default int getPhysicalOpenerCount() {
        return ((BaseChestBlock.BEntity) this).openerCount();
    }

    @Nullable
    @Override
    default Set<UUID> getClientOpeners() {
        return lootrInstance().getClientOpeners();
    }

    @Override
    default boolean isClientOpened() {
        return lootrInstance().isClientOpened();
    }

    @Override
    default void setClientOpened(boolean opened) {
        lootrInstance().setClientOpened(opened);
    }

    @NotNull
    @Override
    default BlockPos getInfoPos() {
        return self().getBlockPos();
    }

    @Override
    default ResourceKey<LootTable> getInfoLootTable() {
        return self().getLootTable();
    }

    @Nullable
    @Override
    default Component getInfoDisplayName() {
        return self().getDisplayName();
    }

    @NotNull
    @Override
    default ResourceKey<Level> getInfoDimension() {
        return self().getLevel().dimension();
    }

    @Override
    default int getInfoContainerSize() {
        return lootrInstance().getInfoContainerSize();
    }

    @Override
    default long getInfoLootSeed() {
        return self().getLootTableSeed();
    }

    @Nullable
    @Override
    default NonNullList<ItemStack> getInfoReferenceInventory() {
        return lootrInstance().getCustomInventory();
    }

    @Override
    default void setInfoReferenceInventory(NonNullList<ItemStack> reference) {
        lootrInstance().setCustomInventory(reference);
    }

    @Override
    default boolean isInfoReferenceInventory() {
        return isInfoReferenceInventoryInternal(lootrInstance().isCustomInventory());
    }

    @Override
    default Level getInfoLevel() {
        return self().getLevel();
    }

    @Nullable
    @Override
    default IContainerTrigger getTrigger() {
        return LootrRegistry.getChestTrigger();
    }

    /** A decayed chest disappears without dropping itself, whatever the replace_when_decayed setting says. */
    @Override
    default void performDecay() {
        Level level = getInfoLevel();
        if (level != null && !level.isClientSide()) level.destroyBlock(getInfoPos(), false);
    }

    @Override
    default int getRandomOffset() {
        return lootrInstance().getRandomOffset();
    }

    @Override
    default void setClientRefreshing(boolean value) {
        lootrInstance().setClientRefreshing(value);
    }

    @Override
    default void setClientDecaying(boolean value) {
        lootrInstance().setClientDecaying(value);
    }

    @Override
    default boolean isClientRefreshing() {
        return lootrInstance().isClientRefreshing();
    }

    @Override
    default boolean isClientDecaying() {
        return lootrInstance().isClientDecaying();
    }
}
