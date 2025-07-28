package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MiaBlockLootTableProvider extends BlockLootSubProvider {
    protected MiaBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(),
                block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        add(MiaBlocks.ABYSS_ANDESITE.get(),
                block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        dropSelf(MiaBlocks.ABYSS_ANDESITE_STAIRS.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_SLAB.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_PILLAR.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.CHISLED_ABYSS_ANDESITE.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get());
        dropSelf(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get());

        dropSelf(MiaBlocks.SKYFOG_LOG.get());
        dropSelf(MiaBlocks.SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.SKYFOG_PLANKS.get());
        dropSelf(MiaBlocks.SKYFOG_STARIS.get());
        add(MiaBlocks.SKYFOG_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.SKYFOG_FENCE.get());
        dropSelf(MiaBlocks.SKYFOG_FENCE_GATE.get());
        add(MiaBlocks.SKYFOG_DOOR.get(), this::createDoorTable);
        dropSelf(MiaBlocks.SKYFOG_TRAPDOOR.get());
        dropSelf(MiaBlocks.SKYFOG_SAPLING.get());
        add(MiaBlocks.SKYFOG_LEAVES.get(),
                block -> createSkyfogLeavesDrops(block, MiaBlocks.SKYFOG_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get(), block -> createSkyfogLeavesDrops2(block,
                MiaBlocks.SKYFOG_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(MiaBlocks.SKYFOG_PRESSURE_PLATE.get());
        dropSelf(MiaBlocks.SKYFOG_BUTTON.get());

        dropSelf(MiaBlocks.FORTITUDE_FLOWER.get());

        dropSelf(MiaBlocks.ARTIFACT_ENHANCEMENT_TABLE.get());

        dropSelf(MiaBlocks.LAMP_TUBE.get());
        dropSelf(MiaBlocks.ABYSS_PORTAL.get());
        dropWhenSilkTouch(MiaBlocks.ABYSS_SPAWNER.get());
        dropSelf(MiaBlocks.ENDLESS_CUP.get());
        dropSelf(MiaBlocks.ROPE.get());
    }

    // protected LootTable.Builder createSilkTouchDrops(Block block, Block other) {
    // return this.createSilkTouchDispatchTable(
    // block, this.applyExplosionDecay(block, LootItem.lootTableItem(other)
    // )
    // );
    // }
    protected LootTable.Builder createSkyfogLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        // HolderLookup.RegistryLookup<Enchantment> registrylookup =
        // this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(oakLeavesBlock, saplingBlock, chances)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(this.doesNotHaveShearsOrSilkTouch()));
    }

    protected LootTable.Builder createSkyfogLeavesDrops2(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(oakLeavesBlock, saplingBlock, chances)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(this.doesNotHaveShearsOrSilkTouch())
                                .add(
                                        ((LootPoolSingletonContainer.Builder) this.applyExplosionCondition(
                                                oakLeavesBlock,
                                                LootItem.lootTableItem(MiaItems.MISTFUZZ_PEACH.get())
                                                        .apply(SetItemCountFunction
                                                                .setCount(UniformGenerator.between(1.0F, 2.0F)))
                                                        .apply(ApplyBonusCount.addOreBonusCount(
                                                                registrylookup.getOrThrow(Enchantments.FORTUNE)))))));
    }

    private LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
        return this.hasShearsOrSilkTouch().invert();
    }

    private LootItemCondition.Builder hasShearsOrSilkTouch() {
        return HAS_SHEARS.or(this.hasSilkTouch());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return MiaBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
