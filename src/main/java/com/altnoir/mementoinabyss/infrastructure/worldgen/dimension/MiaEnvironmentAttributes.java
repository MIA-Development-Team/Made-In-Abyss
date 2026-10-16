package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.attribute.AttributeType;
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
