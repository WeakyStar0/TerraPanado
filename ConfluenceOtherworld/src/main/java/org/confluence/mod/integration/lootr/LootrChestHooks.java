package org.confluence.mod.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import noobanidus.mods.lootr.common.api.LootrAPI;
import noobanidus.mods.lootr.common.api.data.inventory.ILootrInventory;
import org.confluence.mod.common.block.common.BaseChestBlock;

/** Logic shared by the Lootr aware chest block entities. */
final class LootrChestHooks {
    private LootrChestHooks() {
    }

    /** Lootr takes over a chest that still has a loot table, unless the pack turned Lootr off for it. */
    static boolean active(BaseChestBlock.BEntity chest) {
        Level level = chest.getLevel();
        ResourceKey<LootTable> table = chest.getLootTable();
        return table != null
                && level != null
                && !LootrAPI.isDisabled()
                && !LootrAPI.isDimensionBlocked(level.dimension())
                && !LootrAPI.isLootTableBlacklisted(table);
    }

    static void interact(LootrChest chest, ServerPlayer player) {
        BaseChestBlock.BEntity blockEntity = (BaseChestBlock.BEntity) chest;
        if (player.isShiftKeyDown()) {
            LootrAPI.handleProviderSneak(chest, player);
        } else if (!ChestBlock.isChestBlockedAt(blockEntity.getLevel(), blockEntity.getBlockPos())) {
            LootrAPI.handleProviderOpen(chest, player);
        }
    }

    static float destroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, float progress) {
        return LootrAPI.getDestroyProgress(state, player, level, pos, progress);
    }

    static float explosionResistance(BaseChestBlock.BEntity chest, float resistance) {
        // never weaker than what the chest had before
        return Math.max(resistance, LootrAPI.getExplosionResistance(chest.getBlockState().getBlock(), resistance));
    }

    static int comparatorSignal(BlockState state, Level level, BlockPos pos) {
        return LootrAPI.getAnalogOutputSignal(state, level, pos, 0);
    }

    static void playerDestroyed(Level level, Player player, BlockPos pos, BaseChestBlock.BEntity chest) {
        LootrAPI.playerDestroyed(level, player, pos, chest);
    }

    static boolean ownsMenu(LootrChest chest, Container menuContainer) {
        return menuContainer instanceof ILootrInventory inventory && chest.getInfoUUID().equals(inventory.getInfo().getInfoUUID());
    }
}
