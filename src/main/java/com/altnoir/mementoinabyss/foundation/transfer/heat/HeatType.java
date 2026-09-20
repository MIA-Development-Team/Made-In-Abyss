package com.altnoir.mementoinabyss.foundation.transfer.heat;

import com.altnoir.mementoinabyss.MementoInAbyss;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Registry entry for a kind of heat. Parallel to {@code Fluid}, but with no world block, entity, or
 * bucket interaction. Amounts live on {@link HeatStack} / {@link HeatResource} handlers, not here.
 */
@Getter
public final class HeatType {
    /** One "portion" of heat, same magnitude as a fluid bucket ({@code 1000} mB). */
    public static final int UNIT = 1000;

    public static final ResourceKey<Registry<HeatType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(MementoInAbyss.asResource("heat_type"));

    public static final HeatType EMPTY = new HeatType(0, true);

    private final int color;
    private final boolean empty;

    public HeatType(int color) {
        this(color, false);
    }

    private HeatType(int color, boolean empty) {
        this.color = color;
        this.empty = empty;
    }

    @SuppressWarnings("unchecked")
    public static Registry<HeatType> registry() {
        var registry = BuiltInRegistries.REGISTRY.getValue(REGISTRY_KEY.identifier());
        if (registry == null) {
            throw new IllegalStateException("Heat type registry is not ready");
        }
        return (Registry<HeatType>) registry;
    }

    public String getDescriptionId() {
        Identifier id = empty ? MementoInAbyss.asResource("empty") : registry().getKey(this);
        if (id == null) {
            return "heat_type.mementoinabyss.unknown";
        }
        return "heat_type." + id.getNamespace() + "." + id.getPath();
    }
}
