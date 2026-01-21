package com.altnoir.mia.entity.projectile;

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
    // 20格距离
    public final float hookRangeSqr = 20 * 20;
    protected BlockPos hookPos;
    protected BlockState hookedState;

    public HookEntity(EntityType<? extends HookEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HookEntity(Player player, InteractionHand hand) {
        super(MiaEntities.HOOK.get(), player.level());
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
        builder.define(DATA_HOOK_STATE, 0).define(DATA_SHOOT_HAND, true);
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
        HookState hookState = getHookState();
        if (hookState == HookState.BACK) {
            setDeltaMovement(
                    getDeltaMovement()
                            .scale(0.95)
                            .add(
                                    player.position()
                                            .add(0, player.getEyeHeight() - 0.1, 0)
                                            .subtract(position())
                                            .normalize()
                                            // 收回速度
                                            .scale(0.75)
                            )
            );
            // 有时会出现延迟销毁的bug，目前没诊断出原因，但触发概率不大
            if (distanceToSqr(player) < 4.0) {
                discard();
                return;
            }
        }
        if (!level().isClientSide()) {
            // 由于HookHandler的触发先于此tick逻辑，导致HookHandler先一步判断能够拉取，然后此处判断收回
            // HookHandler需要重构
            if (hookState != HookState.BACK && distanceToSqr(player) > hookRangeSqr) {
                setHookState(HookState.BACK);
            } else if (hookState == HookState.HOOKED && (hookPos == null || level().getBlockState(hookPos) != hookedState)) {
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
        if (hookState == HookState.SHOOT) {
            setHookState(HookState.HOOKED);
            hookPos = result.getBlockPos();
            hookedState = level().getBlockState(hookPos);
        }
    }

    public enum HookState implements StringRepresentable {
        SHOOT(0, "shoot"), // 发射
        BACK(1, "back"), // 收回
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
