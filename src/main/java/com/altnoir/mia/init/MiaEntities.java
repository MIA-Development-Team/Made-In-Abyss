package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(Registries.ENTITY_TYPE, MIA.MOD_ID);

    // public static final DeferredHolder<EntityType<?>, EntityType<BeamEntity>>
    // BEAM_ENTITY =
    // ENTITY_TYPES.register("beam_entity",
    // () -> EntityType.Builder.<BeamEntity>of(BeamEntity::new, MobCategory.MISC)
    // .sized(1.0F, 1.0F).clientTrackingRange(32).updateInterval(1)
    // .build("beam_entity"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
