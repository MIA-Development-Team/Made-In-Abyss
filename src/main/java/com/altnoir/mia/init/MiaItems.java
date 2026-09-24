package com.altnoir.mia.init;

import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.util.entry.ItemEntry;
import com.altnoir.mia.MIA;
import com.altnoir.mia.common.component.MiaFoods;
import com.altnoir.mia.common.item.*;
import com.altnoir.mia.common.item.abs.IArtifactItem.Grade;
import com.altnoir.mia.datagen.BlockStateGen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/**
 * MIA 的物品注册。
 * <p>
 * 与方块一样，全部走 AbyssLib 的 Reginth builder 链：{@code REGINTH.<Item>item(...)} 默认就会
 * 自动生成物品模型（{@code minecraft:item/generated} + {@code mia:item/<name>}）与语言键
 * {@code item.mia.<name>}，所以这里大部分条目什么都不用额外写。
 * <p>
 * <b>为什么这里也需要 {@link #bootstrap()}</b>：理由和 {@link MiaBlocks#bootstrap()} 一样 ——
 * Reginth 的注册表是在类初始化时被填充的，本类必须早于 {@code RegisterEvent} 被加载。
 */
public class MiaItems {

    private static final Reginth REGINTH = MIA.registrate();

    /**
     * 强制初始化本类，见类注释。
     */
    public static void bootstrap() {}

    public static final ItemEntry<Item> RED_WHISTLE =
            REGINTH.<Item>item("red_whistle", p -> new SimpleWhistle(p, 1, 4)).register();
    public static final ItemEntry<Item> BLUE_WHISTLE =
            REGINTH.<Item>item("blue_whistle", p -> new SimpleWhistle(p, 2, 8)).register();
    //    public static final ItemEntry<Item> MOON_WHISTLE = REGINTH.<Item>item("moon_whistle", p ->
    // new SimpleWhistle(
    //            p, 3, 12)).register();
    //    public static final ItemEntry<Item> BLACK_WHISTLE = REGINTH.<Item>item("black_whistle", p
    // -> new SimpleWhistle(
    //            p, 4, 16)).register();
    //    public static final ItemEntry<Item> WHITE_WHISTLE = REGINTH.<Item>item("white_whistle", p
    // -> new SimpleWhistle(
    //            p, 5, 20)).register();

    public static final ItemEntry<Item> GRAY_ARTIFACT_BUNDLE =
            REGINTH.<Item>item("gray_artifact_bundle", p -> new ArtifactBundle(p, Grade.C, 3))
                    .register();
    public static final ItemEntry<Item> FANCY_ARTIFACT_BUNDLE =
            REGINTH.<Item>item("fancy_artifact_bundle", p -> new ArtifactBundle(p, Grade.B, 6))
                    .register();

    public static final ItemEntry<Item> TEST_ARTIFACT_1 =
            REGINTH.<Item>item("test_artifact_1", p -> new ArtifactItem(p, Grade.D, 1)).register();
    public static final ItemEntry<Item> TEST_ARTIFACT_2 =
            REGINTH.<Item>item("test_artifact_2", p -> new ArtifactItem(p, Grade.C, 2)).register();
    public static final ItemEntry<Item> TEST_ARTIFACT_3 =
            REGINTH.<Item>item("test_artifact_3", p -> new ArtifactItem(p, Grade.S, 4)).register();

    public static final ItemEntry<Item> HEALTH_JUNKIE =
            REGINTH.<Item>item("health_junkie", p -> new ArtifactItem(p, Grade.C, 1)).register();

    // 技能物品的贴图不在 mia:item/<name>，而在 mia:item/skill/<name>，所以显式给模型。
    public static final ItemEntry<Item> ARTIFACT_HASTE =
            REGINTH.<Item>item("artifact_haste", HasteSkill::new)
                    .model(BlockStateGen::skillItem)
                    .register();

    // 水晶碎片
    public static final ItemEntry<Item> PRASIOLITE_SHARD =
            REGINTH.<Item>item("prasiolite_shard", Item::new).register();
    public static final ItemEntry<Item> CAERULITE_SHARD =
            REGINTH.<Item>item("caerulite_shard", Item::new).register();

    public static final ItemEntry<Item> RAW_CHLOROPHYTE =
            REGINTH.<Item>item("raw_chlorophyte", Item::new).register();
    public static final ItemEntry<Item> CHLOROPHYTE_NUGGET =
            REGINTH.<Item>item("chlorophyte_nugget", Item::new).register();
    public static final ItemEntry<Item> CHLOROPHYTE_INGOT =
            REGINTH.<Item>item("chlorophyte_ingot", Item::new).register();
    public static final ItemEntry<Item> GROW_SWORD =
            REGINTH.<Item>item(
                            "grow_sword",
                            p ->
                                    new GrowSwordItem(
                                            MiaTiers.PRASIOLITE,
                                            p.attributes(
                                                    SwordItem.createAttributes(
                                                            MiaTiers.PRASIOLITE, 0.0F, -2.4F))))
                    .model(BlockStateGen::handheldItem)
                    .register();
    public static final ItemEntry<Item> BLAZE_REAP =
            REGINTH.<Item>item(
                            "blaze_reap",
                            p ->
                                    new BlazeReapItem(
                                            p.attributes(
                                                    DiggerItem.createAttributes(
                                                            Tiers.NETHERITE, 9.0F, -3.0F))))
                    .model(BlockStateGen::handheldItem)
                    .register();
    public static final ItemEntry<Item> PEACE_PHOBIA =
            REGINTH.<Item>item("peace_phobia", p -> new FoilItem(p.rarity(Rarity.EPIC).stacksTo(1)))
                    .register();
    public static final ItemEntry<Item> PRASIOLITE_PICKAXE =
            REGINTH.<Item>item(
                            "prasiolite_pickaxe",
                            p ->
                                    new CompositeItem(
                                            MiaTiers.PRASIOLITE,
                                            p.attributes(
                                                    CompositeItem.createAttributes(
                                                            MiaTiers.PRASIOLITE, 8.0F, -2.8F))))
                    .model(BlockStateGen::handheldItem)
                    .register();
    public static final ItemEntry<Item> PRASIOLITE_HOE =
            REGINTH.<Item>item(
                            "prasiolite_hoe",
                            p ->
                                    new PrasioliteHoeItem(
                                            MiaTiers.PRASIOLITE,
                                            p.attributes(
                                                    HoeItem.createAttributes(
                                                            MiaTiers.PRASIOLITE, 0.0F, 0.0F))))
                    .model(BlockStateGen::handheldItem)
                    .register();

    public static final ItemEntry<Item> MISTFUZZ_PEACH =
            REGINTH.<Item>item("mistfuzz_peach", p -> new Item(p.food(MiaFoods.MISTFUZZ_PEACH)))
                    .register();
    // ItemNameBlockItem 是 BlockItem 的子类，但 Reginth 的 defaultModel() 对所有物品都出
    // item/generated + mia:item/<name>，与旧 datagen 的 basicItem() 一致，所以不用额外指定。
    public static final ItemEntry<Item> GLOOM_BERRY =
            REGINTH.<Item>item(
                            "gloom_berry",
                            p ->
                                    new ItemNameBlockItem(
                                            MiaBlocks.GLOOM_BERRY_PLANT.get(),
                                            p.food(MiaFoods.GLOOM_BERRY)))
                    .register();
    public static final ItemEntry<Item> DREAM_LICHEE =
            REGINTH.<Item>item(
                            "dream_lichee",
                            p ->
                                    new ItemNameBlockItem(
                                            MiaBlocks.DREAM_LICHEE_PLANT.get(),
                                            p.food(MiaFoods.DREAM_LICHEE)))
                    .register();

    // 抓钩的物品模型 parent 是 mia:item/template/grappling_hook，不是 item/generated。
    public static final ItemEntry<Item> GRAPPLING_HOOK =
            REGINTH.<Item>item(
                            "grappling_hook",
                            p ->
                                    new HookItem(
                                            p.component(
                                                            DataComponents.CUSTOM_DATA,
                                                            CustomData.EMPTY)
                                                    .stacksTo(1)))
                    .model((ctx, prov) -> BlockStateGen.templateItem(ctx, prov, "grappling_hook"))
                    .register();
    public static final ItemEntry<Item> DEBUG_ATTRIBUTE_TOOL =
            REGINTH.<Item>item("debug_attribute_tool", p -> new DebugAttributeTool(p.stacksTo(1)))
                    .model(BlockStateGen::handheldItem)
                    .register();

    // Block Item
    // 绳子的物品是 RopeItem，需要方块对象，所以由 MiaBlocks.ROPE 的
    // BlockBuilder.item(RopeItem::new) 注册 —— 而且 HEAD 里没有 item.mia.rope 这个语言键，
    // BlockBuilder.item 正好会把 LANG 设成 noop。这里不重复注册（否则 mia:rope 注册两次）。

    public static final ItemEntry<Item> STAR_COMPASS =
            REGINTH.<Item>item("star_compass", p -> new StarCompassItem(p.stacksTo(1))).register();
}
