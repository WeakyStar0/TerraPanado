package org.confluence.mod.common.block.common;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.confluence.lib.common.block.StateProperties;
import org.confluence.mod.common.init.block.ChestBlocks;
import org.confluence.mod.integration.lootr.LootrIntegration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BaseChestBlock extends ChestBlock {
    public static final BooleanProperty UNLOCKED = StateProperties.UNLOCKED;
    @Nullable
    private final Key key;

    public BaseChestBlock(@Nullable Key key) {
        this(Properties.ofFullCopy(Blocks.CHEST), ChestBlocks.BASE_CHEST_ENTITY::get, key);
    }

    public BaseChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, @Nullable Key key) {
        super(properties, supplier);
        this.key = key;
        registerDefaultState(defaultBlockState().setValue(UNLOCKED, key == null));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(UNLOCKED));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return LootrIntegration.baseChest(pPos, pState);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = asItem().getDefaultInstance();
        setupComponent(stack, state.getValue(UNLOCKED));
        return Collections.singletonList(stack);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        float resistance = state.getValue(UNLOCKED) ? super.getExplosionResistance(state, level, pos, explosion) : 18000;
        if (level.getBlockEntity(pos) instanceof BEntity chest && chest.isLootrChest()) {
            return chest.lootrExplosionResistance(resistance);
        }
        return resistance;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        return state.getValue(UNLOCKED) && super.canEntityDestroy(state, level, pos, entity)
                && !(level.getBlockEntity(pos) instanceof BEntity chest && chest.isLootrChest() && chest.lootrBlastImmune());
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BEntity chest && chest.isLootrChest()) {
            return chest.lootrComparatorSignal(state, level, pos);
        }
        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof BEntity chest && chest.isLootrChest()) chest.lootrPlayerDestroyed(level, player, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = useLootrChest(level, pos, player);
        return result != null ? result : super.useWithoutItem(state, level, pos, player, hitResult);
    }

    /**
     * Opens a per-player (Lootr) chest. Returns null when the chest at {@code pos} is a normal chest.
     */
    @Nullable
    public static InteractionResult useLootrChest(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof BEntity chest) || !chest.isLootrChest()) return null;
        if (!level.isClientSide() && !player.isSpectator() && player instanceof ServerPlayer serverPlayer) {
            chest.lootrInteract(serverPlayer);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return wrapLootrTicker(super.getTicker(level, state, type));
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> wrapLootrTicker(@Nullable BlockEntityTicker<T> vanilla) {
        return (level, pos, state, blockEntity) -> {
            if (vanilla != null) vanilla.tick(level, pos, state, blockEntity);
            if (blockEntity instanceof BEntity chest) chest.lootrTick(level, pos, state);
        };
    }

    @Nullable
    @Override
    protected Direction candidatePartnerFacing(BlockPlaceContext context, Direction direction) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        if (blockstate.is(this) && blockstate.getValue(UNLOCKED) == unlocked(context.getItemInHand())) {
            return blockstate.getValue(TYPE) == ChestType.SINGLE ? blockstate.getValue(FACING) : null;
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean unlocked = unlocked(context.getItemInHand());
        if (!unlocked && context.getPlayer() != null && context.getPlayer().isCreative()) {
            unlocked = true;
        }
        return super.getStateForPlacement(context).setValue(UNLOCKED, unlocked);
    }

    protected boolean unlocked(ItemStack stack) {
        return stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).properties().getOrDefault("unlocked", "true").equals("true");
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!state.getValue(UNLOCKED)) {
            if (key != null && !key.useKeyOn(stack, state, level, pos, player, hand, hitResult)) {
                return ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
        setupComponent(stack, state.getValue(UNLOCKED));
        return stack;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!state.getValue(UNLOCKED)) return 0;
        float progress = super.getDestroyProgress(state, player, level, pos);
        if (level.getBlockEntity(pos) instanceof BEntity chest && chest.isLootrChest()) {
            return chest.lootrDestroyProgress(state, player, level, pos, progress);
        }
        return progress;
    }

    public static void setupComponent(ItemStack stack, boolean unlocked) {
        stack.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("unlocked", unlocked ? "true" : "false")));
    }

    public static class BItem extends BlockItem {
        public BItem(Block block, Properties properties) {
            super(block, properties.component(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.of("unlocked", "true"))));
        }

        @Override
        public void onCraftedPostProcess(ItemStack stack, Level level) {
            setupComponent(stack, true);
        }
    }

    public static class BEntity extends ChestBlockEntity {
        private static final String LOOTR_SYNC_KEY = "ConfluenceLootrChest";
        private boolean lootrChestClient;

        public BEntity(BlockPos pPos, BlockState pBlockState) {
            this(ChestBlocks.BASE_CHEST_ENTITY.get(), pPos, pBlockState);
        }

        public BEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
            super(pType, pPos, pBlockState);
        }

        @Override
        protected Component getDefaultName() {
            return getBlockState().getBlock().getName();
        }

        @Override
        public boolean canOpen(Player pPlayer) {
            return getBlockState().getValue(UNLOCKED) && super.canOpen(pPlayer);
        }

        @Override
        public boolean canTakeItem(Container target, int slot, ItemStack stack) {
            return getBlockState().getValue(UNLOCKED) && !isLootrChest();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return getBlockState().getValue(UNLOCKED) && !isLootrChest();
        }

        // ---- Lootr (per-player loot) extension points. The Lootr-aware subclasses live in integration.lootr ----

        /**
         * True when this chest hands out per-player loot: it has a loot table and Lootr is active for it.
         * Chests placed by players have no loot table and always stay normal chests.
         */
        public final boolean isLootrChest() {
            Level level = getLevel();
            if (level == null) return false;
            return level.isClientSide() ? lootrChestClient : lootrActive();
        }

        /** Server side check, only the Lootr subclasses return true. */
        protected boolean lootrActive() {
            return false;
        }

        protected boolean lootrSyncsToClient() {
            return false;
        }

        protected void lootrLoad(CompoundTag tag, HolderLookup.Provider registries) {
        }

        protected void lootrSave(CompoundTag tag, HolderLookup.Provider registries) {
        }

        protected void lootrFillUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        }

        /** Has the given (client) player already looted this chest? Used for the grayscale texture. */
        public boolean hasLootrOpened(Player player) {
            return false;
        }

        public void lootrInteract(ServerPlayer player) {
        }

        public void lootrTick(Level level, BlockPos pos, BlockState state) {
        }

        public float lootrDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, float progress) {
            return progress;
        }

        public float lootrExplosionResistance(float resistance) {
            return resistance;
        }

        public boolean lootrBlastImmune() {
            return false;
        }

        public int lootrComparatorSignal(BlockState state, Level level, BlockPos pos) {
            return 0;
        }

        public void lootrPlayerDestroyed(Level level, Player player, BlockPos pos) {
        }

        public final int openerCount() {
            return openersCounter.getOpenerCount();
        }

        /** Used by the open counter so the chest lid stays open while a per-player inventory is open. */
        public boolean ownsMenu(Container menuContainer) {
            return false;
        }

        /** Reads the Lootr data of a save or update tag. Death chests call this from their own update handling. */
        protected final void readLootrSync(CompoundTag tag, HolderLookup.Provider registries) {
            if (tag.contains(LOOTR_SYNC_KEY)) lootrChestClient = tag.getBoolean(LOOTR_SYNC_KEY);
            lootrLoad(tag, registries);
        }

        protected final void writeLootrSync(CompoundTag tag, HolderLookup.Provider registries) {
            if (!lootrSyncsToClient()) return;
            boolean lootrChest = isLootrChest();
            tag.putBoolean(LOOTR_SYNC_KEY, lootrChest);
            if (lootrChest) lootrFillUpdateTag(tag, registries);
        }

        @Override
        public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            readLootrSync(tag, registries);
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            if (getLootTable() != null) lootrSave(tag, registries); // chests without a loot table stay plain chests
        }

        @Override
        public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
            CompoundTag tag = super.getUpdateTag(registries);
            writeLootrSync(tag, registries);
            return tag;
        }

        @Nullable
        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return lootrSyncsToClient() ? ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag) : null;
        }
    }

    @FunctionalInterface
    public interface Key {
        boolean useKeyOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult);
    }
}
