package com.altnoir.mia.datagen.loottable;

import com.altnoir.mia.datagen.MiaLootTableProvider;
import com.altnoir.mia.init.MiaItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public record MiaArchaeologyLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
    public static final ResourceKey<LootTable> ABYSS_RUINS = MiaLootTableProvider.register("archaeology/abyss_ruins");

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ABYSS_RUINS, LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.ARMS_UP_POTTERY_SHERD).setWeight(2))
                                .add(LootItem.lootTableItem(Items.BREWER_POTTERY_SHERD).setWeight(2))
                                .add(LootItem.lootTableItem(MiaItems.PRASIOLITE_SHARD.get()))
                                .add(LootItem.lootTableItem(MiaItems.TEST_ARTIFACT_1.get()))
                                .add(LootItem.lootTableItem(MiaItems.TEST_ARTIFACT_2.get()))
                                .add(LootItem.lootTableItem(MiaItems.TEST_ARTIFACT_3.get()))
                                .add(
                                        LootItem.lootTableItem(Items.SUSPICIOUS_STEW)
                                                .apply(
                                                        SetStewEffectFunction.stewEffect()
                                                                .withEffect(MobEffects.NIGHT_VISION, UniformGenerator.between(7.0F, 10.0F))
                                                                .withEffect(MobEffects.JUMP, UniformGenerator.between(7.0F, 10.0F))
                                                                .withEffect(MobEffects.WEAKNESS, UniformGenerator.between(6.0F, 8.0F))
                                                                .withEffect(MobEffects.BLINDNESS, UniformGenerator.between(5.0F, 7.0F))
                                                                .withEffect(MobEffects.POISON, UniformGenerator.between(10.0F, 20.0F))
                                                                .withEffect(MobEffects.SATURATION, UniformGenerator.between(7.0F, 10.0F))
                                                )
                                )
                )
        );
    }
}
