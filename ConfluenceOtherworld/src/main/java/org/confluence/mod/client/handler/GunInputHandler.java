package org.confluence.mod.client.handler;

import net.minecraft.world.item.ItemStack;
import org.confluence.mod.client.ModClientConfigs;
import org.confluence.mod.client.ModKeyBindings;
import org.confluence.mod.common.item.gun.BaseGun;

/// 枪的动作键配置为右键时，右键用来开火，不能同时触发方块交互（开箱子、开门等）。
public final class GunInputHandler implements ClientWeaponInputHandler {
    public static final GunInputHandler INSTANCE = new GunInputHandler();

    private GunInputHandler() {}

    @Override
    public boolean blocksUse(ItemStack stack) {
        return stack.getItem() instanceof BaseGun
                && ModClientConfigs.gunUseButton == ModClientConfigs.WeaponUseButton.RIGHT
                && ModKeyBindings.GUN_SHOOT.get().isDefault();
    }
}
