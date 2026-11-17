package org.confluence.mod.common.block.natural.spreadable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.confluence.mod.common.block.conversion.ConversionTable;
import org.confluence.mod.common.block.conversion.natural.*;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.IntFunction;

/// [生物群系蔓延](https://terraria.wiki.gg/zh/wiki/%E7%94%9F%E7%89%A9%E7%BE%A4%E7%B3%BB%E8%94%93%E5%BB%B6)
public interface ISpreadable {
    // That was a joke haha!
    BooleanProperty STILL_ALIVE = BooleanProperty.create("still_alive");

    Type getSpreadType();

    /// Passive biome spreading is removed; conversion now only happens through solutions, powders and the Hardmode conversion.
    default void spread(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource randomSource) {
    }

    default boolean isFullBlock(ServerLevel level, BlockPos pos) {
        return isFullBlock(level.getBlockState(pos), level, pos);
    }

    default boolean isFullBlock(BlockState state, ServerLevel level, BlockPos pos) {
        return state.isSolidRender(level, pos);
    }

    enum Type implements StringRepresentable {
        HALLOW(new TheHallowConversionTable()),
        CRIMSON(new TheCrimsonConversionTable()),
        CORRUPT(new TheCorruptionConversionTable()),
        GLOWING(new GlowingMushroomConversionTable()),
        JUNGLE(new JungleConversionTable()),
        PURE(new PureConversionTable()),
        ASH(new AshConversionTable());

        private static final IntFunction<Type> BY_ID = ByIdMap.continuous(Type::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        private final ConversionTable conversionTable;

        Type(ConversionTable conversionTable) {
            this.conversionTable = conversionTable;
        }

        public BlockState getNotNull(BlockState source, boolean hardmode) {
            BlockState target = conversionTable.get(source, hardmode);
            return target == null ? source : target;
        }

        public @Nullable BlockState getNullable(BlockState source, boolean hardmode) {
            return conversionTable.get(source, hardmode);
        }

        public boolean spread(Level level, BlockPos pos, boolean hardmode) {
            BlockState target = getNullable(level.getBlockState(pos), hardmode);
            if (target != null) {
                return level.setBlockAndUpdate(pos, target);
            }
            return false;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Type byId(int pId) {
            return BY_ID.apply(pId);
        }
    }
}
