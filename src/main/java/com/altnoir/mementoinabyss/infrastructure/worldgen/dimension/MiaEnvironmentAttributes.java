package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.attribute.AttributeRange;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaEnvironmentAttributes {
    private static final DeferredRegister<EnvironmentAttribute<?>> ENVIRONMENT_ATTRIBUTES =
            DeferredRegister.create(Registries.ENVIRONMENT_ATTRIBUTE, MementoInAbyss.ID);
    private static final VerticalBoundary UNSET = new VerticalBoundary(Level.OVERWORLD, 0.0);

    public static final EnvironmentAttribute<VerticalBoundary> VERTICAL_BELOW =
            boundary("vertical_below");
    public static final EnvironmentAttribute<VerticalBoundary> VERTICAL_ABOVE =
            boundary("vertical_above");

    public static final EnvironmentAttribute<Float> ABYSS_FOG_DENSITY = fogDensity();
    public static final EnvironmentAttribute<SkyLightRegionSettings> SKY_LIGHT_REGION =
            skyLightRegion();
    public static final EnvironmentAttribute<BiomeAmbientLightSettings> AMBIENT_LIGHT_BIOME =
            biomeAmbientLight();

    private static EnvironmentAttribute<BiomeAmbientLightSettings> biomeAmbientLight() {
        EnvironmentAttribute<BiomeAmbientLightSettings> attribute =
                EnvironmentAttribute.builder(
                                AttributeType.ofNotInterpolated(BiomeAmbientLightSettings.CODEC))
                        .defaultValue(BiomeAmbientLightSettings.EMPTY)
                        .syncable()
                        .notPositional()
                        .build();
        ENVIRONMENT_ATTRIBUTES.register("ambient_light_biome", () -> attribute);
        return attribute;
    }

    private static EnvironmentAttribute<SkyLightRegionSettings> skyLightRegion() {
        EnvironmentAttribute<SkyLightRegionSettings> attribute =
                EnvironmentAttribute.builder(
                                AttributeType.ofNotInterpolated(SkyLightRegionSettings.CODEC))
                        .defaultValue(new SkyLightRegionSettings(0, 0, 0, 0))
                        .syncable()
                        .notPositional()
                        .build();
        ENVIRONMENT_ATTRIBUTES.register("sky_light_region", () -> attribute);
        return attribute;
    }

    private static EnvironmentAttribute<Float> fogDensity() {
        EnvironmentAttribute<Float> attribute =
                EnvironmentAttribute.builder(AttributeTypes.FLOAT)
                        .defaultValue(0.0F)
                        .valueRange(AttributeRange.UNIT_FLOAT)
                        .spatiallyInterpolated()
                        .syncable()
                        .build();
        ENVIRONMENT_ATTRIBUTES.register("abyss_fog_density", () -> attribute);
        return attribute;
    }

    private static EnvironmentAttribute<VerticalBoundary> boundary(String name) {
        EnvironmentAttribute<VerticalBoundary> attribute =
                EnvironmentAttribute.builder(
                                AttributeType.ofNotInterpolated(VerticalBoundary.CODEC))
                        .defaultValue(UNSET)
                        .syncable()
                        .notPositional()
                        .build();
        ENVIRONMENT_ATTRIBUTES.register(name, () -> attribute);
        return attribute;
    }

    public static void register(IEventBus bus) {
        ENVIRONMENT_ATTRIBUTES.register(bus);
    }

    private MiaEnvironmentAttributes() {}
}
