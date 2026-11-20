package org.confluence.mod.common.block.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.confluence.lib.common.block.StateProperties;
import org.confluence.mod.common.init.block.ChestBlocks;
import org.confluence.mod.integration.lootr.LootrIntegration;
import org.confluence.mod.util.AchievementUtils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class BiomeChestBlock extends ChestBlock {
    public static final BooleanProperty UNLOCKED = StateProperties.UNLOCKED;
    private final Predicate<ItemStack> isKey;

    public BiomeChestBlock(Predicate<ItemStack> isKey) {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).explosionResistance(18000), ChestBlocks.BIOME_CHEST_ENTITY::get);
        this.isKey = isKey;
        registerDefaultState(defaultBlockState().setValue(UNLOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(UNLOCKED));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return LootrIntegration.biomeChest(pos, state);
    }

    @Override
    protected @Nullable Direction candidatePartnerFacing(BlockPlaceContext context, Direction direction) {
        return null; // 不能合并
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
            return state.setValue(UNLOCKED, true);
        }
        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && isKey.test(stack) && !state.getValue(UNLOCKED)) {
            level.setBlock(pos, state.setValue(UNLOCKED, true), Block.UPDATE_ALL);
            serverPlayer.level().playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS);
            double posX = pos.getX() + 0.5;
            double posZ = pos.getZ() + 0.5;
            serverPlayer.serverLevel().sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHAIN.defaultBlockState()),
                    posX, pos.getY() + 0.5, posZ, 200, 0.0625, 0.0625, 0.0625, 0.15
            );
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            AchievementUtils.awardAchievement(serverPlayer, "big_booty");
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        return state.getValue(UNLOCKED)
                && !(level.getBlockEntity(pos) instanceof BaseChestBlock.BEntity chest && chest.isLootrChest() && chest.lootrBlastImmune());
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BaseChestBlock.BEntity chest && chest.isLootrChest()) {
            return chest.lootrComparatorSignal(state, level, pos);
        }
        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof BaseChestBlock.BEntity chest && chest.isLootrChest()) chest.lootrPlayerDestroyed(level, player, pos);
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!state.getValue(UNLOCKED)) return 0;
        float progress = super.getDestroyProgress(state, player, level, pos);
        if (level.getBlockEntity(pos) instanceof BaseChestBlock.BEntity chest && chest.isLootrChest()) {
            return chest.lootrDestroyProgress(state, player, level, pos, progress);
        }
        return progress;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = BaseChestBlock.useLootrChest(level, pos, player);
        return result != null ? result : super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BaseChestBlock.wrapLootrTicker(super.getTicker(level, state, type));
    }

    public static class BEntity extends BaseChestBlock.BEntity {
        public BEntity(BlockPos pos, BlockState blockState) {
            super(ChestBlocks.BIOME_CHEST_ENTITY.get(), pos, blockState);
        }

        @Override
        protected Component getDefaultName() {
            return Component.translatable("container.confluence." + BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath());
        }
    }
}
