package com.altnoir.mia.datagen;

import com.altnoir.abysslib.reginth.providers.loot.ReginthBlockLootTables;
import java.util.List;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * MIA 的自定义战利品表 helper（Reginth 版）。
 * <p>
 * 为什么不用 {@code BlockLootSubProvider} 里那些现成的：{@code hasShearsOrSilkTouch()} 在 1.21.1 里是
 * <b>private</b>，既不能覆写也拿不到（{@code javap} 实测；只有 {@code hasSilkTouch()} 是 protected）。
 * 所以这里按原版 {@code hasSilkTouch()} 的字节码等价重建条件，而不是去改 AbyssLib。
 * <p>
 * 其余版本：{@code ReginthBlockLootTables} 已经把 {@code BlockLootSubProvider} 里的 protected
 * helper（{@code createOreDrop} / {@code createCopperOreDrops} / {@code createPetalsDrops} /
 * {@code createSingleItemTableWithSilkTouch} …）全部用 public override 暴露出来了，
 * 所以那些能直接在 {@code .loot((tables, block) -> tables.xxx(...))} 里用，不必在这里再抄一遍。
 * 这里只放**原版没有**或**原版是 private** 的那几个：晶簇、深渊草、双格作物/花的条件表。
 */
public final class MiaLootGen {

    /**
     * 复刻 {@code BlockLootSubProvider.NORMAL_LEAVES_SAPLING_CHANCES}（protected，值取自现有产出）。
     */
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = {
        0.05F, 0.0625F, 0.083333336F, 0.1F
    };

    private MiaLootGen() {}

    /**
     * 天雾树叶：普通掉落 + 一个"无剪刀/无精准采集时"的附加池。
     * <p>
     * 注意原实现（{@code createSkyfogLeavesDrops}）**没有**往那个池里加条目，产出里是一个
     * {@code "entries": []} 的空池 —— 这里保持一致，行为等价。
     */
    public static LootTable.Builder skyfogLeaves(
            ReginthBlockLootTables tables, Block block, Block sapling) {
        return tables.createLeavesDrops(block, sapling, NORMAL_LEAVES_SAPLING_CHANCES)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(doesNotHaveShearsOrSilkTouch(tables)));
    }

    /**
     * 结果天雾树叶：在上面基础上，无剪刀/无精准采集时额外掉果实（1~2 个，受时运加成）。
     */
    public static LootTable.Builder skyfogLeavesWithFruits(
            ReginthBlockLootTables tables, Block block, Block sapling, Item fruit) {
        return tables.createLeavesDrops(block, sapling, NORMAL_LEAVES_SAPLING_CHANCES)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(doesNotHaveShearsOrSilkTouch(tables))
                                .add(
                                        tables.applyExplosionCondition(
                                                block,
                                                LootItem.lootTableItem(fruit)
                                                        .apply(
                                                                SetItemCountFunction.setCount(
                                                                        UniformGenerator.between(
                                                                                1.0F, 2.0F)))
                                                        .apply(
                                                                ApplyBonusCount.addOreBonusCount(
                                                                        enchantment(
                                                                                tables,
                                                                                Enchantments
                                                                                        .FORTUNE))))));
    }

    /**
     * 普通树叶掉落（只有标准 {@code createLeavesDrops}，没有额外池）。
     * 对应旧 datagen 里手写的
     * {@code add(block, b -> createLeavesDrops(b, sapling, NORMAL_LEAVES_SAPLING_CHANCES))}。
     */
    public static LootTable.Builder plainLeaves(
            ReginthBlockLootTables tables, Block block, Block sapling) {
        return tables.createLeavesDrops(block, sapling, NORMAL_LEAVES_SAPLING_CHANCES);
    }

    /**
     * 晶簇/晶芽：精准采集走原矿，否则掉碎片；用对晶簇有效的工具（镐）时给 {@code count} 个，
     * 其它工具给一半。逐字对应旧 {@code createClusterDrops}。
     */
    public static LootTable.Builder clusterDrops(
            ReginthBlockLootTables tables, Block block, Item other, float count) {
        return tables.createSilkTouchDispatchTable(
                block,
                tables.applyExplosionDecay(block, LootItem.lootTableItem(other))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                        .apply(
                                ApplyBonusCount.addOreBonusCount(
                                        enchantment(tables, Enchantments.FORTUNE)))
                        .when(
                                MatchTool.toolMatches(
                                        ItemPredicate.Builder.item()
                                                .of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                        .otherwise(
                                tables.applyExplosionDecay(
                                        block,
                                        LootItem.lootTableItem(other)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                ConstantValue.exactly(
                                                                        count / 2))))));
    }

    /**
     * 深渊草（矮草/蕨类）：剪刀掉自身，否则 12.5% 概率掉小麦种子（时运每级 +0~2）。
     */
    public static LootTable.Builder abyssGrassDrops(ReginthBlockLootTables tables, Block block) {
        return tables.createShearsDispatchTable(
                block,
                (LootPoolEntryContainer.Builder<?>)
                        tables.applyExplosionDecay(
                                block,
                                LootItem.lootTableItem(Items.WHEAT_SEEDS)
                                        .when(LootItemRandomChanceCondition.randomChance(0.125F))
                                        .apply(
                                                ApplyBonusCount.addUniformBonusCount(
                                                        enchantment(tables, Enchantments.FORTUNE),
                                                        2))));
    }

    /**
     * 双格植物（芦苇）：只有下半格掉自身。逐字对应旧 {@code createSinglePropConditionTable}。
     */
    public static <T extends Comparable<T> & StringRepresentable>
            LootTable.Builder singlePropConditionTable(
                    ReginthBlockLootTables tables, Block block, Property<T> property, T value) {
        return LootTable.lootTable()
                .withPool(
                        tables.applyExplosionCondition(
                                block,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(
                                                LootItem.lootTableItem(block)
                                                        .when(
                                                                LootItemBlockStatePropertyCondition
                                                                        .hasBlockStateProperties(
                                                                                block)
                                                                        .setProperties(
                                                                                StatePropertiesPredicate
                                                                                        .Builder
                                                                                        .properties()
                                                                                        .hasProperty(
                                                                                                property,
                                                                                                value))))));
    }

    /**
     * 双格浆果（幽暗莓 / 梦荔枝）：只有下半格且 {@code AGE} 到 3 掉 2~3 个、到 2 掉 1~2 个，都受时运加成。
     * 逐字对应旧 {@code createSingleCropConditionTable}。
     */
    public static <T extends Comparable<T> & StringRepresentable>
            LootTable.Builder singleCropConditionTable(
                    ReginthBlockLootTables tables,
                    Block block,
                    Item other,
                    Property<T> property,
                    T value) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                                        block)
                                                .setProperties(
                                                        StatePropertiesPredicate.Builder
                                                                .properties()
                                                                .hasProperty(
                                                                        SweetBerryBushBlock.AGE, 3)
                                                                .hasProperty(property, value)))
                                .add(LootItem.lootTableItem(other))
                                .apply(
                                        SetItemCountFunction.setCount(
                                                UniformGenerator.between(2.0F, 3.0F)))
                                .apply(
                                        ApplyBonusCount.addUniformBonusCount(
                                                enchantment(tables, Enchantments.FORTUNE))))
                .withPool(
                        LootPool.lootPool()
                                .when(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                                        block)
                                                .setProperties(
                                                        StatePropertiesPredicate.Builder
                                                                .properties()
                                                                .hasProperty(
                                                                        SweetBerryBushBlock.AGE, 2)
                                                                .hasProperty(property, value)))
                                .add(LootItem.lootTableItem(other))
                                .apply(
                                        SetItemCountFunction.setCount(
                                                UniformGenerator.between(1.0F, 2.0F)))
                                .apply(
                                        ApplyBonusCount.addUniformBonusCount(
                                                enchantment(tables, Enchantments.FORTUNE))));
    }

    /**
     * 等价于原版 {@code BlockLootSubProvider#hasShearsOrSilkTouch().invert()}。
     */
    private static LootItemCondition.Builder doesNotHaveShearsOrSilkTouch(
            ReginthBlockLootTables tables) {
        LootItemCondition.Builder hasShears =
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
        LootItemCondition.Builder hasSilkTouch =
                MatchTool.toolMatches(
                        ItemPredicate.Builder.item()
                                .withSubPredicate(
                                        ItemSubPredicates.ENCHANTMENTS,
                                        ItemEnchantmentsPredicate.enchantments(
                                                List.of(
                                                        new EnchantmentPredicate(
                                                                enchantment(
                                                                        tables,
                                                                        Enchantments.SILK_TOUCH),
                                                                MinMaxBounds.Ints.atLeast(1))))));
        return hasShears.or(hasSilkTouch).invert();
    }

    private static net.minecraft.core.Holder<Enchantment> enchantment(
            ReginthBlockLootTables tables, net.minecraft.resources.ResourceKey<Enchantment> key) {
        HolderLookup.RegistryLookup<Enchantment> lookup =
                tables.getRegistries().lookupOrThrow(Registries.ENCHANTMENT);
        return lookup.getOrThrow(key);
    }
}
