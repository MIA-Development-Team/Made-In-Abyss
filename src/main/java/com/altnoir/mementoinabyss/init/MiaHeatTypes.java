package com.altnoir.mementoinabyss.init;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.builders.NoConfigBuilder;
import com.altnoir.abysslib.reginth.util.entry.RegistryEntry;
import com.altnoir.abysslib.reginth.util.nullness.NonNullSupplier;
import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class MiaHeatTypes {
    private static final Reginth REGISTRATE = MementoInAbyss.registrate();

    static {
        makeHeatTypeRegistry();
    }

    public static final RegistryEntry<HeatType, HeatType> EMPTY =
            heatType("empty", () -> HeatType.EMPTY).register();

    public static final RegistryEntry<HeatType, HeatType> HEAT =
            heatType("heat", () -> new HeatType(0xE25822)).register();

    /** Declares the synced heat_type registry; must run before the register events fire. */
    private static ResourceKey<Registry<HeatType>> makeHeatTypeRegistry() {
        return REGISTRATE.makeRegistry(
                HeatType.REGISTRY_KEY.identifier().getPath(),
                key ->
                        new RegistryBuilder<HeatType>(key)
                                .sync(true)
                                .defaultKey(MementoInAbyss.asResource("empty")));
    }

    private static NoConfigBuilder<HeatType, HeatType, Reginth> heatType(
            String name, NonNullSupplier<HeatType> factory) {
        return REGISTRATE
                .generic(REGISTRATE, name, HeatType.REGISTRY_KEY, factory)
                .lang(HeatType::getDescriptionId);
    }

    public static void register() {}

    private MiaHeatTypes() {}
}
