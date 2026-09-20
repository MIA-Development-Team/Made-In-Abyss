package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.effect.AbyssBlessingEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MiaEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MementoInAbyss.ID);

    public static final Holder<MobEffect> ABYSS_BLESSING =
            EFFECTS.register(
                    "abyss_blessing",
                    () -> new AbyssBlessingEffect(MobEffectCategory.BENEFICIAL, 0xA6E467));

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }

    private MiaEffects() {}
}
