package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MiaBlockLootTable extends BlockLootSubProvider {
    public MiaBlockLootTable(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    private final HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

    @Override
    public void generate() {
        add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(),
                block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        add(MiaBlocks.COVERGRASS_TUFF.get(),
                block -> createSingleItemTableWithSilkTouch(block, Blocks.TUFF));
        // 深界安山岩
        add(MiaBlocks.ABYSS_ANDESITE.get(),
                block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        dropSelf(MiaBlocks.ABYSS_ANDESITE_STAIRS.get());
        add(MiaBlocks.ABYSS_ANDESITE_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.ABYSS_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS.get());
        add(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS.get());
        add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE.get());
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS.get());
        add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_PILLAR.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.CHISLED_ABYSS_ANDESITE.get());
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS.get());
        add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL.get());
        dropSelf(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get());
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS.get());
        add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL.get());

        // 化石树
        dropSelf(MiaBlocks.FOSSILIZED_LOG.get());
        dropSelf(MiaBlocks.FOSSILIZED_WOOD.get());
        dropSelf(MiaBlocks.STRIPPED_FOSSILIZED_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_FOSSILIZED_WOOD.get());

        dropSelf(MiaBlocks.MOSSY_FOSSILIZED_LOG.get());
        dropSelf(MiaBlocks.MOSSY_FOSSILIZED_WOOD.get());
        dropSelf(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG.get());
        dropSelf(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD.get());


        dropSelf(MiaBlocks.POLISHED_FOSSILIZED_WOOD.get());
        dropSelf(MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS.get());
        add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL.get());
        dropSelf(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD.get());
        dropSelf(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS.get());
        add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL.get());
        dropSelf(MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD.get());

        dropSelf(MiaBlocks.FOSSILIZED_WOOD_BRICKS.get());
        dropSelf(MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS.get());
        add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL.get());
        dropSelf(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS.get());
        dropSelf(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS.get());
        add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL.get());

        // 天雾树
        dropSelf(MiaBlocks.SKYFOG_LOG.get());
        dropSelf(MiaBlocks.SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.SKYFOG_PLANKS.get());
        dropSelf(MiaBlocks.SKYFOG_STAIRS.get());
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

        //倒悬树
        dropSelf(MiaBlocks.INVERTED_LOG.get());
        dropSelf(MiaBlocks.INVERTED_WOOD.get());
        dropSelf(MiaBlocks.STRIPPED_INVERTED_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_INVERTED_WOOD.get());
        dropSelf(MiaBlocks.INVERTED_PLANKS.get());
        dropSelf(MiaBlocks.INVERTED_STAIRS.get());
        add(MiaBlocks.INVERTED_SLAB.get(), this::createSlabItemTable);
        dropSelf(MiaBlocks.INVERTED_FENCE.get());
        dropSelf(MiaBlocks.INVERTED_FENCE_GATE.get());
        add(MiaBlocks.INVERTED_DOOR.get(), this::createDoorTable);
        dropSelf(MiaBlocks.INVERTED_TRAPDOOR.get());
        dropSelf(MiaBlocks.INVERTED_SAPLING.get());
        add(MiaBlocks.INVERTED_LEAVES.get(),
                block -> createLeavesDrops(block, MiaBlocks.INVERTED_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
        dropSelf(MiaBlocks.INVERTED_PRESSURE_PLATE.get());
        dropSelf(MiaBlocks.INVERTED_BUTTON.get());


        // 晶石
        dropSelf(MiaBlocks.PRASIOLITE_BLOCK.get());
        add(MiaBlocks.BUDDING_PRASIOLITE.get(), noDrop());
        add(MiaBlocks.PRASIOLITE_CLUSTER.get(), block -> createClusterDrops(block, MiaItems.PRASIOLITE_SHARD.get(), 4.0f));
        dropWhenSilkTouch(MiaBlocks.LARGE_PRASIOLITE_BUD.get());
        dropWhenSilkTouch(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get());
        dropWhenSilkTouch(MiaBlocks.SMALL_PRASIOLITE_BUD.get());

        // 植物
        add(MiaBlocks.FORTITUDE_FLOWER.get(), this::createPetalsDrops);
        add(MiaBlocks.REED.get(), block -> createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        // 工作台
        dropSelf(MiaBlocks.ARTIFACT_SMITHING_TABLE.get());
        // 设备
        dropOther(MiaBlocks.HOPPER_FARMLAND.get(), MiaBlocks.ABYSS_COBBLED_ANDESITE.get());
        dropSelf(MiaBlocks.SUN_STONE.get());
        dropSelf(MiaBlocks.CAVE_EXPLORER_BEACON.get());
        dropSelf(MiaBlocks.AMETHYST_LAMPTUBE.get());
        dropSelf(MiaBlocks.PRASIOLITE_LAMPTUBE.get());
        dropSelf(MiaBlocks.PEDESTAL.get());
        dropWhenSilkTouch(MiaBlocks.ABYSS_SPAWNER.get());
        dropSelf(MiaBlocks.ENDLESS_CUP.get());
        dropSelf(MiaBlocks.ROPE.get());
    }

    private LootTable.Builder createClusterDrops(Block block, Item other, float count) {
        return this.createSilkTouchDispatchTable(
                block,
                this.applyExplosionDecay(block, LootItem.lootTableItem(other))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                        .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                        .otherwise(
                                this.applyExplosionDecay(
                                        block, LootItem.lootTableItem(other).apply(SetItemCountFunction.setCount(ConstantValue.exactly(count / 2)))
                                )
                        )
        );
    }

    private LootTable.Builder createSkyfogLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        return this.createLeavesDrops(oakLeavesBlock, saplingBlock, chances)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(this.doesNotHaveShearsOrSilkTouch()));
    }

    private LootTable.Builder createSkyfogLeavesDrops2(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        return this.createLeavesDrops(oakLeavesBlock, saplingBlock, chances)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(this.doesNotHaveShearsOrSilkTouch())
                                .add(
                                        this.applyExplosionCondition(
                                                oakLeavesBlock,
                                                LootItem.lootTableItem(MiaItems.MISTFUZZ_PEACH.get())
                                                        .apply(SetItemCountFunction
                                                                .setCount(UniformGenerator.between(1.0F, 2.0F)))
                                                        .apply(ApplyBonusCount.addOreBonusCount(
                                                                registrylookup.getOrThrow(Enchantments.FORTUNE))))));
    }

    private LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
        return this.hasShearsOrSilkTouch().invert();
    }

    private LootItemCondition.Builder hasShearsOrSilkTouch() {
        return HAS_SHEARS.or(this.hasSilkTouch());
    }

    protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(
            Block block, Property<T> property, T value
    ) {
        return LootTable.lootTable()
                .withPool(
                        this.applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .when(
                                                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
                                                        )
                                        )
                        )
                );
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return MiaBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
