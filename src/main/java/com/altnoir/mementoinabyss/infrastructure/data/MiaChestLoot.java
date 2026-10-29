package com.altnoir.mementoinabyss.infrastructure.data;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaArtifactItems;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public record MiaChestLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    public static final ResourceKey<LootTable> STAR_COMPASS_RUINS = key("star_compass_ruins");
    public static final ResourceKey<LootTable> FIGHTING_ROOM =
            key("abyss_stronghold/fighting_room");
    public static final ResourceKey<LootTable> FIGHTING_ROOM_FINAL =
            key("abyss_stronghold/fighting_room_final");
    public static final ResourceKey<LootTable> STAR_COMPASS_RUINS_FINAL =
            key("star_compass_ruins_final");

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(STAR_COMPASS_RUINS, commonChest());
        output.accept(FIGHTING_ROOM, commonChest());
        output.accept(
                FIGHTING_ROOM_FINAL,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(4))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(8))
                                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(2))
                                        .add(
                                                LootItem.lootTableItem(Items.ENDER_PEARL)
                                                        .setWeight(5))));
        output.accept(
                STAR_COMPASS_RUINS_FINAL,
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(
                                                LootItem.lootTableItem(
                                                        MiaArtifactItems.STAR_COMPASS.get())))
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(3))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10))
                                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3))
                                        .add(
                                                LootItem.lootTableItem(Items.ENDER_PEARL)
                                                        .setWeight(6))));
    }

    private static LootTable.Builder commonChest() {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(5))
                                .add(LootItem.lootTableItem(Items.BONE).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10))
                                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10))
                                .add(LootItem.lootTableItem(Items.STRING).setWeight(10))
                                .add(LootItem.lootTableItem(Items.GLOW_BERRIES).setWeight(15)));
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE, MementoInAbyss.asResource("chests/" + path));
    }
}
