package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.abyss_brink.AbyssBrinkBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class MiaBiomes {
    public static final ResourceKey<Biome> ABYSS_BRINK = register("abyss_brink/abyss_brink");
    public static final ResourceKey<Biome> SKYFOG_FOREST = register("abyss_brink/skyfog_forest");
    public static final ResourceKey<Biome> ABYSS_PLAINS = register("abyss_brink/abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = register("abyss_brink/prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = register("abyss_brink/abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_DRIPSTONE_CAVES = register("abyss_brink/abyss_dripstone_caves");

    public static void boostrap(BootstrapContext<Biome> context) {
        context.register(ABYSS_BRINK, AbyssBrinkBiomes.abyssBrink(context));
        context.register(SKYFOG_FOREST, AbyssBrinkBiomes.skyfogForest(context));
        context.register(ABYSS_PLAINS, AbyssBrinkBiomes.abyssPlains(context));
        context.register(PRASIOLITE_CAVES, AbyssBrinkBiomes.prasioliteCaves(context));
        context.register(ABYSS_LUSH_CAVES, AbyssBrinkBiomes.abyssLushCaves(context));
        context.register(ABYSS_DRIPSTONE_CAVES, AbyssBrinkBiomes.abyssDripstoneCaves(context));
    }

    private static ResourceKey<Biome> register(String path) {
        return ResourceKey.create(Registries.BIOME, MiaUtil.miaId(path));
    }
}
