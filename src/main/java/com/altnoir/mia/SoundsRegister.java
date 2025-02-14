package com.altnoir.mia;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundsRegister {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "mia");
    public static final RegistryObject<SoundEvent> STOP = SOUNDS.register("stop", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mia", "stop")));
    public static final RegistryObject<SoundEvent> THROW = SOUNDS.register("knife_throw", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mia", "knife_throw")));
    public static final RegistryObject<SoundEvent> KNIFE_HIT = SOUNDS.register("knife_hit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mia", "knife_hit")));
    public static final RegistryObject<SoundEvent> YOUR_WORTH = SOUNDS.register("your_worth", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mia", "your_worth")));
    public static final RegistryObject<SoundEvent> COMMON_WHISTLE = SOUNDS.register("common_whistle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("mia", "common_whistle")));

}