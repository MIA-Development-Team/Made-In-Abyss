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
    public static final int PARCEL_FLIGHT_HEIGHT = 20;
    private static final int CHEST_FLIGHT_HEIGHT = 16;
    private static final int CONSTANT_SPEED_TICKS = 80;
    private static final int ACCELERATION_TICKS = 20;
    private static final int PARCEL_FLIGHT_TICKS = CONSTANT_SPEED_TICKS + ACCELERATION_TICKS / 2;
    public static final int FADE_TICKS = 20;
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MementoInAbyss.ID);
    public static final DeferredHolder<EntityType<?>, EntityType<BalloonCargo>> CHEST_BALLOON =
            registerType("chest_balloon", 0.95F, 2.0F);
    public static final DeferredHolder<EntityType<?>, EntityType<BalloonCargo>> PARCEL_BALLOON =
            registerType("parcel_balloon", 2.0F, 4.0F);

    private static DeferredHolder<EntityType<?>, EntityType<BalloonCargo>> registerType(
            String name, float width, float height) {
        return ENTITIES.register(
                name,
                id ->
                        EntityType.Builder.of(BalloonCargo::new, MobCategory.MISC)
                                .sized(width, height)
                                .updateInterval(1)
                                .noSummon()
                                .noLootTable()
                                .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(BalloonCargo.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> FLIGHT_TICKS =
            SynchedEntityData.defineId(BalloonCargo.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ACCELERATES =
            SynchedEntityData.defineId(BalloonCargo.class, EntityDataSerializers.BOOLEAN);

    private final InterpolationHandler interpolation = new InterpolationHandler(this);

    public BalloonCargo(EntityType<? extends BalloonCargo> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static void register(IEventBus bus) {
        ENTITIES.addAlias(
                MementoInAbyss.asResource("balloon_cargo"), CHEST_BALLOON.getId());
        ENTITIES.register(bus);
    }

    void prepare(BlockPos pos, Direction facing, DyeColor color) {
        snapTo(pos, facing.toYRot(), 0.0F);
        entityData.set(ACCELERATES, getType() == PARCEL_BALLOON.get());
        entityData.set(COLOR, color.getId());
    }

    public float elapsedFlightTicks(float partialTick) {
        return entityData.get(FLIGHT_TICKS) + partialTick;
    }

    public int flightDuration() {
        return entityData.get(ACCELERATES) ? PARCEL_FLIGHT_TICKS : CONSTANT_SPEED_TICKS;
    }

    private double ascentPerTick(int elapsedTicks) {
        int height = getType() == PARCEL_BALLOON.get() ? PARCEL_FLIGHT_HEIGHT : CHEST_FLIGHT_HEIGHT;
        double cruiseSpeed = (double) height / CONSTANT_SPEED_TICKS;
        if (entityData.get(ACCELERATES) && elapsedTicks < ACCELERATION_TICKS) {
            // The midpoint gives the distance traveled during this tick under linear acceleration.
            return cruiseSpeed * (elapsedTicks + 0.5) / ACCELERATION_TICKS;
        }
        return cruiseSpeed;
    }
    public DyeColor color() {
        return DyeColor.byId(entityData.get(COLOR));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        data.define(COLOR, DyeColor.WHITE.getId());
        data.define(FLIGHT_TICKS, 0);
        data.define(ACCELERATES, false);
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
            } else {
                int elapsed = entityData.get(FLIGHT_TICKS);
                if (elapsed < flightDuration()) {
                    setPos(getX(), getY() + ascentPerTick(elapsed), getZ());
                    entityData.set(FLIGHT_TICKS, elapsed + 1);
                }
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
        entityData.set(FLIGHT_TICKS, input.getIntOr("flight_ticks", 0));
        entityData.set(ACCELERATES, input.getBooleanOr("accelerates", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("color", color().getId());
        output.putInt("flight_ticks", entityData.get(FLIGHT_TICKS));
        output.putBoolean("accelerates", entityData.get(ACCELERATES));
    }
}
