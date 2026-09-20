package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.foundation.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.foundation.transfer.heat.HeatType;
import com.tterrag.registrate.util.entry.RegistryEntry;

public final class MiaHeatTypes {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.makeHeatTypeRegistry();
    }

    public static final RegistryEntry<HeatType, HeatType> EMPTY =
            REGISTRATE.heatType("empty", () -> HeatType.EMPTY).register();

    public static final RegistryEntry<HeatType, HeatType> HEAT =
            REGISTRATE.heatType("heat", () -> new HeatType(0xE25822)).register();

    public static void register() {}

    private MiaHeatTypes() {}
}
