package org.confluence.mod.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.LoadingModList;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.confluence.mod.common.block.common.BiomeChestBlock;
import org.confluence.mod.common.block.functional.DeathChestBlock;

/**
 * Entry points that pick the Lootr aware chest block entities when Lootr is installed.
 * This class must not reference any Lootr class, the Lootr aware ones are only touched through {@link LootrChestFactory}.
 */
public final class LootrIntegration {
    public static final String MOD_ID = "lootr";
    private static Boolean loaded;

    private LootrIntegration() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            LoadingModList list = LoadingModList.get();
            if (list == null) return false; // too early, do not cache
            loaded = list.getModFileById(MOD_ID) != null;
        }
        return loaded;
    }

    /** Registers the game event handlers that need Lootr. Call once during mod construction. */
    public static void init() {
        if (isLoaded()) LootrChestFactory.registerEvents();
    }

    public static BaseChestBlock.BEntity baseChest(BlockPos pos, BlockState state) {
        return isLoaded() ? LootrChestFactory.baseChest(pos, state) : new BaseChestBlock.BEntity(pos, state);
    }

    public static BiomeChestBlock.BEntity biomeChest(BlockPos pos, BlockState state) {
        return isLoaded() ? LootrChestFactory.biomeChest(pos, state) : new BiomeChestBlock.BEntity(pos, state);
    }

    public static DeathChestBlock.BEntity deathChest(BlockPos pos, BlockState state) {
        return isLoaded() ? LootrChestFactory.deathChest(pos, state) : new DeathChestBlock.BEntity(pos, state);
    }
}
