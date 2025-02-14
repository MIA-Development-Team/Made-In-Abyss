package com.altnoir.mia;

import com.altnoir.mia.content.effect.PerspectiveEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectsRegister {
    private static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MIA.MOD_ID);

    public static final RegistryObject<MobEffect> PERSPECTIVE = REGISTER.register("perspective", PerspectiveEffect::new);

    public static void register(IEventBus modEventBus){
        REGISTER.register(modEventBus);
    }
}
