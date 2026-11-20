package org.confluence.mod.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.confluence.mod.common.block.common.BiomeChestBlock;
import org.confluence.mod.common.block.functional.DeathChestBlock;

/** Only loaded when Lootr is present, see {@link LootrIntegration}. */
final class LootrChestFactory {
    private LootrChestFactory() {
    }

    static void registerEvents() {
        NeoForge.EVENT_BUS.register(LootrBreakHandler.class);
    }

    static BaseChestBlock.BEntity baseChest(BlockPos pos, BlockState state) {
        return new LootrChestEntities.Base(pos, state);
    }

    static BiomeChestBlock.BEntity biomeChest(BlockPos pos, BlockState state) {
        return new LootrChestEntities.Biome(pos, state);
    }

    static DeathChestBlock.BEntity deathChest(BlockPos pos, BlockState state) {
        return new LootrChestEntities.Death(pos, state);
    }
}
