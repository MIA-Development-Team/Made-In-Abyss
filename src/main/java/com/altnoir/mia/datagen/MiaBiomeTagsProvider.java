package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaBiomeTagsProvider extends BiomeTagsProvider {
    public MiaBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MiaTags.Biomes.THE_ABYSS_CLEAR)
                .add(MiaBiomes.TEMPTATION_FOREST);
        tag(MiaTags.Biomes.THE_ABYSS_DENSE)
                .add(MiaBiomes.ABYSS_DRIPSTONE_CAVES);
    }
}
