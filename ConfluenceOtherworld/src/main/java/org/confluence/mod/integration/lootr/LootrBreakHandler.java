package org.confluence.mod.integration.lootr;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import noobanidus.mods.lootr.common.api.LootrAPI;
import org.confluence.mod.common.block.common.BaseChestBlock;

/** Lootr's break rules (disable_break, enable_break, break_to_drop_loot, ...) for per-player Confluence chests. */
public final class LootrBreakHandler {
    private LootrBreakHandler() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof BaseChestBlock.BEntity chest) || !chest.isLootrChest() || !(chest instanceof LootrChest lootrChest)) return;
        Player player = event.getPlayer();
        if (LootrAPI.canDestroyOrBreak(player)) return;

        boolean dumped = false;
        if (LootrAPI.breakToDropLoot() && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer && event.getLevel() instanceof ServerLevel serverLevel) {
            LootrAPI.dumpPlayerLoot(lootrChest, serverPlayer, serverLevel);
            dumped = true;
        }

        if (LootrAPI.isBreakDisabled()) {
            if (player.getAbilities().instabuild) {
                if (!player.isShiftKeyDown()) {
                    event.setCanceled(true);
                    if (!dumped) tell(player, "lootr.message.cannot_break_sneak");
                }
            } else {
                event.setCanceled(true);
                if (!dumped) tell(player, "lootr.message.cannot_break");
            }
        } else if (!player.isShiftKeyDown()) {
            event.setCanceled(true);
            if (!dumped) {
                tell(player, "lootr.message.should_sneak");
                tell(player, "lootr.message.should_sneak2");
            }
        }
    }

    private static void tell(Player player, String key) {
        player.displayClientMessage(Component.translatable(key).setStyle(LootrAPI.getChatStyle()), false);
    }
}
