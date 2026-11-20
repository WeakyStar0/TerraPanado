package org.confluence.mod.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.DefaultLootFiller;
import noobanidus.mods.lootr.common.api.data.LootFiller;
import noobanidus.mods.lootr.common.api.data.SimpleLootrInstance;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.confluence.mod.common.block.common.BiomeChestBlock;
import org.confluence.mod.common.block.functional.DeathChestBlock;
import org.confluence.mod.common.init.item.VanityArmorItems;
import org.jetbrains.annotations.Nullable;

/**
 * The per-player loot versions of the Confluence chest block entities. Same behaviour as Lootr's own chest,
 * but the blocks, keys, textures and trap logic stay Confluence's.
 */
public final class LootrChestEntities {
    private LootrChestEntities() {
    }

    public static final class Base extends BaseChestBlock.BEntity implements LootrChest {
        private final SimpleLootrInstance lootr = new SimpleLootrInstance(this::getVisualOpeners, 27);

        public Base(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public SimpleLootrInstance lootrInstance() {
            return lootr;
        }

        @Override
        protected boolean lootrActive() {
            return LootrChestHooks.active(this);
        }

        @Override
        protected boolean lootrSyncsToClient() {
            return true;
        }

        @Override
        protected void lootrLoad(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.loadAdditional(tag, registries);
        }

        @Override
        protected void lootrSave(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.saveAdditional(tag, registries, level != null && level.isClientSide());
        }

        @Override
        protected void lootrFillUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.fillUpdateTag(tag, registries, level != null && level.isClientSide());
        }

        @Override
        public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
            lootr.setSavingToItem(true);
            super.saveToItem(stack, registries);
            lootr.setSavingToItem(false);
        }

        @Override
        public void unpackLootTable(@Nullable Player player) {
            if (!isLootrChest()) super.unpackLootTable(player);
        }

        @Override
        public boolean hasLootrOpened(Player player) {
            return hasClientOpened(player);
        }

        @Override
        public void lootrInteract(ServerPlayer player) {
            LootrChestHooks.interact(this, player);
        }

        @Override
        public void lootrTick(Level level, BlockPos pos, BlockState state) {
            if (isLootrChest()) defaultTick(level, pos, state);
        }

        @Override
        public float lootrDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, float progress) {
            return LootrChestHooks.destroyProgress(state, player, level, pos, progress);
        }

        @Override
        public float lootrExplosionResistance(float resistance) {
            return LootrChestHooks.explosionResistance(this, resistance);
        }

        @Override
        public boolean lootrBlastImmune() {
            return LootrAPI.isBlastImmune();
        }

        @Override
        public int lootrComparatorSignal(BlockState state, Level level, BlockPos pos) {
            return LootrChestHooks.comparatorSignal(state, level, pos);
        }

        @Override
        public void lootrPlayerDestroyed(Level level, Player player, BlockPos pos) {
            LootrChestHooks.playerDestroyed(level, player, pos, this);
        }

        @Override
        public boolean ownsMenu(Container menuContainer) {
            return LootrChestHooks.ownsMenu(this, menuContainer);
        }
    }

    public static final class Biome extends BiomeChestBlock.BEntity implements LootrChest {
        private final SimpleLootrInstance lootr = new SimpleLootrInstance(this::getVisualOpeners, 27);

        public Biome(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public SimpleLootrInstance lootrInstance() {
            return lootr;
        }

        @Override
        protected boolean lootrActive() {
            return LootrChestHooks.active(this);
        }

        @Override
        protected boolean lootrSyncsToClient() {
            return true;
        }

        @Override
        protected void lootrLoad(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.loadAdditional(tag, registries);
        }

        @Override
        protected void lootrSave(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.saveAdditional(tag, registries, level != null && level.isClientSide());
        }

        @Override
        protected void lootrFillUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.fillUpdateTag(tag, registries, level != null && level.isClientSide());
        }

        @Override
        public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
            lootr.setSavingToItem(true);
            super.saveToItem(stack, registries);
            lootr.setSavingToItem(false);
        }

        @Override
        public void unpackLootTable(@Nullable Player player) {
            if (!isLootrChest()) super.unpackLootTable(player);
        }

        @Override
        public boolean hasLootrOpened(Player player) {
            return hasClientOpened(player);
        }

        @Override
        public void lootrInteract(ServerPlayer player) {
            LootrChestHooks.interact(this, player);
        }

        @Override
        public void lootrTick(Level level, BlockPos pos, BlockState state) {
            if (isLootrChest()) defaultTick(level, pos, state);
        }

        @Override
        public float lootrDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, float progress) {
            return LootrChestHooks.destroyProgress(state, player, level, pos, progress);
        }

        @Override
        public float lootrExplosionResistance(float resistance) {
            return LootrChestHooks.explosionResistance(this, resistance);
        }

        @Override
        public boolean lootrBlastImmune() {
            return LootrAPI.isBlastImmune();
        }

        @Override
        public int lootrComparatorSignal(BlockState state, Level level, BlockPos pos) {
            return LootrChestHooks.comparatorSignal(state, level, pos);
        }

        @Override
        public void lootrPlayerDestroyed(Level level, Player player, BlockPos pos) {
            LootrChestHooks.playerDestroyed(level, player, pos, this);
        }

        @Override
        public boolean ownsMenu(Container menuContainer) {
            return LootrChestHooks.ownsMenu(this, menuContainer);
        }
    }

    public static final class Death extends DeathChestBlock.BEntity implements LootrChest {
        private final SimpleLootrInstance lootr = new SimpleLootrInstance(this::getVisualOpeners, 27);

        public Death(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public SimpleLootrInstance lootrInstance() {
            return lootr;
        }

        @Override
        protected boolean lootrActive() {
            return LootrChestHooks.active(this);
        }

        @Override
        protected boolean lootrSyncsToClient() {
            return true;
        }

        @Override
        protected void lootrLoad(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.loadAdditional(tag, registries);
        }

        @Override
        protected void lootrSave(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.saveAdditional(tag, registries, level != null && level.isClientSide());
        }

        @Override
        protected void lootrFillUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
            lootr.fillUpdateTag(tag, registries, level != null && level.isClientSide());
        }

        @Override
        public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
            lootr.setSavingToItem(true);
            super.saveToItem(stack, registries);
            lootr.setSavingToItem(false);
        }

        /** The trap chest always holds a Dead Man's Sweater, so every player's own loot gets one too. */
        @Override
        public LootFiller getDefaultFiller() {
            return (provider, player, container) -> {
                container.setItem(0, VanityArmorItems.DEAD_MANS_SWEATER.get().getDefaultInstance());
                DefaultLootFiller.getInstance().unpackLootTable(provider, player, container);
            };
        }

        @Override
        public void unpackLootTable(@Nullable Player player) {
            if (!isLootrChest()) super.unpackLootTable(player);
        }

        @Override
        public boolean hasLootrOpened(Player player) {
            return hasClientOpened(player);
        }

        @Override
        public void lootrInteract(ServerPlayer player) {
            LootrChestHooks.interact(this, player);
        }

        @Override
        public void lootrTick(Level level, BlockPos pos, BlockState state) {
            if (isLootrChest()) defaultTick(level, pos, state);
        }

        @Override
        public float lootrDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, float progress) {
            return LootrChestHooks.destroyProgress(state, player, level, pos, progress);
        }

        @Override
        public float lootrExplosionResistance(float resistance) {
            return LootrChestHooks.explosionResistance(this, resistance);
        }

        @Override
        public boolean lootrBlastImmune() {
            return LootrAPI.isBlastImmune();
        }

        @Override
        public int lootrComparatorSignal(BlockState state, Level level, BlockPos pos) {
            return LootrChestHooks.comparatorSignal(state, level, pos);
        }

        @Override
        public void lootrPlayerDestroyed(Level level, Player player, BlockPos pos) {
            LootrChestHooks.playerDestroyed(level, player, pos, this);
        }

        @Override
        public boolean ownsMenu(Container menuContainer) {
            return LootrChestHooks.ownsMenu(this, menuContainer);
        }
    }
}
