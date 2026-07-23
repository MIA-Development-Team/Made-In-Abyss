package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

public class MiaSoundEvents {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    public static final RegistryEntry<SoundEvent, SoundEvent> ABYSS_PORTAL_TRAVEL = REGISTRATE.simple(
            "block.abyss_portal.travel",
            Registries.SOUND_EVENT,
            () -> SoundEvent.createVariableRangeEvent(MementoInAbyss.asResource("block.abyss_portal.travel"))
    );

    public static void register() {}
}
