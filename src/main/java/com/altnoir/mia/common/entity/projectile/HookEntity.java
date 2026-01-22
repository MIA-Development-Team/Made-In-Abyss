package com.altnoir.mia.common.entity.projectile;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaEntities;
import com.altnoir.mia.init.MiaItems;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class HookEntity extends Projectile {
    public static final EntityDataAccessor<Integer> DATA_HOOK_STATE = SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> DATA_SHOOT_HAND = SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.BOOLEAN);
    // 钩住位置和方块状态
    public BlockPos hookedBlockPos;
    public BlockState hookedBlockState;

    public HookEntity(EntityType<? extends HookEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public HookEntity(Player player, InteractionHand hand) {
        this(MiaEntities.HOOK.get(), player.level());
        setOwner(player);
        setShootHand(hand);
        setNoGravity(true);
        setPos(
                player.getX(),
                player.getEyeY() - 0.1,
                player.getZ()
        );
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HOOK_STATE, HookState.SHOOT.id)
                .define(DATA_SHOOT_HAND, true);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // 64 * 64
        return distance < 4096;
    }

    public @Nullable Player getPlayer() {
        return getOwner() instanceof Player player ? player : null;
    }

    public HookState getHookState() {
        return HookState.byId(entityData.get(DATA_HOOK_STATE));
    }

    public void setHookState(HookState hookState) {
        entityData.set(DATA_HOOK_STATE, hookState.id);
    }

    public boolean isHooked() {
        return getHookState() == HookState.HOOKED;
    }

    public InteractionHand getShootHand() {
        return entityData.get(DATA_SHOOT_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public void setShootHand(InteractionHand hand) {
        entityData.set(DATA_SHOOT_HAND, hand == InteractionHand.MAIN_HAND);
    }

    @Override
    public void tick() {
        super.tick();
        Player player = getPlayer();
        if (player == null || player.isRemoved() || !player.isAlive() || !player.getItemInHand(getShootHand()).is(MiaItems.HOOK_ITEM)) {
            discard();
            return;
        }
        switch (getHookState()) {
            // SHOOT：飞行中
            case SHOOT -> tickShoot(player);
            // BACK：收回中
            case BACK -> tickHooked(player);
            // HOOKED：已钩住
            case HOOKED -> tickBack(player);
        }
        ;
    }

    /**
     * SHOOT 状态逻辑
     */
    private void tickShoot(Player player) {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitresult)) {
            this.hitTargetOrDeflectSelf(hitresult);
        }
        this.checkInsideBlocks();
        this.updateRotation();
        Vec3 vec3 = getDeltaMovement();
        this.setDeltaMovement(vec3.scale(0.99));
        this.applyGravity();
        this.setPos(
                getX() + vec3.x,
                getY() + vec3.y,
                getZ() + vec3.z
        );
        player.resetFallDistance();
        // 检查是否超出最大距离
        double distanceSqr = position().distanceToSqr(player.position());
        if (distanceSqr > MiaConfig.hookMaxDistance * MiaConfig.hookMaxDistance) {
            setHookState(HookState.BACK);
        }
    }

    /**
     * BACK 状态逻辑
     */
    private void tickBack(Player player) {
        // 计算返回速度
        Vec3 direction = player.position()
                .add(0, player.getEyeHeight() - 0.1, 0)
                .subtract(position())
                .normalize()
                .scale(MiaConfig.hookRetractVelocity);
        Vec3 velocity = getDeltaMovement()
                .scale(0.95)
                .add(direction);
        setDeltaMovement(velocity);
        // 移动实体
        Vec3 vec3 = getDeltaMovement();
        this.setPos(
                getX() + vec3.x,
                getY() + vec3.y,
                getZ() + vec3.z
        );
        // 到达玩家后销毁
        if (distanceToSqr(player) < MiaConfig.hookInstantRetractDistance * MiaConfig.hookInstantRetractDistance) {
            discard();
        }
    }

    /**
     * HOOKED 状态逻辑
     */
    private void tickHooked(Player player) {
        if (!level().isClientSide()) {
            // 检查方块是否仍然存在
            if (hookedBlockPos == null || level().getBlockState(hookedBlockPos) != hookedBlockState) {
                setHookState(HookState.BACK);
                return;
            }
            // 检查是否超出最大距离
            if (distanceToSqr(player.position()) > MiaConfig.hookMaxDistance * MiaConfig.hookMaxDistance) {
                setHookState(HookState.BACK);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        HookState hookState = getHookState();
        if (hookState == HookState.BACK) return;
        Vec3 vec3 = result.getLocation().subtract(position());
        setDeltaMovement(vec3);
        if (hookState != HookState.SHOOT) return;
        setHookState(HookState.HOOKED);
        hookedBlockPos = result.getBlockPos();
        hookedBlockState = level().getBlockState(hookedBlockPos);
    }

    public enum HookState implements StringRepresentable {
        SHOOT(0, "shoot"),   // 发射
        BACK(1, "back"),     // 收回
        HOOKED(2, "hooked"); // 抓住

        public static final Codec<HookState> CODEC = StringRepresentable.fromEnum(HookState::values);
        private static final IntFunction<HookState> BY_ID = ByIdMap.continuous(HookState::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        HookState(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public static HookState byId(int id) {
            return BY_ID.apply(id);
        }

        public int getId() {
            return id;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
