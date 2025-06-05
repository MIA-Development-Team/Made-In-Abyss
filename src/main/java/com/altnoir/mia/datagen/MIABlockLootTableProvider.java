package com.altnoir.mia.datagen;

import com.altnoir.mia.block.MIABlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class MIABlockLootTableProvider extends BlockLootSubProvider {
    protected MIABlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropOther(MIABlocks.ABYSS_GRASS_BLOCK.get(), Blocks.DIRT);
        dropOther(MIABlocks.ABYSS_ANDESITE.get(),  MIABlocks.ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MIABlocks.ABYSS_ANDESITE.get());
    }
}
