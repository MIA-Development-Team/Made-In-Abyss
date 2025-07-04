package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MIA.MOD_ID);

    public static final Supplier<SimpleParticleType> SKYFOG_LEAVES = PARTICLE_TYPES.register("skyfog_leaves", () ->
            new SimpleParticleType(false));
     public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
