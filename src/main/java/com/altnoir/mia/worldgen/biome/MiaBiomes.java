package com.altnoir.mia.worldgen.biome;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.biome.great_fault.GreatFaultBiomes;
import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class MiaBiomes {
    /**
     * 全部生物群系 key。
     * <p>
     * <b>在类初始化时填充</b>（见 {@link #getResourceKey(String)}）而不是 bootstrap 期间：
     * datagen 时各 provider 是并发跑的，MIA 的 lang 数据在 Reginth 的 lang provider 构建时就要求值，
     * 那时如果还没轮到 bootstrap，这份列表就是空的 —— 实测会静默丢掉全部 {@code biome.mia.*} 键。
     */
    public static final List<ResourceKey<Biome>> BIOMES = new ArrayList<>();

    // Layer 1
    public static final ResourceKey<Biome> THE_ABYSS = abyssEdgeKey("the_abyss");
    public static final ResourceKey<Biome> SKYFOG_FOREST = abyssEdgeKey("skyfog_forest");
    public static final ResourceKey<Biome> DENSE_SKYFOG_FOREST =
            abyssEdgeKey("dense_skyfog_forest");
    public static final ResourceKey<Biome> FOSSILIZED_FOREST = abyssEdgeKey("fossilized_forest");
    public static final ResourceKey<Biome> RICH_FOSSILIZED_FOREST =
            abyssEdgeKey("rich_fossilized_forest");
    public static final ResourceKey<Biome> UNDER_FOSSILIZED_FOREST =
            abyssEdgeKey("under_fossilized_forest");
    public static final ResourceKey<Biome> ABYSS_PLAINS = abyssEdgeKey("abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = abyssEdgeKey("prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = abyssEdgeKey("abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_DRIPSTONE_CAVES =
            abyssEdgeKey("abyss_dripstone_caves");
    // Layer 2

    public static final ResourceKey<Biome> PRIMO_FOREST = abyssEdgeKey("primo_forest");
    public static final ResourceKey<Biome> TEMPTATION_FOREST = abyssEdgeKey("temptation_forest");
    public static final ResourceKey<Biome> INVERTED_FOREST = abyssEdgeKey("inverted_forest");
    // Layer 3
    public static final ResourceKey<Biome> THE_GREAT_FAULT = greatFaultKey("the_great_fault");
    public static final ResourceKey<Biome> GREAT_FAULT = greatFaultKey("great_fault");

    public static void boostrap(BootstrapContext<Biome> context) {
        // Layer 1
        register(THE_ABYSS, TheAbyssBiomes.theAbyss(context), context);
        register(SKYFOG_FOREST, TheAbyssBiomes.skyfogForest(context), context);
        register(DENSE_SKYFOG_FOREST, TheAbyssBiomes.denseSkyfogForest(context), context);
        register(FOSSILIZED_FOREST, TheAbyssBiomes.fossilizedForest(context), context);
        register(RICH_FOSSILIZED_FOREST, TheAbyssBiomes.richFossilizedForest(context), context);
        register(UNDER_FOSSILIZED_FOREST, TheAbyssBiomes.UnderfossilizedForest(context), context);
        register(ABYSS_PLAINS, TheAbyssBiomes.abyssPlains(context), context);
        register(PRASIOLITE_CAVES, TheAbyssBiomes.prasioliteCaves(context), context);
        register(ABYSS_LUSH_CAVES, TheAbyssBiomes.abyssLushCaves(context), context);
        register(ABYSS_DRIPSTONE_CAVES, TheAbyssBiomes.abyssDripstoneCaves(context), context);
        // Layer 2
        register(PRIMO_FOREST, TheAbyssBiomes.primoForest(context), context);
        register(TEMPTATION_FOREST, TheAbyssBiomes.temptationForest(context), context);
        register(INVERTED_FOREST, TheAbyssBiomes.invertedForest(context), context);
        // Layer 3
        register(THE_GREAT_FAULT, GreatFaultBiomes.theGreatFault(context), context);
        register(GREAT_FAULT, GreatFaultBiomes.greatFault(context), context);
    }

    private static void register(
            ResourceKey<Biome> key, Biome biome, BootstrapContext<Biome> context) {
        context.register(key, biome);
    }

    private static ResourceKey<Biome> getResourceKey(String path) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, MiaUtil.miaId(path));
        BIOMES.add(key);
        return key;
    }

    private static ResourceKey<Biome> abyssEdgeKey(String path) {
        return getResourceKey("the_abyss/" + path);
    }

    private static ResourceKey<Biome> greatFaultKey(String path) {
        return getResourceKey("great_fault/" + path);
    }
}
