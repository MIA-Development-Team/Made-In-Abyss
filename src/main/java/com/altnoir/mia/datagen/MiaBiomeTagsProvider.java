package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaBiomeTagsProvider extends BiomeTagsProvider {
    public MiaBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MIA.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        tag(MiaTags.Biomes.HAS_ISLAND)
                .add(Biomes.MUSHROOM_FIELDS)
                .addTags(BiomeTags.IS_OVERWORLD);
        tag(MiaTags.Biomes.THE_ABYSS_CLEAR)
                .add(MiaBiomes.TEMPTATION_FOREST);
        tag(MiaTags.Biomes.HAS_ANCIENT_BABYLON_COMPASS_RUINS)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE);
        tag(MiaTags.Biomes.HAS_ANCIENT_MAYA_COMPASS_RUINS)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE);
        tag(MiaTags.Biomes.HAS_ANCIENT_ROMAN_COMPASS_RUINS)
                .addTags(Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SAVANNA);
        tag(MiaTags.Biomes.HAS_ANCIENT_TRIAL_COMPASS_RUINS)
                .addTag(BiomeTags.HAS_TRIAL_CHAMBERS);
        tag(MiaTags.Biomes.HAS_ANCIENT_ANGKOR_COMPASS_RUINS)
                .addTag(Tags.Biomes.IS_DESERT);
        tag(MiaTags.Biomes.HAS_ABYSS_WINDMILL)
                .add(MiaBiomes.THE_ABYSS)
                .add(MiaBiomes.INVERTED_FOREST);
    }
}
