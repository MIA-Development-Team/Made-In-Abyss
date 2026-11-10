package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BalloonCargo extends Entity {
    static final int FLIGHT_HEIGHT = 16;
    static final int FLIGHT_TICKS = 80;
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MementoInAbyss.ID);
    public static final DeferredHolder<EntityType<?>, EntityType<BalloonCargo>> TYPE =
            ENTITIES.register(
                    "balloon_cargo",
                    id ->
                            EntityType.Builder.of(BalloonCargo::new, MobCategory.MISC)
                                    .sized(0.95F, 2.0F)
                                    .updateInterval(1)
                                    .noSummon()
                                    .noLootTable()
                                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(BalloonCargo.class, EntityDataSerializers.INT);

    private final InterpolationHandler interpolation = new InterpolationHandler(this);

    private int flightTicks;

    public BalloonCargo(EntityType<? extends BalloonCargo> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    void prepare(BlockPos pos, Direction facing, DyeColor color) {
        snapTo(pos, facing.toYRot(), 0.0F);
        entityData.set(COLOR, color.getId());
    }

    public DyeColor color() {
        return DyeColor.byId(entityData.get(COLOR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(COLOR, DyeColor.WHITE.getId());
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return interpolation;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (!ReturnStorage.get(server).isFlying(color(), getUUID())) {
                discard();
            } else if (flightTicks < FLIGHT_TICKS) {
                setPos(getX(), getY() + (double) FLIGHT_HEIGHT / FLIGHT_TICKS, getZ());
                flightTicks++;
            }
        } else {
            interpolation.interpolate();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        entityData.set(COLOR, input.getIntOr("color", DyeColor.WHITE.getId()));
        flightTicks = input.getIntOr("flight_ticks", 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("color", color().getId());
        output.putInt("flight_ticks", flightTicks);
    }
}
