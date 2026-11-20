package org.confluence.mod.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.client.ModClientConfigs;
import org.confluence.mod.common.init.ModTags;
import org.lwjgl.glfw.GLFW;

/// 法杖动作键配置为左键时：左键按住等同于按住原版使用键，右键则不再触发法杖。
///
/// 法杖全部走原版的“使用物品”流程（按住持续使用、松开结算），所以这里只改使用键的按下状态，
/// 不另起一套封包。
public final class StaffInputHandler implements ClientWeaponInputHandler {
    public static final StaffInputHandler INSTANCE = new StaffInputHandler();
    private boolean overriding;

    private StaffInputHandler() {}

    @Override
    public void tick(LocalPlayer player, ItemStack stack, boolean attackHeld) {
        Minecraft minecraft = Minecraft.getInstance();
        KeyMapping use = minecraft.options.keyUse;
        boolean active = usesLeftButton(stack) && minecraft.screen == null && minecraft.isWindowActive()
                && player.isAlive() && !player.isSpectator();
        if (active) {
            use.setDown(attackHeld);
            overriding = true;
        } else if (overriding) {
            overriding = false;
            use.setDown(physicallyDown(minecraft, use));
        }
    }

    /// 左键配置下，右键单独按下不能使用法杖；左键按住时由上面模拟出的使用键放行。
    @Override
    public boolean blocksUse(ItemStack stack) {
        return usesLeftButton(stack) && !Minecraft.getInstance().options.keyAttack.isDown();
    }

    @Override
    public boolean blocksAttack(ItemStack stack) {
        return usesLeftButton(stack);
    }

    @Override
    public void reset() {
        if (overriding) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.options.keyUse.setDown(false);
        }
        overriding = false;
    }

    private static boolean usesLeftButton(ItemStack stack) {
        return stack.is(ModTags.Items.MANA_WEAPON) && ModClientConfigs.staffUseButton == ModClientConfigs.WeaponUseButton.LEFT;
    }

    private static boolean physicallyDown(Minecraft minecraft, KeyMapping mapping) {
        InputConstants.Key key = mapping.getKey();
        long window = minecraft.getWindow().getWindow();
        return key.getType() == InputConstants.Type.MOUSE
                ? GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS
                : InputConstants.isKeyDown(window, key.getValue());
    }
}
