package com.altnoir.mia.datagen;

import com.altnoir.mia.init.MiaBlocks;
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
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MiaBlockLootTableProvider extends BlockLootSubProvider {
    protected MiaBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(MiaBlocks.ABYSS_GRASS_BLOCK.get(), block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        add(MiaBlocks.ABYSS_ANDESITE.get(), block -> createSingleItemTableWithSilkTouch(block, MiaBlocks.ABYSS_COBBLED_ANDESITE.get()));
        dropSelf(MiaBlocks.ABYSS_COBBLED_ANDESITE.get());

        dropSelf(MiaBlocks.SKYFOG_LOG.get());
        dropSelf(MiaBlocks.SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_LOG.get());
        dropSelf(MiaBlocks.STRIPPED_SKYFOG_WOOD.get());
        dropSelf(MiaBlocks.SKYFOG_PLANKS.get());
        dropSelf(MiaBlocks.SKYFOG_SAPLING.get());
        add(MiaBlocks.SKYFOG_LEAVES.get(), block -> createSkyfogLeavesDrops(block, MiaBlocks.SKYFOG_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        dropSelf(MiaBlocks.FORTITUDE_FLOWER.get());
        dropSelf(MiaBlocks.LAMP_TUBE.get());
        dropWhenSilkTouch(MiaBlocks.ABYSS_SPAWNER.get());
        dropSelf(MiaBlocks.ENDLESS_CUP.get());
    }

    //    protected LootTable.Builder createSilkTouchDrops(Block block, Block other) {
//        return this.createSilkTouchDispatchTable(
//                block, this.applyExplosionDecay(block, LootItem.lootTableItem(other)
//                )
//        );
//    }
    protected LootTable.Builder createSkyfogLeavesDrops(Block oakLeavesBlock, Block saplingBlock, float... chances) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops(oakLeavesBlock, saplingBlock, chances)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(this.doesNotHaveShearsOrSilkTouch())
                                .add(
                                        ((LootPoolSingletonContainer.Builder) this.applyExplosionCondition(oakLeavesBlock, LootItem.lootTableItem(Items.APPLE)))
                                                .when(
                                                        BonusLevelTableCondition.bonusLevelFlatChance(
                                                                registrylookup.getOrThrow(Enchantments.FORTUNE), 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F
                                                        )
                                                )
                                )
                );
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
