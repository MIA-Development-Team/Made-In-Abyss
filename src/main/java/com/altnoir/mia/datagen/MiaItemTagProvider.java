package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.compat.curios.CuriosItemTags;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaItemTagProvider extends ItemTagsProvider {
    public MiaItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CuriosItemTags.WHISTLE).replace(false).add(MiaItems.RED_WHISTLE.get());

        tag(ItemTags.LOGS_THAT_BURN)
                .add(MiaBlocks.SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.SKYFOG_WOOD.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get().asItem())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get().asItem());

        tag(ItemTags.PLANKS)
                .add(MiaBlocks.SKYFOG_PLANKS.get().asItem());
    }
}
