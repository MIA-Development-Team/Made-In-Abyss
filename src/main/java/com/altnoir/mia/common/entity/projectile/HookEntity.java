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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
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
    // 被钩住的实体
    @Nullable
    public Entity hookedIn;

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

    public boolean isShoot() {
        return getHookState() == HookState.SHOOT;
    }

    public boolean isBack() {
        return getHookState() == HookState.BACK;
    }

    public boolean isHooked() {
        return isHookedBlock() || isHookedEntity();
    }

    public boolean isHookedBlock() {
        return getHookState() == HookState.HOOKED_BLOCK;
    }

    public boolean isHookedEntity() {
        return getHookState() == HookState.HOOKED_ENTITY;
    }

    public void setHookedEntity(@Nullable Entity entity) {
        this.hookedIn = entity;
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
        if (player == null || player.isRemoved() || !player.isAlive() || !player.getItemInHand(getShootHand()).is(MiaItems.HOOK)) {
            discard();
            return;
        }
        player.resetFallDistance();
        if (player.isPassenger()) {
            player.getVehicle().resetFallDistance();
        }
        switch (getHookState()) {
            // SHOOT：飞行中
            case SHOOT -> tickShoot(player);
            // BACK：收回中
            case BACK -> tickBack(player);
            // HOOKED_ENTITY：钩住实体
            case HOOKED_ENTITY -> tickHookedEntity(player);
            // HOOKED_BLOCK：钩住方块
            case HOOKED_BLOCK -> tickHookedBlock(player);
        }
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
        // 检查是否超出最大距离
        if (distanceToSqr(player) > MiaConfig.hookMaxDistance * MiaConfig.hookMaxDistance) {
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
        if (distanceToSqr(player) < MiaConfig.hookRetractDistance * MiaConfig.hookRetractDistance) {
            discard();
        }
    }

    /**
     * HOOKED_BLOCK 状态逻辑
     */
    private void tickHookedBlock(Player player) {
        if (!level().isClientSide()) {
            // 检查方块是否仍然存在
            if (hookedBlockPos == null || level().getBlockState(hookedBlockPos) != hookedBlockState) {
                setHookState(HookState.BACK);
                return;
            }
            // 检查是否超出最大距离
            if (distanceToSqr(player) > MiaConfig.hookMaxDistance * MiaConfig.hookMaxDistance) {
                setHookState(HookState.BACK);
            }
        }
    }

    /**
     * HOOKED_ENTITY 状态逻辑
     */
    private void tickHookedEntity(Player player) {
        if (hookedIn == null) {
            setHookState(HookState.BACK);
            return;
        }
        if (!hookedIn.isRemoved() && hookedIn.level() == level()) {
            setPos(hookedIn.getX(), hookedIn.getY(0.8), hookedIn.getZ());
            pullEntity(hookedIn, player);
        } else {
            setHookedEntity(null);
            setHookState(HookState.BACK);
        }
    }

    /**
     * 拉取实体朝向玩家
     */
    private void pullEntity(Entity entity, Player player) {
        Vec3 target = player.position().add(0, player.getEyeHeight() - 0.1, 0);
        // 这里把hookStopPullDistance乘了2，不然距离太近会一直吸附
        if (entity.distanceToSqr(player) < (MiaConfig.hookStopPullDistance * MiaConfig.hookStopPullDistance) * 2) {
            entity.setDeltaMovement(Vec3.ZERO);
            discard();
            return;
        }
        Vec3 direction = target
                .subtract(entity.position())
                .normalize()
                .scale(MiaConfig.hookPullVelocity);
        Vec3 velocity = entity.getDeltaMovement().add(direction);
        entity.setDeltaMovement(velocity);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        // 不包括持有者
        return super.canHitEntity(target) && target != getPlayer();
    }

    public boolean canHooked() {
        Player player = getPlayer();
        return player != null
                && isShoot()
                && !(distanceToSqr(player) > MiaConfig.hookMaxDistance * MiaConfig.hookMaxDistance);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!canHooked()) return;
        setHookedEntity(result.getEntity());
        setHookState(HookState.HOOKED_ENTITY);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (isBack()) return;
        Vec3 vec3 = result.getLocation().subtract(position());
        setDeltaMovement(vec3);
        if (!canHooked()) return;
        setHookState(HookState.HOOKED_BLOCK);
        hookedBlockPos = result.getBlockPos();
        hookedBlockState = level().getBlockState(hookedBlockPos);
    }

    public enum HookState implements StringRepresentable {
        SHOOT(0, "shoot"),                 // 发射
        BACK(1, "back"),                   // 收回
        HOOKED_BLOCK(2, "hooked_block"),   // 抓住方块
        HOOKED_ENTITY(3, "hooked_entity"); // 抓住实体

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
