package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.artifact.ArtifactEnhancement;
import com.altnoir.mementoinabyss.content.artifact.ArtifactProfile;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArtifactProfile>> ARTIFACT_PROFILE =
            COMPONENTS.registerComponentType("artifact_profile", builder -> builder
                    .persistent(ArtifactProfile.CODEC)
                    .networkSynchronized(ArtifactProfile.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArtifactEnhancement>> ARTIFACT_ENHANCEMENT =
            COMPONENTS.registerComponentType("artifact_enhancement", builder -> builder
                    .persistent(ArtifactEnhancement.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(ArtifactEnhancement.CODEC)));

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }

    private MiaDataComponents() {}
}
