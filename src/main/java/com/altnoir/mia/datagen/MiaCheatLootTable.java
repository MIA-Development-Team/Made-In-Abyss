package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public record MiaCheatLootTable(HolderLookup.Provider registries) implements LootTableSubProvider {
    public static final ResourceKey<LootTable> SIMPLE_RUINS = MiaLootTableProvider.register("chests/simple_ruins");

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        output.accept(SIMPLE_RUINS, LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1.0F, 3.0F))
                                .add(LootItem.lootTableItem(Items.SADDLE).setWeight(20))
                                .add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(15))
                                .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(2))
                                .add(LootItem.lootTableItem(Items.MUSIC_DISC_OTHERSIDE).setWeight(2))
                                .add(LootItem.lootTableItem(MiaItems.PRASIOLITE_SHARD.get()).setWeight(15))
                                .add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(20))
                                .add(LootItem.lootTableItem(Items.SKELETON_SKULL).setWeight(20))
                                .add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(10))
                                .add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(15))
                                .add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(5))
                                .add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))
                )
                .withPool(
                        LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1.0F, 4.0F))
                                .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.BREAD).setWeight(20))
                                .add(LootItem.lootTableItem(Items.BUCKET).setWeight(10))
                                .add(LootItem.lootTableItem(Items.SKELETON_SKULL).setWeight(20))
                                .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.COAL).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                                .add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                )
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(3.0F))
                                .add(LootItem.lootTableItem(Items.BONE).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
                                .add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
                                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
                                .add(LootItem.lootTableItem(Items.STRING).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
                )
        );
    }
}
