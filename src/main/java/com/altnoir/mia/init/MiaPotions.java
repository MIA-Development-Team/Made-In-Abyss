package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, MIA.MOD_ID);

    public static final Holder<Potion> HASTE = POTIONS.register("haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 3600, 0)));
    public static final Holder<Potion> LONG_HASTE = POTIONS.register("long_haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 9600, 0)));
    public static final Holder<Potion> STRONG_HASTE = POTIONS.register("strong_haste",
            () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1)));

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }
}
