package com.altnoir.mementoinabyss.foundation.registrate;

import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import java.util.function.Supplier;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/** Reusable loot rules. Referenced entries are resolved only when datagen runs. */
public final class LootGen {
    private LootGen() {}

    public static <B extends Block> NonNullBiConsumer<RegistrateBlockLootTables, B> lowerHalf() {
        return (loot, block) -> lowerHalf(loot, block, block);
    }

    public static <B extends Block> NonNullBiConsumer<RegistrateBlockLootTables, B> lowerHalf(
            Supplier<? extends ItemLike> drop) {
        return (loot, block) -> lowerHalf(loot, block, drop.get());
    }

    private static void lowerHalf(RegistrateBlockLootTables loot, Block block, ItemLike drop) {
        loot.add(
                block,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .when(ExplosionCondition.survivesExplosion())
                                        .add(
                                                LootItem.lootTableItem(drop)
                                                        .when(
                                                                LootItemBlockStatePropertyCondition
                                                                        .hasBlockStateProperties(
                                                                                block)
                                                                        .setProperties(
                                                                                StatePropertiesPredicate
                                                                                        .Builder
                                                                                        .properties()
                                                                                        .hasProperty(
                                                                                                BlockStateProperties
                                                                                                        .DOUBLE_BLOCK_HALF,
                                                                                                DoubleBlockHalf
                                                                                                        .LOWER))))));
    }

    public static <B extends Block> NonNullBiConsumer<RegistrateBlockLootTables, B> leaves(
            Supplier<? extends Block> sapling) {
        return (loot, block) ->
                loot.add(
                        block,
                        loot.createLeavesDrops(
                                block, sapling.get(), 0.05F, 0.0625F, 0.083333336F, 0.1F));
    }

    public static <B extends Block> NonNullBiConsumer<RegistrateBlockLootTables, B> silkTouchOr(
            Supplier<? extends ItemLike> drop) {
        return (loot, block) ->
                loot.add(
                        block,
                        loot.createSilkTouchDispatchTable(
                                block, LootItem.lootTableItem(drop.get())));
    }
}
