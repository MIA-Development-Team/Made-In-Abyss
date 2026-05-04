package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.*;

public class MiaItemGroups {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> BASE = REGISTRATE.object("base")
            .creativeTab(tab -> {})
            .register();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> ARTIFACT = REGISTRATE.object("artifact")
            .creativeTab(tab -> {})
            .register();

    public static void register() {}
}
