package org.confluence.mod.mixin.world.level.block.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The chest's open counter only counts a player as "viewing" when their menu holds the chest itself.
 * A per-player (Lootr) chest opens a private inventory instead, so without this the lid closes while it is open.
 */
@Mixin(targets = "net.minecraft.world.level.block.entity.ChestBlockEntity$1")
public abstract class ChestOpenersCounterMixin {
    @Shadow
    @Final
    ChestBlockEntity this$0;

    @Inject(method = "isOwnContainer", at = @At("HEAD"), cancellable = true)
    private void confluence$perPlayerMenu(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this$0 instanceof BaseChestBlock.BEntity chest && player.containerMenu instanceof ChestMenu menu && chest.ownsMenu(menu.getContainer())) {
            cir.setReturnValue(true);
        }
    }
}
