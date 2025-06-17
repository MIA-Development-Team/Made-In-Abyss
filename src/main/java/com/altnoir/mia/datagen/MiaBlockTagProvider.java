package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MiaBlocks;
import com.altnoir.mia.tag.MiaBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaBlockTagProvider extends BlockTagsProvider {
    public MiaBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //深渊标签
        tag(MiaBlockTags.ANDESITE_ORE_REPLACEABLES)
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(MiaBlockTags.BASE_STONE_ABYSS)
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(MiaBlockTags.COVERGRASS)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());

        //基础标签
        tag(BlockTags.DIRT)
                .add(MiaBlocks.ABYSS_GRASS_BLOCK.get())
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());

        tag(BlockTags.MOSS_REPLACEABLE)
                .add(MiaBlocks.ABYSS_GRASS_BLOCK.get())
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(BlockTags.FLOWERS)
                .add(MiaBlocks.FORTITUDE_FLOWER.get());

        //工具标签
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(MiaBlocks.ABYSS_GRASS_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get());

        //标签嵌套
        tag(BlockTags.SCULK_REPLACEABLE)
                .addTag(MiaBlockTags.ANDESITE_ORE_REPLACEABLES);
    }
}
