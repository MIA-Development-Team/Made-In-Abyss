package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class MiaBiomes {
    // Layer 1
    public static final ResourceKey<Biome> THE_ABYSS = abyssEdgeKey("the_abyss");
    public static final ResourceKey<Biome> SKYFOG_FOREST = abyssEdgeKey("skyfog_forest");
    public static final ResourceKey<Biome> FOSSILIZED_FOREST = abyssEdgeKey("fossilized_forest");
    public static final ResourceKey<Biome> UNDER_FOSSILIZED_FOREST = abyssEdgeKey("under_fossilized_forest");
    public static final ResourceKey<Biome> ABYSS_PLAINS = abyssEdgeKey("abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = abyssEdgeKey("prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = abyssEdgeKey("abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_DRIPSTONE_CAVES = abyssEdgeKey("abyss_dripstone_caves");
    // Layer 2
    public static final ResourceKey<Biome> TEMPTATION_FOREST = abyssEdgeKey("temptation_forest");
    public static final ResourceKey<Biome> INVERTED_FOREST = abyssEdgeKey("inverted_forest");

    public static void boostrap(BootstrapContext<Biome> context) {
        // Layer 1
        context.register(THE_ABYSS, TheAbyssBiomes.theAbyss(context));
        context.register(SKYFOG_FOREST, TheAbyssBiomes.skyfogForest(context));
        context.register(FOSSILIZED_FOREST, TheAbyssBiomes.fossilizedForest(context));
        context.register(UNDER_FOSSILIZED_FOREST, TheAbyssBiomes.UnderfossilizedForest(context));
        context.register(ABYSS_PLAINS, TheAbyssBiomes.abyssPlains(context));
        context.register(PRASIOLITE_CAVES, TheAbyssBiomes.prasioliteCaves(context));
        context.register(ABYSS_LUSH_CAVES, TheAbyssBiomes.abyssLushCaves(context));
        context.register(ABYSS_DRIPSTONE_CAVES, TheAbyssBiomes.abyssDripstoneCaves(context));
        // Layer 2
        context.register(TEMPTATION_FOREST, TheAbyssBiomes.temptationForest(context));
        context.register(INVERTED_FOREST, TheAbyssBiomes.invertedForest(context));
    }

    private static ResourceKey<Biome> register(String path) {
        return ResourceKey.create(Registries.BIOME, MiaUtil.miaId(path));
    }

    private static ResourceKey<Biome> abyssEdgeKey(String path) {
        return register("the_abyss/" + path);
    }
}
