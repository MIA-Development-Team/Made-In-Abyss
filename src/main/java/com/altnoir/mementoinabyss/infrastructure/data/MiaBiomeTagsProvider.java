package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.init.MiaTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

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
        tag(MiaTags.BiomeTags.AMBIENT_LIGHT_DARK.tag).add(MiaBiomes.PALE_GARDEN);
        tag(MiaTags.BiomeTags.HAS_ABYSS_WINDMILL.tag)
                .add(MiaBiomes.THE_ABYSS)
                .add(MiaBiomes.INVERTED_FOREST);
        tag(MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag)
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
        tag(MiaTags.BiomeTags.HAS_ANCIENT_ANGKOR_COMPASS_RUINS.tag).addTag(Tags.Biomes.IS_DESERT);
        tag(MiaTags.BiomeTags.HAS_ANCIENT_BABYLON_COMPASS_RUINS.tag)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE);
        tag(MiaTags.BiomeTags.HAS_ANCIENT_MAYA_COMPASS_RUINS.tag)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE);
        tag(MiaTags.BiomeTags.HAS_ANCIENT_ROMAN_COMPASS_RUINS.tag)
                .addTag(Tags.Biomes.IS_PLAINS)
                .addTag(Tags.Biomes.IS_SAVANNA);
        tag(MiaTags.BiomeTags.HAS_ANCIENT_TRIAL_COMPASS_RUINS.tag)
                .addTag(BiomeTags.HAS_TRIAL_CHAMBERS);
        tag(MiaTags.BiomeTags.HAS_CAVE_RAIDER_HUT.tag)
                .add(MiaBiomes.TEMPTATION_FOREST)
                .add(MiaBiomes.TEMPTATION_PLAINS);
        tag(MiaTags.BiomeTags.HAS_FISHERMAN_HUT.tag)
                .add(MiaBiomes.SKYFOG_FOREST)
                .add(MiaBiomes.DENSE_SKYFOG_FOREST)
                .add(MiaBiomes.FOSSILIZED_FOREST)
                .add(MiaBiomes.RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.UNDER_RICH_FOSSILIZED_FOREST)
                .add(MiaBiomes.ABYSS_PLAINS);
        tag(MiaTags.BiomeTags.HAS_ISLAND.tag)
                .add(Biomes.MUSHROOM_FIELDS)
                .addTag(BiomeTags.IS_OVERWORLD);
        tag(MiaTags.BiomeTags.HAS_PETRIFIED_SHIP.tag)
                .add(MiaBiomes.THE_GREAT_FAULT)
                .add(MiaBiomes.GREAT_FAULT);
        tag(MiaTags.BiomeTags.HAS_RUINED_CAVE_RAIDER_HUT.tag).add(MiaBiomes.ABYSS_PLAINS);
        tag(MiaTags.BiomeTags.HAS_STAR_COMPASS_TEMPLE.tag)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE)
                .add(Biomes.BAMBOO_JUNGLE)
                .add(Biomes.MANGROVE_SWAMP);
        tag(BiomeTags.IS_RIVER).add(MiaBiomes.ABYSS_RIVER);
        tag(BiomeTags.SPAWNS_WARM_VARIANT_FROGS).add(MiaBiomes.MANGROVE_SWAMP);
        tag(BiomeTags.WATER_ON_MAP_OUTLINES).add(MiaBiomes.MANGROVE_SWAMP);
    }
}
