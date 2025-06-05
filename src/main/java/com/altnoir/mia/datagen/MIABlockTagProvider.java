package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.tag.MIABlockTags;
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
        //深渊标签
        tag(MIABlockTags.ANDESITE_ORE_REPLACEABLES)
                .add(MIABlocks.ABYSS_ANDESITE.get());

        tag(MIABlockTags.BASE_STONE_ABYSS)
                .add(MIABlocks.ABYSS_ANDESITE.get());

        //基础标签
        tag(BlockTags.DIRT)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get());

        tag(BlockTags.MOSS_REPLACEABLE)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get())
                .add(MIABlocks.ABYSS_ANDESITE.get());

        //工具标签
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(MIABlocks.ABYSS_GRASS_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MIABlocks.ABYSS_ANDESITE.get())
                .add(MIABlocks.ABYSS_COBBLED_ANDESITE.get());

        //标签嵌套
        tag(BlockTags.SCULK_REPLACEABLE)
                .addTag(MIABlockTags.ANDESITE_ORE_REPLACEABLES);
    }
}
