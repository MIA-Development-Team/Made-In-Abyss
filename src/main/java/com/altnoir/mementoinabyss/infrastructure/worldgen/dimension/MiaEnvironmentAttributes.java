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
