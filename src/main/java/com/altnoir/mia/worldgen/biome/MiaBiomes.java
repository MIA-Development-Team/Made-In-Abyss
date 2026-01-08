package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class MiaBiomes {
    public static final ResourceKey<Biome> THE_ABYSS = abyssEdgeKey("the_abyss");
    public static final ResourceKey<Biome> SKYFOG_FOREST = abyssEdgeKey("skyfog_forest");
    public static final ResourceKey<Biome> ABYSS_PLAINS = abyssEdgeKey("abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = abyssEdgeKey("prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = abyssEdgeKey("abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_DRIPSTONE_CAVES = abyssEdgeKey("abyss_dripstone_caves");

    public static void boostrap(BootstrapContext<Biome> context) {
        context.register(THE_ABYSS, TheAbyssBiomes.theAbyss(context));
        context.register(SKYFOG_FOREST, TheAbyssBiomes.skyfogForest(context));
        context.register(ABYSS_PLAINS, TheAbyssBiomes.abyssPlains(context));
        context.register(PRASIOLITE_CAVES, TheAbyssBiomes.prasioliteCaves(context));
        context.register(ABYSS_LUSH_CAVES, TheAbyssBiomes.abyssLushCaves(context));
        context.register(ABYSS_DRIPSTONE_CAVES, TheAbyssBiomes.abyssDripstoneCaves(context));
    }

    private static ResourceKey<Biome> register(String path) {
        return ResourceKey.create(Registries.BIOME, MiaUtil.miaId(path));
    }

    private static ResourceKey<Biome> abyssEdgeKey(String path) {
        return register("the_abyss/" + path);
    }
}
