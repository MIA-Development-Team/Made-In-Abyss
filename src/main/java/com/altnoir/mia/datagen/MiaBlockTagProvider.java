package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlockTags;
import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MiaBlockTagProvider extends BlockTagsProvider {
    public MiaBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MIA.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 深渊标签
        tag(MiaBlockTags.ANDESITE_ORE_REPLACEABLES)
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(MiaBlockTags.BASE_STONE_ABYSS)
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(MiaBlockTags.COVERGRASS)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());

        // 基础标签
        tag(BlockTags.DIRT)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());

        tag(BlockTags.MOSS_REPLACEABLE)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get());

        tag(BlockTags.LOGS_THAT_BURN) // 自动添加LOGS标签
                .add(MiaBlocks.SKYFOG_LOG.get())
                .add(MiaBlocks.SKYFOG_WOOD.get())
                .add(MiaBlocks.STRIPPED_SKYFOG_LOG.get())
                .add(MiaBlocks.STRIPPED_SKYFOG_WOOD.get());
        tag(BlockTags.LEAVES)
                .add(MiaBlocks.SKYFOG_LEAVES.get())
                .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get());
        tag(BlockTags.PLANKS)
                .add(MiaBlocks.SKYFOG_PLANKS.get());
        tag(BlockTags.WOODEN_STAIRS) // 自动添加STAIRS标签
                .add(MiaBlocks.SKYFOG_STARIS.get());
        tag(BlockTags.STAIRS)
                .add(MiaBlocks.ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get());
        tag(BlockTags.SLABS)
                .add(MiaBlocks.ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.SKYFOG_SLAB.get());
        tag(BlockTags.WALLS)
                .add(MiaBlocks.ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get());
        tag(BlockTags.FENCES)
                .add(MiaBlocks.SKYFOG_FENCE.get());
        tag(BlockTags.FENCE_GATES)
                .add(MiaBlocks.SKYFOG_FENCE_GATE.get());
        tag(BlockTags.SAPLINGS)
                .add(MiaBlocks.SKYFOG_SAPLING.get());

        tag(BlockTags.FLOWERS)
                .add(MiaBlocks.FORTITUDE_FLOWER.get());

        tag(BlockTags.CLIMBABLE)
                .add(MiaBlocks.ROPE.get());

        //NeoForge标签
        tag(Tags.Blocks.VILLAGER_FARMLANDS)
                .add(MiaBlocks.HOPPER_FARMLAND.get());

        // 工具标签
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get())
                .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get())
                .add(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get())
                .add(MiaBlocks.ABYSS_ANDESITE_PILLAR.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.CHISLED_ABYSS_ANDESITE.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get())
                .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get())
                .add(MiaBlocks.LAMP_TUBE.get())
                .add(MiaBlocks.PEDESTAL.get())
                .add(MiaBlocks.ARTIFACT_SMITHING_TABLE.get())
                .add(MiaBlocks.ENDLESS_CUP.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(MiaBlocks.HOPPER_FARMLAND.get());

        // 标签嵌套
        tag(BlockTags.SCULK_REPLACEABLE)
                .addTag(MiaBlockTags.ANDESITE_ORE_REPLACEABLES);
    }
}
