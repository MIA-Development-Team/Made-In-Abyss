package com.altnoir.mia;

import com.altnoir.abysslib.creative.ALCreativeTabSection;
import com.altnoir.abysslib.creative.ALSectionedCreativeModeTab;
import com.altnoir.abysslib.creative.ALTitlePlate;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * MIA 的创造栏：**一个标签页 + 五个分区**（AbyssLib 的"分区式创造栏"）。
 * <p>
 * 每个分区带一张 162×18 的整行横幅贴图（{@code assets/mia/textures/gui/ctab/*.png}，即
 * 原 {@code CTAB_*.png}）。贴图里自带标题与底纹，所以渲染器<b>不会</b>再往横幅上叠代码文字
 * （见 {@code ALSectionedCreativeTabRenderer#drawFullRowTexture}），分区标题键因此只在无贴图时才会用到。
 * <p>
 * 为什么不用 {@code Reginth.defaultCreativeSection(...)} 那套自动登记：那套是在<b>注册时</b>
 * 往分区里塞一次条目，而 {@code ALSectionedCreativeModeTab.buildContents()} 每次重建标签页内容时
 * 都会先 {@code section.clear()}，自动塞进去的条目会被清掉。所以这里统一走 populator 显式登记。
 * <p>
 * 旧的 {@code mia_tab_artifact} 标签页已经取消，遗物类内容并入 {@link #SECTION_ARTIFACTS}。
 */
public class MiaItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MIA.MOD_ID);

    /**
     * 整行横幅贴图的资源路径前缀。
     */
    private static final String BANNER_DIR = "textures/gui/ctab/";

    /**
     * 分区横幅贴图 id。
     * <p>
     * <b>{@code .png} 必须显式写上</b>：{@code ALSectionedCreativeTabRenderer} 对非 {@code abysslib:}
     * 命名空间的贴图走 {@code RenderSystem.setShaderTexture(0, loc)}，也就是 MC 的 {@code SimpleTexture}
     * 加载路径 —— 它把路径原样交给资源管理器，<b>不会自己补 {@code .png}</b>。
     * 少写扩展名会实测得到
     * {@code Failed to load texture: mia:textures/gui/ctab/building / FileNotFoundException}，
     * 游戏里就是一块黑紫。
     * <p>
     * AbyssLib 侧（{@code assetTexture()}）现在也会补扩展名，所以两种写法都能跑；
     * 这里保留 {@code .png} 是为了兼容已发布的 AbyssLib 1.0.0。
     */
    private static ResourceLocation banner(String name) {
        return ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, BANNER_DIR + name + ".png");
    }

    // ---------------- 五个分区 ----------------
    // 横幅贴图是 64×18，占 4 格宽（左对齐），物品从同一行第 5 格开始排。

    /**
     * 横幅占的格数（1~9）；MIA 的贴图是 64×18，这里按 4 格（72px）绘制。
     */
    private static final int BANNER_UNITS = 4;

    /**
     * 分区标题底板：横幅贴图有渐变/花纹，白字直接压上去在浅色区域读不清，所以垫一层 25% 黑底。
     * <p>
     * AbyssLib 侧默认是 {@link ALTitlePlate#DISABLED 不启用}，这里是 MIA 显式打开。
     * 想调淡/调深就改这一个值：{@code 0x33}≈20%、{@code 0x40}≈25%（当前）、{@code 0x59}≈35%；
     * 要关掉整行改成 {@code ALTitlePlate.DISABLED} 即可。
     */
    private static final ALTitlePlate TITLE_PLATE = ALTitlePlate.argb(0x40000000);

    public static final ALCreativeTabSection SECTION_BUILDING =
            new ALCreativeTabSection(
                            "itemgroup.mia.section.building", banner("building"), BANNER_UNITS)
                    .titlePlate(TITLE_PLATE);
    public static final ALCreativeTabSection SECTION_NATURAL =
            new ALCreativeTabSection(
                            "itemgroup.mia.section.natural", banner("natural"), BANNER_UNITS)
                    .titlePlate(TITLE_PLATE);
    public static final ALCreativeTabSection SECTION_FUNCTIONAL =
            new ALCreativeTabSection(
                            "itemgroup.mia.section.functional", banner("functional"), BANNER_UNITS)
                    .titlePlate(TITLE_PLATE);
    public static final ALCreativeTabSection SECTION_ITEMS =
            new ALCreativeTabSection("itemgroup.mia.section.items", banner("items"), BANNER_UNITS)
                    .titlePlate(TITLE_PLATE);
    public static final ALCreativeTabSection SECTION_ARTIFACTS =
            new ALCreativeTabSection(
                            "itemgroup.mia.section.artifacts", banner("artifacts"), BANNER_UNITS)
                    .titlePlate(TITLE_PLATE);

    // ---------------- 标签页 ----------------
    // 两个标签页都走 AbyssLib 的分区式创造栏：主标签页 4 个分区，
    // 遗物标签页 1 个分区；每个分区各配一张 162×18 的整行横幅贴图。

    public static final Supplier<CreativeModeTab> MIA_TAB =
            CREATIVE_MODE_TAB.register(
                    "mia_tab",
                    () ->
                            ALSectionedCreativeModeTab.configure(
                                            CreativeModeTab.builder()
                                                    .title(Component.translatable("itemgroup.mia"))
                                                    .icon(
                                                            () ->
                                                                    new ItemStack(
                                                                            MiaItems.RED_WHISTLE
                                                                                    .get())),
                                            MiaItemGroups::populate,
                                            SECTION_BUILDING,
                                            SECTION_NATURAL,
                                            SECTION_FUNCTIONAL,
                                            SECTION_ITEMS)
                                    .build());

    public static final Supplier<CreativeModeTab> MIA_TAB_ARIFACT =
            CREATIVE_MODE_TAB.register(
                    "mia_tab_artifact",
                    () ->
                            ALSectionedCreativeModeTab.configure(
                                            CreativeModeTab.builder()
                                                    .title(
                                                            Component.translatable(
                                                                    "itemgroup.mia_artifact"))
                                                    .icon(
                                                            () ->
                                                                    new ItemStack(
                                                                            MiaItems
                                                                                    .FANCY_ARTIFACT_BUNDLE
                                                                                    .get())),
                                            MiaItemGroups::populateArtifacts,
                                            SECTION_ARTIFACTS)
                                    .build());

    private static void populate(CreativeModeTab.ItemDisplayParameters parameters) {
        // ================= 建筑 =================
        // 深层安山岩
        SECTION_BUILDING.add(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
        SECTION_BUILDING.add(MiaBlocks.COVERGRASS_TUFF);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE);
        SECTION_BUILDING.add(MiaBlocks.MARLITH);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_SLAB);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_WALL);
        // 圆石
        SECTION_BUILDING.add(MiaBlocks.ABYSS_COBBLED_ANDESITE);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL);
        // 苔石
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL);
        // 磨制
        SECTION_BUILDING.add(MiaBlocks.POLISHED_ABYSS_ANDESITE);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_PILLAR);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_COLUMN);
        // 石砖
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL);
        SECTION_BUILDING.add(MiaBlocks.CHISLED_ABYSS_ANDESITE);
        // 苔石砖
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL);

        // 化石树
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_LOG);
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_LOG);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_LOG);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB);
        SECTION_BUILDING.add(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL);
        SECTION_BUILDING.add(MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_WOOD_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_FOSSILIZED_WOOD_BRICKS_WALL);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB);
        SECTION_BUILDING.add(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL);

        // 木材：天雾树 / 翠寂菌 / 倒悬树
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_LOG);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_WOOD);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_SKYFOG_LOG);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_SKYFOG_WOOD);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_PLANKS);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_SLAB);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_FENCE);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_FENCE_GATE);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_DOOR);
        SECTION_BUILDING.add(MiaBlocks.SKYFOG_TRAPDOOR);

        SECTION_BUILDING.add(MiaBlocks.VERDANT_STEM);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_HYPHAE);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_VERDANT_STEM);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_VERDANT_HYPHAE);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_PLANKS);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_SLAB);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_FENCE);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_FENCE_GATE);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_DOOR);
        SECTION_BUILDING.add(MiaBlocks.VERDANT_TRAPDOOR);

        SECTION_BUILDING.add(MiaBlocks.INVERTED_LOG);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_INVERTED_LOG);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_INVERTED_WOOD);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_PLANKS);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_SLAB);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_FENCE);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_FENCE_GATE);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_DOOR);
        SECTION_BUILDING.add(MiaBlocks.INVERTED_TRAPDOOR);

        // 太初菌（移植自 PoopSky）
        SECTION_BUILDING.add(MiaBlocks.MYCELIUM_BLOCK);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_STEM);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_HYPHAE);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_PRIMO_STEM);
        SECTION_BUILDING.add(MiaBlocks.STRIPPED_PRIMO_HYPHAE);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_PLANKS);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_STAIRS);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_SLAB);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_FENCE);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_FENCE_GATE);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_DOOR);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_TRAPDOOR);
        SECTION_BUILDING.add(MiaBlocks.PRIMO_CAP);
        SECTION_BUILDING.add(MiaBlocks.GLOW_PRIMO_CAP);

        // 矿物块
        SECTION_BUILDING.add(MiaBlocks.CHLOROPHYTE_BLOCK);
        SECTION_BUILDING.add(MiaBlocks.RAW_CHLOROPHYTE_BLOCK);
        SECTION_BUILDING.add(MiaBlocks.PRASIOLITE_BLOCK);
        SECTION_BUILDING.add(MiaBlocks.CAERULITE_BLOCK);
        SECTION_BUILDING.add(MiaBlocks.SUN_STONE);

        // ================= 自然 =================
        SECTION_NATURAL.add(MiaBlocks.ABYSS_IRON_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_COPPER_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_GOLD_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_LAPIS_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_REDSTONE_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_DIAMOND_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_EMERALD_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_QUARTZ_ORE);
        SECTION_NATURAL.add(MiaBlocks.ABYSS_CHLOROPHYTE_ORE);
        SECTION_NATURAL.add(MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE);

        // 晶石
        SECTION_NATURAL.add(MiaBlocks.BUDDING_PRASIOLITE);
        SECTION_NATURAL.add(MiaBlocks.SMALL_PRASIOLITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.MEDIUM_PRASIOLITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.LARGE_PRASIOLITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.PRASIOLITE_CLUSTER);
        SECTION_NATURAL.add(MiaBlocks.BUDDING_CAERULITE);
        SECTION_NATURAL.add(MiaBlocks.SMALL_CAERULITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.MEDIUM_CAERULITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.LARGE_CAERULITE_BUD);
        SECTION_NATURAL.add(MiaBlocks.CAERULITE_CLUSTER);

        // 树叶与树苗
        SECTION_NATURAL.add(MiaBlocks.SKYFOG_LEAVES);
        SECTION_NATURAL.add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS);
        SECTION_NATURAL.add(MiaBlocks.VERDANT_LEAVES);
        SECTION_NATURAL.add(MiaBlocks.INVERTED_LEAVES);
        SECTION_NATURAL.add(MiaBlocks.SKYFOG_SAPLING);
        SECTION_NATURAL.add(MiaBlocks.VERDANT_FUNGUS);
        SECTION_NATURAL.add(MiaBlocks.INVERTED_SAPLING);
        // 太初菌（移植自 PoopSky）
        SECTION_NATURAL.add(MiaBlocks.PRIMO_FUNGUS);
        SECTION_NATURAL.add(MiaBlocks.GLOW_PRIMO_FUNGUS);
        SECTION_NATURAL.add(MiaBlocks.MYCELIUM_MAT);
        SECTION_NATURAL.add(MiaBlocks.MUSHROOM_BED);

        // 植物
        SECTION_NATURAL.add(MiaBlocks.MARGINAL_WEED);
        SECTION_NATURAL.add(MiaBlocks.CRIMSON_VEILGRASS);
        SECTION_NATURAL.add(MiaBlocks.SCORCHLEAF);
        SECTION_NATURAL.add(MiaBlocks.FORTITUDE_FLOWER);
        SECTION_NATURAL.add(MiaBlocks.REED);
        SECTION_NATURAL.add(MiaBlocks.BALLOON_PLANT);
        SECTION_NATURAL.add(MiaBlocks.LANTERN_PLANT);
        SECTION_NATURAL.add(MiaBlocks.GREEN_PERILLA);
        SECTION_NATURAL.add(MiaBlocks.KONJAC_ROOT);
        SECTION_NATURAL.add(MiaBlocks.SILVEAF_FUNGUS);
        SECTION_NATURAL.add(MiaBlocks.GLOOM_BERRY_PLANT);
        SECTION_NATURAL.add(MiaBlocks.DREAM_LICHEE_PLANT);

        // ================= 功能 =================
        SECTION_FUNCTIONAL.add(MiaBlocks.ARTIFACT_SMITHING_TABLE);
        SECTION_FUNCTIONAL.add(MiaBlocks.HOPPER_FARMLAND);
        SECTION_FUNCTIONAL.add(MiaBlocks.PRASIOLITE_LAMPTUBE);
        SECTION_FUNCTIONAL.add(MiaBlocks.AMETHYST_LAMPTUBE);
        SECTION_FUNCTIONAL.add(MiaBlocks.PEDESTAL);
        SECTION_FUNCTIONAL.add(MiaBlocks.CAVE_EXPLORER_BEACON);
        SECTION_FUNCTIONAL.add(MiaBlocks.ENDLESS_CUP);
        SECTION_FUNCTIONAL.add(MiaBlocks.ROPE);

        // 红石
        SECTION_FUNCTIONAL.add(MiaBlocks.SKYFOG_PRESSURE_PLATE);
        SECTION_FUNCTIONAL.add(MiaBlocks.SKYFOG_BUTTON);
        SECTION_FUNCTIONAL.add(MiaBlocks.VERDANT_PRESSURE_PLATE);
        SECTION_FUNCTIONAL.add(MiaBlocks.VERDANT_BUTTON);
        SECTION_FUNCTIONAL.add(MiaBlocks.INVERTED_PRESSURE_PLATE);
        SECTION_FUNCTIONAL.add(MiaBlocks.INVERTED_BUTTON);
        // 太初菌（移植自 PoopSky）
        SECTION_FUNCTIONAL.add(MiaBlocks.PRIMO_PRESSURE_PLATE);
        SECTION_FUNCTIONAL.add(MiaBlocks.PRIMO_BUTTON);

        // 深渊结构相关
        SECTION_FUNCTIONAL.add(MiaBlocks.ABYSS_PORTAL_CORE);
        SECTION_FUNCTIONAL.add(MiaBlocks.ABYSS_PORTAL_FRAME);
        SECTION_FUNCTIONAL.add(MiaBlocks.ABYSS_SPAWNER);

        // ================= 物品 =================
        SECTION_ITEMS.add(MiaItems.RAW_CHLOROPHYTE);
        SECTION_ITEMS.add(MiaItems.CHLOROPHYTE_NUGGET);
        SECTION_ITEMS.add(MiaItems.CHLOROPHYTE_INGOT);
        SECTION_ITEMS.add(MiaItems.PRASIOLITE_SHARD);
        SECTION_ITEMS.add(MiaItems.CAERULITE_SHARD);
        SECTION_ITEMS.add(MiaItems.PRASIOLITE_PICKAXE);
        SECTION_ITEMS.add(MiaItems.PRASIOLITE_HOE);
        // 食物
        SECTION_ITEMS.add(MiaItems.MISTFUZZ_PEACH);
        SECTION_ITEMS.add(MiaItems.GLOOM_BERRY);
        SECTION_ITEMS.add(MiaItems.DREAM_LICHEE);
        // 工具/杂项
        SECTION_ITEMS.add(MiaItems.RED_WHISTLE);
        SECTION_ITEMS.add(MiaItems.BLUE_WHISTLE);
        SECTION_ITEMS.add(MiaItems.STAR_COMPASS);
        SECTION_ITEMS.add(MiaItems.PEACE_PHOBIA);
    }

    private static void populateArtifacts(CreativeModeTab.ItemDisplayParameters parameters) {
        // ================= 遗物 =================
        SECTION_ARTIFACTS.add(MiaItems.GRAY_ARTIFACT_BUNDLE);
        SECTION_ARTIFACTS.add(MiaItems.FANCY_ARTIFACT_BUNDLE);
        SECTION_ARTIFACTS.add(MiaItems.TEST_ARTIFACT_1);
        SECTION_ARTIFACTS.add(MiaItems.TEST_ARTIFACT_2);
        SECTION_ARTIFACTS.add(MiaItems.TEST_ARTIFACT_3);
        SECTION_ARTIFACTS.add(MiaItems.HEALTH_JUNKIE);
        SECTION_ARTIFACTS.add(MiaItems.ARTIFACT_HASTE);
        SECTION_ARTIFACTS.add(MiaItems.GRAPPLING_HOOK);
        SECTION_ARTIFACTS.add(MiaItems.GROW_SWORD);
        SECTION_ARTIFACTS.add(MiaItems.BLAZE_REAP);
        // DEV
        SECTION_ARTIFACTS.add(MiaItems.DEBUG_ATTRIBUTE_TOOL);
        SECTION_ARTIFACTS.add(MiaBlocks.ABYSS_PORTAL);
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
