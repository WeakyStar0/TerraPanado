package org.confluence.mod.common.item.mana;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.confluence.lib.common.LibAttributes;
import org.confluence.lib.common.component.ModRarity;
import org.confluence.lib.common.item.CustomRarityItem;
import org.confluence.lib.util.LibEntityUtils;
import org.confluence.lib.util.LibEntityUtils.TargetPolicy;
import org.confluence.lib.util.LibEntityUtils.WeaponTargeting;
import org.confluence.mod.Confluence;
import org.confluence.mod.api.item.IWeaponTooltip;
import org.confluence.mod.common.entity.projectile.mana.StarSpiritMissile;
import org.confluence.mod.common.init.ModSoundEvents;
import org.confluence.mod.common.summoner.util.ZenithTargeting;
import org.confluence.mod.util.PlayerUtils;
import org.confluence.mod.util.PrefixUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/// 星魄：按住使用键，每隔约 1 秒连续射出 3 枚弯曲的魔法飞弹。
///
/// - 一批 3 枚（相隔 2 tick）消耗 23 魔力；
/// - 一批算一次使用，仅在其中至少有一枚命中生物时扣 1 点耐久；
/// - 瞄准射线上的敌人会被锁定，飞弹一路追踪它；没有目标就飞向射线终点。
@EventBusSubscriber(modid = Confluence.MODID)
public class StarSpiritItem extends CustomRarityItem implements IWeaponTooltip {
    public static final float DAMAGE = 23.0F;
    public static final int MANA_COST = 23;
    /// 两批之间的间隔：3 枚发完后再等约 1 秒。
    public static final int CYCLE = 24;
    private static final int SHOT_GAP = 2;
    private static final int MISSILES = 3;
    private static final double REACH = 48.0;
    private static final double TARGET_RANGE = 32.0;
    private static final double TARGET_CONE = 30.0;
    private static final WeaponTargeting TARGETING = new WeaponTargeting(TargetPolicy.ENEMY_FACTION);
    /// 正在发射的这一批：锁定目标、瞄准点与耐久记录由 3 枚共享。
    private static final Map<UUID, Volley> VOLLEYS = new HashMap<>();

    private record Volley(Vec3 forward, Vec3 aimPoint, @Nullable LivingEntity target, StarSpiritMissile.Batch batch) {}

    public StarSpiritItem() {
        super(new Item.Properties().stacksTo(1), ModRarity.ORANGE);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 20;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getMaxStackSize() == 1;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(living instanceof ServerPlayer player)) return;
        int phase = (getUseDuration(stack, living) - remainingUseDuration) % CYCLE;
        if (phase % SHOT_GAP != 0 || phase / SHOT_GAP >= MISSILES) return;
        int index = phase / SHOT_GAP;

        if (index == 0) {
            if (!PlayerUtils.extractMana(player, stack, () -> PrefixUtils.calculateManaCost(stack, MANA_COST))) {
                player.stopUsingItem();
                return;
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            player.getCooldowns().addCooldown(this, CYCLE);
            VOLLEYS.put(player.getUUID(), lockOn(player));
        }
        Volley volley = VOLLEYS.get(player.getUUID());
        if (volley == null) return;

        Vec3 forward = volley.forward();
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 up = right.cross(forward).normalize();
        double angle = Math.toRadians(90.0 + index * 120.0);
        Vec3 side = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));

        StarSpiritMissile missile = new StarSpiritMissile(player);
        missile.setDamage(DAMAGE);
        /// 从玩家身后升起，再绕到前方飞向目标
        Vec3 start = player.getEyePosition().add(0, 0.2, 0).subtract(forward.scale(1.2)).add(side.scale(0.4));
        missile.launch(start, forward, side, volley.aimPoint(), volley.target(), volley.batch());
        level.addFreshEntity(missile);
        level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), ModSoundEvents.ITEM_MAGIC_MISSILE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /// 沿视线做一次射线：命中的敌人被锁定，否则取射线终点（遇到方块就停在方块上）。
    private static Volley lockOn(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 forward = player.getViewVector(1.0F);
        LivingEntity target = findTarget(player, eye, forward);
        Vec3 aim;
        if (target != null) {
            aim = target.getBoundingBox().getCenter();
        } else {
            Vec3 end = eye.add(forward.scale(REACH));
            HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            aim = block.getLocation();
        }
        return new Volley(forward, aim, target, new StarSpiritMissile.Batch());
    }

    /// 和天顶剑相同的选敌规则：视线锥内、只选可攻击的敌对目标，按角度和距离打分；额外要求从眼睛到目标没有方块遮挡。
    private static @Nullable LivingEntity findTarget(ServerPlayer player, Vec3 eye, Vec3 forward) {
        Vec3 center = player.getBoundingBox().getCenter();
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity candidate : LibEntityUtils.queryHitCandidates(player.level(), player, player.getBoundingBox().inflate(TARGET_RANGE), e -> TARGETING.canAcquire(player, e))) {
            Vec3 point = candidate.getBoundingBox().getCenter();
            double score = ZenithTargeting.score(center, forward, point, TARGET_RANGE, TARGET_CONE);
            if (score >= bestScore) continue;
            if (player.level().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
            bestScore = score;
            best = candidate;
        }
        return best == null ? null : LibEntityUtils.effectTarget(best);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeCharged) {
        VOLLEYS.remove(living.getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        VOLLEYS.remove(event.getEntity().getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.confluence.mana_cost", MANA_COST).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.item.confluence.star_spirit.0").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Holder<Attribute> getTooltipDamageAttribute() {
        return LibAttributes.getMagicDamage();
    }

    @Override
    public double getTooltipDamage(ItemStack stack, double attributeValue) {
        return DAMAGE * attributeValue;
    }

    @Override
    public double getTooltipAttackSpeed(ItemStack stack, double attributeValue) {
        return 20.0 / CYCLE;
    }

    @Override
    public double getTooltipAttackSpeed(ItemStack stack, double attributeValue, @Nullable Player player) {
        return 20.0 / CYCLE;
    }
}
