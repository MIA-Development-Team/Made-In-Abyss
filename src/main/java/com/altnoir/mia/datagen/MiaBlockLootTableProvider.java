package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MiaBlockLootTableProvider extends BlockLootSubProvider {
    protected MiaBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(MiaBlocks.ABYSS_GRASS_BLOCK.get(), createSingleItemTableWithSilkTouch(MiaBlocks.ABYSS_GRASS_BLOCK.get(), Blocks.DIRT));
        add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), createSingleItemTableWithSilkTouch(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        add(MiaBlocks.ABYSS_ANDESITE.get(), createSingleItemTableWithSilkTouch(MiaBlocks.ABYSS_ANDESITE.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE.get());

        dropSelf(MiaBlocks.SKYFOGWOOD_LOG.get());
        dropSelf(MiaBlocks.SKYFOGWOOD.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOGWOOD_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOGWOOD.get());
        dropSelf(MiaBlocks.SKYFOGWOO_PLANKS.get());

        dropSelf(MiaBlocks.FORTITUDE_FLOWER.get());
        dropSelf(MiaBlocks.LAMP_TUBE.get());
    }

//    protected LootTable.Builder createSilkTouchDrops(Block block, Block other) {
//        return this.createSilkTouchDispatchTable(
//                block, this.applyExplosionDecay(block, LootItem.lootTableItem(other)
//                )
//        );
//    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return MiaBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
