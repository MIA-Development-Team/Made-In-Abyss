package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class MiaDamageTypes {
    public static final ResourceKey<DamageType> DREAM_LICHEE = createKey("dream_lichee");

    public static void bootstrap(BootstrapContext<DamageType> context) {
        register(context, DREAM_LICHEE, "dreamLichee", 0.1F);
    }

    public static ResourceKey<DamageType> createKey(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, MiaUtil.miaId(name));
    }

    public static Holder.Reference<DamageType> register(BootstrapContext<DamageType> context, ResourceKey<DamageType> key, String localizationKey, float exhaustion) {
        return context.register(key, new DamageType(MIA.MOD_ID + "." + localizationKey, exhaustion));
    }
}
