package com.altnoir.mia.datagen;

import com.altnoir.mia.block.MIABlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MIABlockLootTableProvider extends BlockLootSubProvider {
    protected MIABlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(MIABlocks.ABYSS_GRASS_BLOCK.get(), createSingleItemTableWithSilkTouch(MIABlocks.ABYSS_GRASS_BLOCK.get(), Blocks.DIRT));
        add(MIABlocks.COVERGRASS_ABYSS_ANDESITE.get(), createSingleItemTableWithSilkTouch(MIABlocks.COVERGRASS_ABYSS_ANDESITE.get(), Blocks.ANDESITE));
        add(MIABlocks.ABYSS_ANDESITE.get(), createSingleItemTableWithSilkTouch(MIABlocks.ABYSS_ANDESITE.get(), MIABlocks.ABYSS_COBBLED_ANDESITE.get()));
        dropSelf(MIABlocks.ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MIABlocks.FORTITUDE_FLOWER.get());
    }

//    protected LootTable.Builder createSilkTouchDrops(Block block, Block other) {
//        return this.createSilkTouchDispatchTable(
//                block, this.applyExplosionDecay(block, LootItem.lootTableItem(other)
//                )
//        );
//    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return MIABlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
