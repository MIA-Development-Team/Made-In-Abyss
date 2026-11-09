package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.init.MiaTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public final class MiaBiomeTagsProvider extends BiomeTagsProvider {
    public MiaBiomeTagsProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, MementoInAbyss.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BiomeTags.IS_OCEAN).add(MiaBiomes.WARM_OCEAN).add(MiaBiomes.TEMPTATION_OCEAN);
        tag(BiomeTags.WATER_ON_MAP_OUTLINES)
                .add(MiaBiomes.WARM_OCEAN)
                .add(MiaBiomes.TEMPTATION_OCEAN);
        tag(MiaTags.Biomes.AMBIENT_LIGHT_DARK.tag).add(MiaBiomes.DUSKVEIL_GARDEN);
        tag(MiaTags.Biomes.ABYSS_FOG_CLEAR.tag)
                .add(MiaBiomes.GIANT_FOSSILIZED_FOREST)
                .add(MiaBiomes.SPARSE_GIANT_FOSSILIZED_FOREST);
        tag(MiaTags.Biomes.ABYSS_FOG_DENSE.tag).add(MiaBiomes.DUSKVEIL_GARDEN);
        tag(MiaTags.Biomes.HAS_ABYSS_WINDMILL.tag)
                .add(MiaBiomes.THE_ABYSS)
                .add(MiaBiomes.INVERTED_FOREST);
        tag(MiaTags.Biomes.HAS_ABYSSAL_RUINS.tag)
                .add(MiaBiomes.THE_ABYSS)
                .add(MiaBiomes.SKYFOG_FOREST)
                .add(MiaBiomes.DENSE_SKYFOG_FOREST)
                .add(MiaBiomes.FOSSILIZED_FOREST)
                .add(MiaBiomes.RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.UNDER_FOSSILIZED_FOREST)
                .add(MiaBiomes.UNDER_RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.UNDER_WATER_FOSSILIZED_FOREST)
                .add(MiaBiomes.ABYSS_PLAINS)
                .add(MiaBiomes.PRASIOLITE_CAVES)
                .add(MiaBiomes.ABYSS_LUSH_CAVES)
                .add(MiaBiomes.ABYSS_TUFF_CAVES)
                .add(MiaBiomes.TEMPTATION_FOREST)
                .add(MiaBiomes.TEMPTATION_PLAINS)
                .add(MiaBiomes.PRIMO_FOREST)
                .add(MiaBiomes.PRIMO_PLAINS)
                .add(MiaBiomes.INVERTED_FOREST);
        tag(MiaTags.Biomes.HAS_ANCIENT_ANGKOR_COMPASS_RUINS.tag).addTag(Tags.Biomes.IS_DESERT);
        tag(MiaTags.Biomes.HAS_ANCIENT_BABYLON_COMPASS_RUINS.tag)
                .add(net.minecraft.world.level.biome.Biomes.JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE);
        tag(MiaTags.Biomes.HAS_ANCIENT_MAYA_COMPASS_RUINS.tag)
                .add(net.minecraft.world.level.biome.Biomes.JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE);
        tag(MiaTags.Biomes.HAS_ANCIENT_ROMAN_COMPASS_RUINS.tag)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(Tags.Biomes.IS_SAVANNA);
        tag(MiaTags.Biomes.HAS_ANCIENT_TRIAL_COMPASS_RUINS.tag)
                .addTag(BiomeTags.HAS_TRIAL_CHAMBERS);
        tag(MiaTags.Biomes.HAS_CAVE_RAIDER_HUT.tag)
                .add(MiaBiomes.TEMPTATION_FOREST)
                .add(MiaBiomes.TEMPTATION_PLAINS);
        tag(MiaTags.Biomes.HAS_FISHERMAN_HUT.tag)
                .add(MiaBiomes.SKYFOG_FOREST)
                .add(MiaBiomes.DENSE_SKYFOG_FOREST)
                .add(MiaBiomes.FOSSILIZED_FOREST)
                .add(MiaBiomes.RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.UNDER_RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.ABYSS_PLAINS);
        tag(MiaTags.Biomes.HAS_ISLAND.tag)
                .add(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS)
                .addTag(BiomeTags.IS_OVERWORLD);
        tag(MiaTags.Biomes.HAS_PETRIFIED_SHIP.tag)
                .add(MiaBiomes.THE_GREAT_FAULT)
                .add(MiaBiomes.GREAT_FAULT);
        tag(MiaTags.Biomes.HAS_RUINED_CAVE_RAIDER_HUT.tag).add(MiaBiomes.ABYSS_PLAINS);
        tag(MiaTags.Biomes.HAS_STAR_COMPASS_TEMPLE.tag)
                .add(net.minecraft.world.level.biome.Biomes.JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE)
                .add(net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP);
        tag(BiomeTags.IS_RIVER).add(MiaBiomes.ABYSS_RIVER);
        tag(BiomeTags.SPAWNS_WARM_VARIANT_FROGS).add(MiaBiomes.MANGROVE_SWAMP);
        tag(BiomeTags.WATER_ON_MAP_OUTLINES).add(MiaBiomes.MANGROVE_SWAMP);
    }
}
