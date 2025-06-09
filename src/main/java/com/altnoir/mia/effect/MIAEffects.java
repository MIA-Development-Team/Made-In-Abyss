package com.altnoir.mia.effect;

import com.altnoir.mia.MIA;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MIAEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MIA.MOD_ID);
    public static final Holder<MobEffect> ABYSS_CURSE = MOB_EFFECTS.register("abyss_curse", () ->
            new AbyssCurseEffect(MobEffectCategory.HARMFUL, 0x47311A).setBlendDuration(22)
    );
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
