package org.confluence.mod.common.block.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.confluence.lib.util.LibEntityUtils;
import org.confluence.mod.common.entity.CoinPortalEntity;
import org.confluence.mod.common.worldgen.secret_seed.ForTheWorthy;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;


public class BasePotBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final VoxelShape voxelShape;
    private final float moneyRatio;
    private final IntSupplier invMoneyHoleChance;

    public BasePotBlock(float moneyRatio, IntSupplier invMoneyHoleChance, VoxelShape voxelShape) {
        super(Properties.of().sound(SoundType.DECORATED_POT).instabreak().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.voxelShape = voxelShape;
        this.moneyRatio = moneyRatio;
        this.invMoneyHoleChance = invMoneyHoleChance;
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public @Nullable PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.BLOCKED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return voxelShape;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        dropSequence(level, pos);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        dropSequence(level, pos);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockPos = hit.getBlockPos();
        Entity entity = LibEntityUtils.getOwner(projectile.getOwner());
        if (level.destroyBlock(blockPos, true, entity)) {
            if (entity instanceof Player player) {
                player.awardStat(Stats.BLOCK_MINED.get(this));
            }
            dropSequence(level, blockPos);
        }
    }

    private void dropSequence(Level level, BlockPos blockPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        Vec3 center = blockPos.getCenter();
        if (summonHole(serverLevel, center)) return;
        if (ForTheWorthy.summonPoweredCreeper(serverLevel, blockPos)) return;
        // everything else is in the pot's loot table (data/<namespace>/loot_table/blocks/<pot>.json)
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, center)
                .withParameter(LootContextParams.BLOCK_STATE, defaultBlockState())
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .create(LootContextParamSets.BLOCK);
        LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(getLootTable());
        table.getRandomItems(params, stack -> LibEntityUtils.createItemEntity(stack, center, serverLevel, 0));
    }

    public float getMoneyRatio() {
        return moneyRatio;
    }

    private boolean summonHole(ServerLevel level, Vec3 center) {
        int i = invMoneyHoleChance.getAsInt();
        if (i > 0 && level.random.nextInt(i) == 0) {
            CoinPortalEntity moneyHole = new CoinPortalEntity(level, center);
            moneyHole.setDeltaMovement(0.0, 0.2, 0.0);
            level.addFreshEntity(moneyHole);
            return true;
        }
        return false;
    }
}
