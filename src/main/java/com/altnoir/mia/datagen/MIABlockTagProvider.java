package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MIABlockTagProvider extends BlockTagsProvider {
    public MIABlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MOSS_REPLACEABLE)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get());

        tag(BlockTags.DIRT)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get());
    }
}
