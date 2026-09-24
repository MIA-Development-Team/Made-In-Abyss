package com.altnoir.mia.common.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 太初菌家族的"弹性"行为（移植自 PoopSky 的 {@code PrimoCapBlock} / {@code GlowPrimoCapBlock} /
 * {@code PrimoFungusBlock} 三处重复的实现）。
 * <p>
 * 三个方块的父类各不相同（{@code Block} / {@code HalfTransparentBlock} / {@code MiaFungusBlock}），
 * 没法抽公共父类；{@code fallOn} 又要调各自的 {@code super}，所以只把反弹的数学抽到这里，
 * 各方块保留两行 {@code @Override}。
 */
public final class BounceHelper {
    private BounceHelper() {}

    /**
     * 落地时是否反弹：抑制弹跳的实体（潜行、穿弹跳靴以外的情形）走原版逻辑。
     */
    public static boolean shouldBounce(Entity entity) {
        return !entity.isSuppressingBounce();
    }

    /**
     * 把下落速度按 0.66 反弹，生物为 1.0 倍、其他实体 0.8 倍。
     */
    public static void bounceUp(Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < 0.0) {
            double scale = entity instanceof LivingEntity ? 1.0 : 0.8;
            entity.setDeltaMovement(movement.x, -movement.y * 0.66 * scale, movement.z);
        }
    }
}
