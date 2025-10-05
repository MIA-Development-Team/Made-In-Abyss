package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.MIA;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.abyss_brink.AbyssBrinkBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class MiaBiomes {
    public static final ResourceKey<Biome> ABYSS_BRINK = ResourceKey.create(Registries.BIOME,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink/abyss_brink"));
    public static final ResourceKey<Biome> SKYFOG_FOREST = ResourceKey.create(Registries.BIOME,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink/skyfog_forest"));
    public static final ResourceKey<Biome> ABYSS_PLAINS = ResourceKey.create(Registries.BIOME,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink/abyss_plains"));
    public static final ResourceKey<Biome> PRASIOLITE_CAVE = ResourceKey.create(Registries.BIOME,
            MiaUtil.id(MIA.MOD_ID, "abyss_brink/prasiolite_cave"));

    public static void boostrap(BootstrapContext<Biome> context) {
        context.register(ABYSS_BRINK, AbyssBrinkBiomes.abyssBrink(context));
        context.register(SKYFOG_FOREST, AbyssBrinkBiomes.skyfogForest(context));
        context.register(ABYSS_PLAINS, AbyssBrinkBiomes.abyssPlains(context));
        context.register(PRASIOLITE_CAVE, AbyssBrinkBiomes.prasioliteCave(context));
    }
}
