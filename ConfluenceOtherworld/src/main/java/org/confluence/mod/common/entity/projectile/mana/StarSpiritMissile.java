package org.confluence.mod.common.entity.projectile.mana;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.confluence.lib.util.LibEntityUtils.TargetPolicy;
import org.confluence.lib.util.LibEntityUtils.WeaponTargeting;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.init.ModSoundEvents;
import org.confluence.mod.common.init.entity.ModEntities;
import org.confluence.mod.common.init.item.SoulItems;
import org.confluence.mod.mixed.Immunity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


import java.util.UUID;

/// 星魄发射的魔法飞弹：外观、粒子与拖尾沿用彩虹魔杖的射弹。
///
/// 每次施放同时发出 3 枚，起飞时各自偏向不同方向，再一路弯向目标，形成互不重叠的弧线；
/// 有锁定目标就一直追踪它，没有就飞向射线的终点。
/// 一批 3 枚共用一个 [Batch]，只要其中任意一枚命中了生物，整批只扣 1 点耐久。
public class StarSpiritMissile extends AbstractManaProjectile implements Immunity {
    private static final WeaponTargeting TARGETING = new WeaponTargeting(TargetPolicy.ENEMY_FACTION);
    public static final int MAX_AGE = 70;
    private static final double SPEED = 1.6;
    /// 音量大于 1 会按比例扩大可听范围（约 16 格 × 音量）。
    private static final float IMPACT_VOLUME = 4.0F;

    private Vec3 aimPoint = Vec3.ZERO;
    private @Nullable UUID targetId;
    private @Nullable Batch batch;

    public StarSpiritMissile(EntityType<? extends StarSpiritMissile> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
        withParticle(Confluence.asResource("rainbow_projectile"));
    }

    public StarSpiritMissile(LivingEntity owner) {
        this(ModEntities.STAR_SPIRIT_MISSILE.get(), owner.level());
        setOwner(owner);
    }

    /// 一批 3 枚共享的耐久记录。
    public static final class Batch {
        private boolean paid;

        /// @return 这一批第一次调用时为 true
        boolean claim() {
            if (paid) return false;
            paid = true;
            return true;
        }
    }

    /// @param start 起点
    /// @param forward 视线方向
    /// @param side 这一枚起飞时偏向的方向（垂直于视线）
    public void launch(Vec3 start, Vec3 forward, Vec3 side, Vec3 aimPoint, @Nullable LivingEntity target, Batch batch) {
        this.aimPoint = aimPoint;
        this.targetId = target == null ? null : target.getUUID();
        this.batch = batch;
        setPos(start);
        setDeltaMovement(forward.scale(SPEED * 0.45).add(side.scale(SPEED * 0.9)));
    }

    /// 起飞后的前几 tick 保留弧线，之后对锁定目标做纯追踪，速度逐渐加快，保证追得上移动的怪物。
    private static final int ARC_TICKS = 8;
    private static final int LOCKED_MAX_AGE = 120;

    @Override
    public void baseTick() {
        super.baseTick();
        if (level().isClientSide) {
            setPos(position().add(getDeltaMovement()));
            return;
        }
        LivingEntity target = lockedTarget();
        if (tickCount > (target != null ? LOCKED_MAX_AGE : MAX_AGE)) {
            discardInTicks(1);
            return;
        }
        if (target != null) aimPoint = target.getBoundingBox().getCenter();

        double speed = target != null && tickCount > ARC_TICKS ? SPEED + Math.min(1.2, (tickCount - ARC_TICKS) * 0.1) : SPEED;
        Vec3 motion = getDeltaMovement();
        Vec3 desired = aimPoint.subtract(position());
        double distance = desired.length();
        if (distance > 1.0E-4) {
            Vec3 wanted = desired.scale(speed / distance);
            if (target != null && tickCount > ARC_TICKS) {
                motion = wanted; // 纯追踪：每 tick 直接对准目标
            } else {
                double steer = Math.min(0.6, (target != null ? 0.14 : 0.08) + tickCount * 0.02);
                motion = motion.add(wanted.subtract(motion).scale(steer));
                double current = motion.length();
                if (current > 1.0E-4) motion = motion.scale(speed / current);
            }
        }
        /// 这一步会走到目标身上就直接命中，快速移动的怪物也不会被穿过去
        if (target != null && tickCount > 1 && distance <= motion.length() + target.getBbWidth() * 0.5 + 0.5) {
            onHitEntity(new EntityHitResult(target));
            return;
        }
        setDeltaMovement(motion);
        Vec3 from = position();
        setPos(from.add(motion));
        drawTrail(from, position());
        hasImpulse = true;
    }

    /// 沿这一 tick 走过的路径密集撒一排彩虹色小光点，拖尾连成一条清晰的线。
    private void drawTrail(Vec3 from, Vec3 to) {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Vec3 step = to.subtract(from);
        int count = Math.max(1, (int) Math.ceil(step.length() / 0.22));
        for (int i = 0; i < count; i++) {
            Vec3 point = from.add(step.scale((double) i / count));
            float hue = ((tickCount * 0.045F) + i * 0.012F) % 1.0F;
            serverLevel.sendParticles(rainbowDust(hue, 0.7F), point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        serverLevel.sendParticles(ParticleTypes.END_ROD, to.x, to.y, to.z, 1, 0.03, 0.03, 0.03, 0.0);
    }

    private static DustParticleOptions rainbowDust(float hue, float scale) {
        int rgb = net.minecraft.util.Mth.hsvToRgb(hue, 0.85F, 1.0F);
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F), scale);
    }

    /// 命中时叠加三层：一闪白光、一圈向外扩散的彩虹光环、向四周迸射的星光。
    private void impactEffect(ServerLevel serverLevel) {
        double x = getX(), y = getY(), z = getZ();
        serverLevel.sendParticles(ParticleTypes.FLASH, x, y, z, 1, 0, 0, 0, 0);
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2 / 24;
            double dx = Math.cos(angle), dz = Math.sin(angle);
            serverLevel.sendParticles(rainbowDust(i / 24.0F, 1.3F), x + dx * 0.6, y, z + dz * 0.6, 1, 0, 0, 0, 0);
            if (i % 4 == 0) serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 0, dx, 0.05, dz, 0.3);
        }
        for (int i = 0; i < 6; i++) {
            Vec3 dir = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
            serverLevel.sendParticles(ParticleTypes.FIREWORK, x, y, z, 0, dir.x, dir.y, dir.z, 0.3);
        }
    }

    private @Nullable LivingEntity lockedTarget() {
        if (targetId != null && ((ServerLevel) level()).getEntity(targetId) instanceof LivingEntity living && living.isAlive()) return living;
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (doHurtAndKnockback(target, 0.3, 0.1)) {
            payDurability();
        }
        level().playSound(null, getX(), getY(), getZ(), ModSoundEvents.ITEM_METEOR_IMPACT.get(), SoundSource.PLAYERS, IMPACT_VOLUME, 1.0F);
        if (level() instanceof ServerLevel serverLevel) {
            impactEffect(serverLevel);
        }
        discardInTicks(1);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide && lockedTarget() != null) return; // 有锁定目标时穿过方块继续追
        discardInTicks(1);
    }

    /// 整批只扣一次：由第一枚打到生物的飞弹支付。
    private void payDurability() {
        if (batch == null || !batch.claim() || !(getOwner() instanceof LivingEntity owner)) return;
        ItemStack held = owner.getMainHandItem();
        if (held.is(SoulItems.STAR_SPIRIT.get())) {
            held.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && TARGETING.canAcquire(getOwner(), target);
    }

    /// 每枚飞弹自己的无敌帧：3 枚间隔 2 tick 命中同一个敌人时都要结算，不被原版受伤帧吞掉。
    @Override
    public Type confluence$getImmunityType() {
        return Type.LOCAL;
    }

    @Override
    public int confluence$getImmunityDuration(DamageSource damageSource) {
        return 0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
