package org.confluence.mod.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.confluence.lib.util.LibRenderUtils;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.block.OreBlocks;
import org.confluence.mod.common.init.item.MaterialItems;
import org.confluence.mod.network.s2c.MeteoriteLocationPacketS2C;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public final class MeteorLandingHandler {
    private static final ResourceLocation TEXTURE = Confluence.asResource("textures/environment/meteor.png");
    private static final float RADIUS = 5.0F;
    private static final PoseStack poseStack = new PoseStack();
    private static @Nullable GlobalPos globalPos = null;
    private static @Nullable Vec3 location = null;
    private static int tickUntilLanding = 0;

    private static int totalLandingTick = 0;
    private static float alpha = 0.0F;
    private static float pitch = -Mth.HALF_PI;
    private static float pitchO = -Mth.HALF_PI;
    private static float yaw = Mth.HALF_PI;
    private static float yawO = Mth.HALF_PI;
    private static @Nullable Vec3 vector = null;
    private static double distance = 0.0;
    private static float v0 = 0;
    private static float v1 = 0;

    public static void reset() {
        globalPos = null;
        location = null;
        tickUntilLanding = 0;
    }

    public static void handle(Minecraft minecraft, Player player) {
        if (minecraft.isPaused()) return;
        if (tickUntilLanding > 0) {
            pitchO = pitch;
            yawO = yaw;
            calculate(player);
            tickUntilLanding--;
            v0 = 0.25F * ((tickUntilLanding >> 1) % 4);
            v1 = v0 + 0.25F;
        } else {
            location = null;
        }
    }

    public static void handlePacket(MeteoriteLocationPacketS2C packet, @Nullable Player player) {
        globalPos = GlobalPos.of(packet.dimension(), packet.location());
        if (packet.tickUntilLanding() <= 0) {
            location = null;
            tickUntilLanding = 0;
            return;
        }
        location = packet.location().getCenter();
        tickUntilLanding = packet.tickUntilLanding();

        totalLandingTick = tickUntilLanding;
        if (player != null) {
            calculate(player);
        }
        pitchO = pitch;
        yawO = yaw;
    }

    private static void calculate(Player player) {
        assert location != null;
        vector = player.position().subtract(location);
        distance = vector.length() * 0.0625; // 1.0 / 16.0
        Vec3 vec = vector.normalize();
        alpha = 1.0F - (float) tickUntilLanding / (float) totalLandingTick; // 0.0 -> 1.0
        float ratio = Mth.lerp(alpha, 0.3F, 1.0F); // 0.3 -> 1.0
        pitch = -Mth.HALF_PI * ratio;
        yaw = Mth.HALF_PI * ratio - (float) Math.atan2(vec.z, vec.x);
    }

    public static void render(RenderLevelStageEvent event) {
        if (location == null || vector == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || globalPos == null || player.level().dimension() != globalPos.dimension()) return;
        if (distance < event.getLevelRenderer().getLastViewDistance()) return;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        poseStack.pushPose();
        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.mulPose(Axis.YP
                .rotation(Mth.lerp(partialTick, yawO, yaw))
                .rotateX(Mth.lerp(partialTick, pitchO, pitch)));

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Matrix4f matrix4f = poseStack.last().pose().rotate(LibRenderUtils.ANGLE_45);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix4f, -RADIUS, 100, -RADIUS).setUv(0.0F, v1).setColor(1.0F, 1.0F, 1.0F, alpha);
        bufferBuilder.addVertex(matrix4f, RADIUS, 100, -RADIUS).setUv(1.0F, v1).setColor(1.0F, 1.0F, 1.0F, alpha);
        bufferBuilder.addVertex(matrix4f, RADIUS, 100, RADIUS).setUv(1.0F, v0).setColor(1.0F, 1.0F, 1.0F, alpha);
        bufferBuilder.addVertex(matrix4f, -RADIUS, 100, RADIUS).setUv(0.0F, v0).setColor(1.0F, 1.0F, 1.0F, alpha);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static @Nullable GlobalPos getGlobalPos() {
        return globalPos;
    }

    private static List<ResourceLocation> landingDimensions = List.of();

    public static void setLandingDimensions(List<ResourceLocation> dimensions) {
        landingDimensions = List.copyOf(dimensions);
    }

    /// 在陨石相关物品上标出陨石可能降落的维度（来自服务端配置）。
    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        if (landingDimensions.isEmpty() || !isMeteoriteItem(stack)) return;
        MutableComponent names = Component.empty();
        for (int i = 0; i < landingDimensions.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(dimensionName(landingDimensions.get(i)));
        }
        tooltip.add(Component.translatable("tooltip.confluence.meteorite.dimensions", names).withStyle(ChatFormatting.GRAY));
    }

    private static boolean isMeteoriteItem(ItemStack stack) {
        return stack.is(OreBlocks.METEORITE_ORE.asItem()) || stack.is(OreBlocks.RAW_METEORITE_BLOCK.asItem())
                || stack.is(OreBlocks.METEORITE_BLOCK.asItem()) || stack.is(MaterialItems.RAW_METEORITE.get())
                || stack.is(MaterialItems.METEORITE_INGOT.get()) || stack.is(MaterialItems.METEORITE_NUGGET.get());
    }

    private static Component dimensionName(ResourceLocation id) {
        String key = id.toLanguageKey("dimension");
        if (I18n.exists(key)) return Component.translatable(key);
        StringBuilder name = new StringBuilder();
        for (String word : id.getPath().split("[_/]")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.literal(name.toString());
    }
}
