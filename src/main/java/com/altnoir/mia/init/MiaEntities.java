package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.entity.projectile.HookEntity;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(Registries.ENTITY_TYPE, MIA.MOD_ID);

    // public static final DeferredHolder<EntityType<?>, EntityType<BeamEntity>>
    // BEAM_ENTITY =
    // ENTITY_TYPES.register("beam_entity",
    // () -> EntityType.Builder.<BeamEntity>of(BeamEntity::new, MobCategory.MISC)
    // .sized(1.0F, 1.0F).clientTrackingRange(32).updateInterval(1)
    // .build("beam_entity"));

    public static final Supplier<EntityType<HookEntity>> HOOK = registerHook(
            "hook",
            EntityType.Builder.<HookEntity>of(HookEntity::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(5)
    );

    public static <T extends Entity> Supplier<EntityType<T>> registerHook(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(MiaUtil.id(name).toString()));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
