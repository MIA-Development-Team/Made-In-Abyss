package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENT = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MIA.MOD_ID);

public static final Supplier<SoundEvent> ABYSS_PORTAL_AMBIENT = registerSoundEvent("abyss_portal_ambient");
public static final Supplier<SoundEvent> ABYSS_PORTAL_TRAVEL = registerSoundEvent("abyss_portal_travel");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENT.register(name, () -> SoundEvent.createVariableRangeEvent(MiaUtil.miaId(name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENT.register(bus);
    }
}
