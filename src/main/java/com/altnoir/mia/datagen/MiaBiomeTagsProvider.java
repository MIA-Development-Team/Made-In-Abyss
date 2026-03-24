package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
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
        tag(MiaTags.Biomes.HAS_STAR_COMPASS_TEMPLE)
                .add(Biomes.JUNGLE)
                .add(Biomes.SPARSE_JUNGLE)
                .add(Biomes.BAMBOO_JUNGLE)
                .add(Biomes.MANGROVE_SWAMP);
    }
}
