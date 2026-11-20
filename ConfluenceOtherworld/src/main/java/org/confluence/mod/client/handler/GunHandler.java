package org.confluence.mod.client.handler;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.confluence.lib.api.animation.first_person.CameraAnimation;
import org.confluence.mod.api.event.GunEvent;
import org.confluence.mod.client.ModClientConfigs;
import org.confluence.mod.client.ModKeyBindings;
import org.confluence.mod.client.gui.hud.CrosshairHud;
import org.confluence.mod.client.renderer.entity.bullet.BulletVfxManager;
import org.confluence.mod.common.item.gun.BaseGun;
import org.confluence.mod.network.c2s.InspectPacketC2S;
import org.confluence.mod.network.c2s.ShootPacketC2S;
import org.confluence.mod.util.ModGunUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;

public final class GunHandler {
    private static boolean wasDefaultGunShootHeld = false;

    public static void handle(@Nullable LocalPlayer player, boolean attackHeld) {
        if (player == null) {
            CameraAnimation.clear();
            wasDefaultGunShootHeld = false;
            return;
        }
        inspect(player);

        BulletVfxManager.tick();
        KeyMapping shoot = ModKeyBindings.GUN_SHOOT.get();

        updateGunCameraAnimation(player);
        /// 枪的动作键按配置取左键或右键；自定义了射击键位时以键位为准
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        boolean useHeld = ModClientConfigs.gunUseButton == ModClientConfigs.WeaponUseButton.LEFT
                ? attackHeld : minecraft.screen == null && minecraft.options.keyUse.isDown();
        boolean defaultBindingHeld = shoot.isDefault() && useHeld;
        boolean shootHeld = shoot.isDefault() ? defaultBindingHeld : shoot.isDown();
        boolean defaultBindingPressed = defaultBindingHeld && !wasDefaultGunShootHeld;
        wasDefaultGunShootHeld = defaultBindingHeld;
        if (player.isSpectator() || !shootHeld) return;

        ItemStack mainHandItem = player.getMainHandItem();
        ItemCooldowns cooldowns = player.getCooldowns();
        if (!(mainHandItem.getItem() instanceof BaseGun baseGun) || cooldowns.isOnCooldown(baseGun))
            return;
        if (!ModGunUtils.canShoot(player, mainHandItem)) return;
        if (!baseGun.isAutomatic(mainHandItem)) {
            /// 默认键位按配置的左/右键判断“刚按下”，自定义键位仍用按键的点击计数
            boolean clicked = shoot.isDefault() ? defaultBindingPressed : shoot.consumeClick();
            if (!clicked) return;
        }

        GunEvent.Use use = new GunEvent.Use(player, baseGun, baseGun.getCooldown());
        NeoForge.EVENT_BUS.post(use);
        if (use.isCanceled()) return;

        ShootPacketC2S.sendToServer();
        CrosshairHud.onShot(player, InteractionHand.MAIN_HAND, mainHandItem);
        cooldowns.addCooldown(baseGun, Math.max(0, use.getCooldowns()));
    }

    private static void updateGunCameraAnimation(LocalPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof BaseGun gun) || !gun.isCameraAnimationPlaying(GeoItem.getId(mainHandItem))) {
            CameraAnimation.clear();
        }
    }

    private static void inspect(LocalPlayer player) {
        if (!player.isSpectator() && ModKeyBindings.GUN_INSPECT.get().consumeClick()) {
            if (player.getMainHandItem().getItem() instanceof BaseGun) {
                InspectPacketC2S.sendToServer();
            }
        }
    }

    public static void reset() {
        wasDefaultGunShootHeld = false;
    }
}
