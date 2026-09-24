package com.altnoir.mia.datagen;

import com.altnoir.abysslib.reginth.providers.DataGenContext;
import com.altnoir.abysslib.reginth.providers.ReginthBlockstateProvider;
import com.altnoir.abysslib.reginth.providers.ReginthItemModelProvider;
import com.altnoir.mia.common.block.ColumnBlock;
import com.altnoir.mia.common.block.DoubleBerryblock;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import com.altnoir.mia.util.MiaUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

/**
 * MIA 的方块模型/方块状态 datagen 助手（Reginth 版）。
 * <p>
 * 签名与 Reginth 的 {@code .blockstate(...)} 回调一致，所以用法是：
 * <pre>{@code
 * REGINTH.object("skyfog_stairs")
 *         .block(p -> new StairBlock(SKYFOG_PLANKS.get().defaultBlockState(), p))
 *         .properties(p -> BlockBehaviour.Properties.ofFullCopy(SKYFOG_PLANKS.get()))
 *         .blockstate((ctx, prov) -> BlockStateGen.stairs(ctx, prov, SKYFOG_PLANKS.get()))
 *         .simpleItem()
 *         .register();
 * }</pre>
 * <p>
 * <b>为什么要显式传"基准方块"</b>：楼梯/台阶/栅栏/墙/按钮/压力板这些的贴图来自**它们所属的那个方块**
 * （例如 {@code skyfog_stairs} 用的是 {@code mia:block/skyfog_planks} 的贴图，而不是
 * {@code mia:block/skyfog_stairs}）。旧的 {@code MiaBlockStateProvider} 里是靠
 * {@code stairsBlockWithItem(block, textureBlock)} 的第二个参数传的，这里保持同样做法，
 * 避免用命名约定去猜（{@code verdant_hyphae} 的对应物是 {@code verdant_stem}，猜不出来）。
 * <p>
 * <b>物品模型</b>：Reginth 的 {@code .simpleItem()} 会自动把物品模型 parent 指向该方块默认状态的模型，
 * 所以 cubeAll / 楼梯 / 台阶 / 栅栏门 / 活板门 / 树叶 / 压力板**都不用管**；
 * 只有栅栏、墙、按钮、门、树苗/植物这几类需要 {@link #fenceItem} / {@link #wallItem} /
 * {@link #buttonItem} / {@link #doorItem} / {@link #bushItem} 显式覆盖。
 * <p>
 * 本类分成两半：上半是"简单 helper"（旧的 {@code stairsBlockWithItem} 那一批），
 * 下半是 D 级 helper 的移植（{@code mirroredBlock} / {@code columnBlock} /
 * {@code variantPillarBlockWithItem} / {@code clusterBlock} / {@code createFlowerBed} …）。
 * D 级这批的模型部分直接复用原来的 {@link MiaModelProvider}（它的方法本来就收
 * {@code BlockStateProvider}，而 {@code ReginthBlockstateProvider} 就是它的子类），
 * 只有方块状态部分需要在这里用 {@code prov.} 重写一遍。
 */
public final class BlockStateGen {

    /**
     * 原 {@code MiaBlockStateProvider} / {@code columnBlock} 里那份模型的持有者。
     */
    private static final MiaModelProvider MODELS = new MiaModelProvider();

    // The old provider emitted its two generated *template* models once, at the top of
    // registerStatesAndModels(). Here they are emitted from their first consumer instead,
    // so these flags make sure each is written exactly once per datagen run: calling
    // ModelBuilder.withExistingParent twice for the same path APPENDS a second element.
    private static boolean tsbTemplateEmitted = false;
    private static boolean hopperTemplateEmitted = false;

    private BlockStateGen() {}

    // ------------------------------------------------------------------
    // 简单 helper：方块状态 / 方块模型
    // ------------------------------------------------------------------

    /**
     * 原木/菌柄：轴向贴图，模型名 = 方块名。
     */
    public static <B extends Block> void log(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        prov.logBlock((RotatedPillarBlock) ctx.getEntry());
    }

    /**
     * 木头/菌核：六面都用对应原木的侧面贴图。
     */
    public static <B extends Block> void wood(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block log) {
        ResourceLocation texture = prov.blockTexture(log);
        prov.axisBlock((RotatedPillarBlock) ctx.getEntry(), texture, texture);
    }

    public static <B extends Block> void stairs(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        ResourceLocation texture = prov.blockTexture(base);
        prov.stairsBlock((StairBlock) ctx.getEntry(), texture, texture, texture);
    }

    public static <B extends Block> void slab(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        ResourceLocation texture = prov.blockTexture(base);
        prov.slabBlock((SlabBlock) ctx.getEntry(), texture, texture, texture, texture);
    }

    public static <B extends Block> void fence(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        prov.fenceBlock((FenceBlock) ctx.getEntry(), prov.blockTexture(base));
    }

    public static <B extends Block> void fenceGate(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        prov.fenceGateBlock((FenceGateBlock) ctx.getEntry(), prov.blockTexture(base));
    }

    public static <B extends Block> void wall(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        prov.wallBlock((WallBlock) ctx.getEntry(), prov.blockTexture(base));
    }

    public static <B extends Block> void pressurePlate(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        prov.pressurePlateBlock((PressurePlateBlock) ctx.getEntry(), prov.blockTexture(base));
    }

    public static <B extends Block> void button(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block base) {
        prov.buttonBlock((ButtonBlock) ctx.getEntry(), prov.blockTexture(base));
    }

    /**
     * 门：贴图取自身（{@code <name>_bottom} / {@code <name>_top}），cutout 渲染层。
     */
    public static <B extends Block> void door(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ResourceLocation texture = prov.blockTexture(block);
        prov.doorBlockWithRenderType(
                (DoorBlock) block, extend(texture, "_bottom"), extend(texture, "_top"), "cutout");
    }

    /**
     * 活板门：贴图取自身，cutout 渲染层。
     */
    public static <B extends Block> void trapdoor(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        prov.trapdoorBlockWithRenderType(
                (TrapDoorBlock) block, prov.blockTexture(block), true, "cutout");
    }

    /**
     * 树叶：单贴图套原版 {@code block/leaves} 模板，cutout 渲染层。
     */
    public static <B extends Block> void leaves(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        leaves(ctx, prov, "cutout");
    }

    public static <B extends Block> void leaves(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, String renderType) {
        Block block = ctx.getEntry();
        prov.simpleBlock(
                block,
                prov.models()
                        .singleTexture(
                                ctx.getName(),
                                prov.mcLoc("block/leaves"),
                                "all",
                                prov.blockTexture(block))
                        .renderType(renderType));
    }

    /**
     * 交叉模型（树苗、矮草、花等）：贴图取自身，cutout 渲染层。
     */
    public static <B extends Block> void bush(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        prov.simpleBlock(
                block,
                prov.models().cross(ctx.getName(), prov.blockTexture(block)).renderType("cutout"));
    }

    private static ResourceLocation extend(ResourceLocation rl, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath() + suffix);
    }

    // ------------------------------------------------------------------
    // D 级 helper 移植：方块状态
    // ------------------------------------------------------------------

    /**
     * 旧 {@code mirroredBlock}：cube_all + cube_mirrored_all 两个模型，方块状态里各出 0°/180°。
     */
    public static <B extends Block> void mirrored(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        MODELS.mirroredBlockModel(prov, block);
        prov.getVariantBuilder(block)
                .partialState()
                .addModels(
                        configuredModel(prov, prov.modLoc("block/" + MiaUtil.getBlockPath(block))),
                        configuredModel(
                                prov,
                                prov.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored")),
                        configuredModel(
                                prov, prov.modLoc("block/" + MiaUtil.getBlockPath(block)), 180),
                        configuredModel(
                                prov,
                                prov.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored"),
                                180));
    }

    /**
     * 旧 {@code pillarBlock}：轴向贴图，顶面用 {@code <name>_top}。
     */
    public static <B extends Block> void pillar(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        prov.axisBlock(
                (RotatedPillarBlock) block,
                prov.blockTexture(block),
                extend(prov.blockTexture(block), "_top"));
    }

    /**
     * 旧 {@code columnBlock}：按 {@code COLUMN} 状态切 none/top/bottom/middle 四个模型。
     */
    public static <B extends Block> void column(
            DataGenContext<Block, B> ctx,
            ReginthBlockstateProvider prov,
            Block pillar,
            Block decoration) {
        Block block = ctx.getEntry();
        MODELS.columnBlockModel(prov, block, pillar, decoration);
        String blockPath = MiaUtil.getBlockPath(block);
        prov.getVariantBuilder(block)
                .forAllStates(
                        state -> {
                            String suffix =
                                    switch (state.getValue(ColumnBlock.COLUMN)) {
                                        case NONE -> "";
                                        case TOP -> "_top";
                                        case BOTTOM -> "_bottom";
                                        case MIDDLE -> "_middle";
                                    };
                            return ConfiguredModel.builder()
                                    .modelFile(
                                            prov.models()
                                                    .getExistingFile(
                                                            prov.modLoc(
                                                                    "block/" + blockPath + suffix)))
                                    .build();
                        });
    }

    /**
     * 旧 {@code variantPillarBlockWithItem}：多个随机变体（贴图 {@code <name>0..n-1}）。
     */
    public static <B extends Block> void variantPillar(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, int variants) {
        variantPillar(ctx, prov, variants, null);
    }

    public static <B extends Block> void variantPillar(
            DataGenContext<Block, B> ctx,
            ReginthBlockstateProvider prov,
            int variants,
            int[] weights) {
        Block block = ctx.getEntry();
        variantAxis(
                prov,
                (RotatedPillarBlock) block,
                block,
                extend(prov.blockTexture(block), "_top"),
                variants,
                weights);
    }

    /**
     * 旧 {@code variantAxisBlockWithItem}：轴向变体，侧面贴图取自 {@code variant}。
     */
    public static <B extends Block> void variantAxis(
            DataGenContext<Block, B> ctx,
            ReginthBlockstateProvider prov,
            Block variant,
            int variants) {
        variantAxis(ctx, prov, variant, variants, null);
    }

    public static <B extends Block> void variantAxis(
            DataGenContext<Block, B> ctx,
            ReginthBlockstateProvider prov,
            Block variant,
            int variants,
            int[] weights) {
        variantAxis(prov, (RotatedPillarBlock) ctx.getEntry(), variant, null, variants, weights);
    }

    private static void variantAxis(
            ReginthBlockstateProvider prov,
            RotatedPillarBlock block,
            Block side,
            ResourceLocation end,
            int variants,
            int[] weights) {
        ConfiguredModel[] yModels = new ConfiguredModel[variants];
        ConfiguredModel[] zModels = new ConfiguredModel[variants];
        ConfiguredModel[] xModels = new ConfiguredModel[variants];

        for (int i = 0; i < variants; i++) {
            ResourceLocation variantSide = MiaUtil.miaId("block/" + MiaUtil.getBlockPath(side) + i);
            ResourceLocation topTexture = end != null ? end : variantSide;

            ModelFile model =
                    prov.models()
                            .cubeColumn(MiaUtil.getBlockPath(block) + i, variantSide, topTexture);
            ModelFile horizontalModel =
                    prov.models()
                            .cubeColumnHorizontal(
                                    MiaUtil.getBlockPath(block) + "_horizontal" + i,
                                    variantSide,
                                    topTexture);

            int weight = weights != null ? weights[i] : 1;

            yModels[i] = new ConfiguredModel(model, 0, 0, false, weight);
            zModels[i] = new ConfiguredModel(horizontalModel, 90, 0, false, weight);
            xModels[i] = new ConfiguredModel(horizontalModel, 90, 90, false, weight);
        }

        prov.getVariantBuilder(block)
                .partialState()
                .with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                .setModels(yModels)
                .partialState()
                .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                .setModels(zModels)
                .partialState()
                .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                .setModels(xModels);
    }

    /**
     * 旧 {@code clusterBlock}：交叉模型 + 六向 {@code FACING} 方块状态。
     */
    public static <B extends Block> void cluster(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ResourceLocation model = MODELS.crossModel(prov, block);
        facing(prov, block, model);
    }

    /**
     * 旧 {@code lampTubeBlock}：模板模型 + 六向 {@code FACING}。
     */
    public static <B extends Block> void lampTube(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block particle) {
        Block block = ctx.getEntry();
        facing(prov, block, MODELS.lampTubeBlockModel(prov, block, particle));
    }

    /**
     * 旧 {@code templateAllBlock(block)}：模板名 = 方块名。
     */
    public static <B extends Block> void templateAll(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        templateAll(ctx, prov, MiaUtil.getBlockPath(ctx.getEntry()));
    }

    /**
     * 旧 {@code templateAllBlock(block, templateModel)}。
     */
    public static <B extends Block> void templateAll(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, String templateModel) {
        Block block = ctx.getEntry();
        MODELS.customBlockModel(prov, block, templateModel);
        base(prov, block);
    }

    /**
     * 旧 {@code coverGrassBlock(modelBlock, DeferredBlock)}：底面用 MIA 命名空间。
     */
    public static <B extends Block> void coverGrass(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block bottom) {
        coverGrass(ctx, prov, prov.modLoc("block/" + MiaUtil.getBlockPath(bottom)));
    }

    /**
     * 旧 {@code coverGrassBlock(modelBlock, Block)}：底面用原版命名空间。
     */
    public static <B extends Block> void coverGrassVanilla(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block bottom) {
        coverGrass(ctx, prov, prov.mcLoc("block/" + MiaUtil.getBlockPath(bottom)));
    }

    private static <B extends Block> void coverGrass(
            DataGenContext<Block, B> ctx,
            ReginthBlockstateProvider prov,
            ResourceLocation bottomTexture) {
        Block block = ctx.getEntry();
        // The old provider emitted this template once at the top of registerStatesAndModels();
        // now it is emitted from its only consumer, guarded to run once.
        if (!tsbTemplateEmitted) {
            tsbTemplateEmitted = true;
            MODELS.templateTSBModel(prov);
        }
        String blockPath = MiaUtil.getBlockPath(block);
        prov.models()
                .withExistingParent(blockPath, prov.modLoc("block/template/cube_tsb"))
                .texture("top", prov.modLoc("block/abyss_grass_block_top"))
                .texture("side", prov.modLoc("block/" + blockPath + "_side"))
                .texture("bottom", bottomTexture);
        rotationY(prov, block);
    }

    /**
     * 旧 {@code baseBlockState}：单模型、无旋转。
     */
    public static <B extends Block> void base(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        base(prov, ctx.getEntry());
    }

    private static void base(ReginthBlockstateProvider prov, Block block) {
        prov.getVariantBuilder(block)
                .partialState()
                .addModels(
                        configuredModel(prov, prov.modLoc("block/" + MiaUtil.getBlockPath(block))));
    }

    /**
     * 多面附着方块的六个面及其模型旋转（与原版发光地衣的取向一致）。
     */
    private static final List<MultifaceFace> MULTIFACE_FACES =
            List.of(
                    new MultifaceFace(BlockStateProperties.NORTH, 0, 0),
                    new MultifaceFace(BlockStateProperties.EAST, 0, 90),
                    new MultifaceFace(BlockStateProperties.SOUTH, 0, 180),
                    new MultifaceFace(BlockStateProperties.WEST, 0, 270),
                    new MultifaceFace(BlockStateProperties.UP, 270, 0),
                    new MultifaceFace(BlockStateProperties.DOWN, 90, 0));

    /**
     * 旧 {@code rotationYBlockState}：0/90/180/270 四个变体。
     * <p>
     * <b>它只引用模型，不生成模型</b> —— 走的是 {@code models().getExistingFile(...)}，
     * 所以调用方必须自己先把 {@code mia:block/<名字>} 建出来（例如 {@link #coverGrass} 就是先建
     * {@code template/cube_tsb} 那一份，再调本方法）。需要"cube_all + 四向旋转"的话用
     * {@link #rotationYCubeAll}，那个会自己建模型。
     */
    public static <B extends Block> void rotationY(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        rotationY(prov, ctx.getEntry());
    }

    private static void rotationY(ReginthBlockstateProvider prov, Block block) {
        ResourceLocation model = prov.modLoc("block/" + MiaUtil.getBlockPath(block));
        prov.getVariantBuilder(block)
                .partialState()
                .addModels(
                        configuredModel(prov, model),
                        configuredModel(prov, model, 90),
                        configuredModel(prov, model, 180),
                        configuredModel(prov, model, 270));
    }

    /**
     * 旧 {@code moistureBlockState}：{@code MOISTURE} 0-6 用干模型、7 用 {@code _moist}。
     */
    public static <B extends Block> void moisture(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        // see coverGrass(): the old provider emitted this template once, up front
        if (!hopperTemplateEmitted) {
            hopperTemplateEmitted = true;
            MODELS.templateHopperFarmland(prov);
        }
        MODELS.hopperFarmBlockModel(prov, block);
        VariantBlockStateBuilder builder = prov.getVariantBuilder(block);
        IntegerProperty moisture = BlockStateProperties.MOISTURE;
        for (int i = 0; i < 7; i++) {
            builder.partialState()
                    .with(moisture, i)
                    .addModels(
                            configuredModel(
                                    prov, prov.modLoc("block/" + MiaUtil.getBlockPath(block))));
        }
        builder.partialState()
                .with(moisture, 7)
                .addModels(
                        configuredModel(
                                prov,
                                prov.modLoc("block/" + MiaUtil.getBlockPath(block) + "_moist")));
    }

    /**
     * 旧 {@code abyssSpawnerBlockState}：按 {@code TRIAL_SPAWNER_STATE} 切模型。
     */
    public static <B extends Block> void abyssSpawner(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        MODELS.abyssSpawnerBlockModel(prov, block);
        String blockPath = MiaUtil.getBlockPath(block);
        VariantBlockStateBuilder builder = prov.getVariantBuilder(block);

        Map<net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState, String>
                suffixes =
                        Map.of(
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.ACTIVE,
                                        "_active",
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.COOLDOWN,
                                        "",
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.EJECTING_REWARD,
                                        "_ejecting_reward",
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.INACTIVE,
                                        "",
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.WAITING_FOR_PLAYERS,
                                        "_active",
                                net.minecraft.world.level.block.entity.trialspawner
                                                .TrialSpawnerState.WAITING_FOR_REWARD_EJECTION,
                                        "_ejecting_reward");

        for (var state : suffixes.keySet()) {
            builder.partialState()
                    .with(BlockStateProperties.TRIAL_SPAWNER_STATE, state)
                    .modelForState()
                    .modelFile(
                            prov.models()
                                    .getExistingFile(
                                            prov.modLoc(
                                                    "block/" + blockPath + suffixes.get(state))))
                    .addModel();
        }
    }

    /**
     * 旧 {@code ropeBlock}。
     */
    public static <B extends Block> void rope(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        MODELS.ropeBlockModel(prov, block);
        base(prov, block);
    }

    /**
     * 旧 {@code abyssPortalBlock}。
     */
    public static <B extends Block> void abyssPortal(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        MODELS.abyssPortalBlockModel(prov, block);
        base(prov, block);
    }

    /**
     * 旧 {@code abyssPortalCoreBlock}。
     */
    public static <B extends Block> void abyssPortalCore(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block frame) {
        Block block = ctx.getEntry();
        MODELS.abyssPortalCoreModel(prov, block, frame);
        base(prov, block);
    }

    /**
     * 旧 {@code artifactSmithingTableBlock}。
     */
    public static <B extends Block> void artifactSmithingTable(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block bottom) {
        Block block = ctx.getEntry();
        MODELS.artifactSmithingTableBlockModel(prov, block, bottom);
        base(prov, block);
    }

    /**
     * 旧 {@code pedestalBlock}。
     */
    public static <B extends Block> void pedestal(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        MODELS.pedestalBlockModel(prov, block);
        base(prov, block);
    }

    /**
     * 旧 {@code createDoublePlant}：上下半格各一个交叉模型。
     */
    public static <B extends Block> void doublePlant(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ResourceLocation topModel = MODELS.crossModel(prov, block, "_top");
        ResourceLocation bottomModel = MODELS.crossModel(prov, block, "_bottom");
        prov.getVariantBuilder(block)
                .partialState()
                .with(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                .modelForState()
                .modelFile(prov.models().getExistingFile(bottomModel))
                .addModel()
                .partialState()
                .with(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                .modelForState()
                .modelFile(prov.models().getExistingFile(topModel))
                .addModel();
    }

    /**
     * 旧 {@code createDoubleBerry}：按半格 + {@code AGE} 选模型。
     */
    public static <B extends Block> void doubleBerry(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, int maxAge) {
        Block block = ctx.getEntry();
        for (int age = 0; age <= maxAge; age++) {
            MODELS.crossModel(prov, block, "_bottom" + age);
        }
        for (int age = 2; age <= maxAge; age++) {
            MODELS.crossModel(prov, block, "_top" + age);
        }
        prov.getVariantBuilder(block)
                .forAllStates(
                        state -> {
                            DoubleBlockHalf half =
                                    state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
                            int age = state.getValue(DoubleBerryblock.AGE);
                            String modelName =
                                    half == DoubleBlockHalf.UPPER && age >= 2
                                            ? MiaUtil.getBlockPath(block) + "_top" + age
                                            : MiaUtil.getBlockPath(block) + "_bottom" + age;
                            return new ConfiguredModel[] {
                                new ConfiguredModel(
                                        prov.models()
                                                .getExistingFile(prov.modLoc("block/" + modelName)))
                            };
                        });
    }

    /**
     * 旧 {@code createFlowerBed}：{@code FLOWER_AMOUNT} × {@code HORIZONTAL_FACING} 的 multipart。
     */
    public static <B extends Block> void flowerBed(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ResourceLocation[] bedModels = MODELS.flowerbedModels(prov, block);
        var builder = prov.getMultipartBuilder(block);
        Integer[][] amounts = {{1, 2, 3, 4}, {2, 3, 4}, {3, 4}, {4}};
        Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        int[] rotations = {0, 90, 180, 270};
        for (int model = 0; model < 4; model++) {
            for (int f = 0; f < 4; f++) {
                builder.part()
                        .modelFile(prov.models().getExistingFile(bedModels[model]))
                        .rotationY(rotations[f])
                        .addModel()
                        .condition(BlockStateProperties.FLOWER_AMOUNT, amounts[model])
                        .condition(BlockStateProperties.HORIZONTAL_FACING, facings[f])
                        .end();
            }
        }
    }

    /**
     * cube_all + 四个 Y 轴旋转变体（对应 PoopSky 的 {@code rotationYblockWithItem}）。
     * <p>
     * 与 {@link #rotationY} 的区别是它会**先生成** cube_all 模型
     * （{@link MiaModelProvider#cubeAllModel}），所以调用方不用另外建模型。
     */
    public static <B extends Block> void rotationYCubeAll(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ModelFile model = MODELS.cubeAllModel(prov, block);
        prov.getVariantBuilder(block)
                .partialState()
                .addModels(
                        new ConfiguredModel(model),
                        new ConfiguredModel(model, 0, 90, false),
                        new ConfiguredModel(model, 0, 180, false),
                        new ConfiguredModel(model, 0, 270, false));
    }

    /**
     * 菌痕（多面附着方块，移植自 PoopSky 的 {@code multifaceBlock}）。
     * <p>
     * 单面模型复用 {@link MiaModelProvider#multifaceModel}；六个朝向各出两个 part ——
     * 一个在该面为 {@code true} 时生效，一个在<b>六面全 false</b> 时生效
     * （后者对应"只有 {@code waterlogged} 而没有附着面"的状态，缺了它那条状态就没有模型）。
     */
    public static <B extends Block> void multifaceMat(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ModelFile model = MODELS.multifaceModel(prov, block);
        var builder = prov.getMultipartBuilder(block);
        for (MultifaceFace face : MULTIFACE_FACES) {
            boolean locked = face.rotationX() != 0 || face.rotationY() != 0;
            builder.part()
                    .modelFile(model)
                    .rotationX(face.rotationX())
                    .rotationY(face.rotationY())
                    .uvLock(locked)
                    .addModel()
                    .condition(face.property(), true)
                    .end();

            var noFace =
                    builder.part()
                            .modelFile(model)
                            .rotationX(face.rotationX())
                            .rotationY(face.rotationY())
                            .uvLock(locked)
                            .addModel();
            MULTIFACE_FACES.forEach(entry -> noFace.condition(entry.property(), false));
            noFace.end();
        }
    }

    /**
     * 太初菌（移植自 PoopSky 的 {@code primoFungusModel}）。
     * <p>
     * 它不是十字模型，而是拿一块手写的 Blockbench 模型 {@code mia:block/mushroom}
     * 当 parent（菌伞 + 菌柄两段），贴图取 {@code mia:block/mushroom/<注册名>}。
     */
    public static <B extends Block> void mushroomFungus(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block particle) {
        Block block = ctx.getEntry();
        String path = MiaUtil.getBlockPath(block);
        prov.simpleBlock(
                block,
                prov.models()
                        .withExistingParent(path, prov.modLoc("block/mushroom"))
                        .texture("all", prov.modLoc("block/mushroom/" + path))
                        .texture("particle", prov.blockTexture(particle)));
    }

    /**
     * 发光太初菌（移植自 PoopSky 的 {@code glowPrimoFungusModel}）。
     * <p>
     * 拆成 {@code _bottom}（菌柄，不透明）与 {@code _top}（菌伞，{@code translucent}）两个模型，
     * 由 multipart 同时叠加 —— 这样只有菌伞是半透明的，菌柄仍然正常遮挡。
     */
    public static <B extends Block> void glowMushroomFungus(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov, Block cap) {
        Block block = ctx.getEntry();
        String path = MiaUtil.getBlockPath(block);
        ResourceLocation texture = prov.modLoc("block/mushroom/" + path);
        ResourceLocation particle = prov.blockTexture(cap);

        ModelFile bottom =
                prov.models()
                        .withExistingParent(path + "_bottom", prov.mcLoc("block/block"))
                        .texture("all", texture)
                        .texture("particle", particle)
                        .element()
                        .from(5, 0, 5)
                        .to(11, 8, 11)
                        .face(Direction.NORTH)
                        .uvs(6, 9, 9, 13)
                        .texture("#all")
                        .end()
                        .face(Direction.EAST)
                        .uvs(6, 9, 9, 13)
                        .texture("#all")
                        .end()
                        .face(Direction.SOUTH)
                        .uvs(6, 9, 9, 13)
                        .texture("#all")
                        .end()
                        .face(Direction.WEST)
                        .uvs(6, 9, 9, 13)
                        .texture("#all")
                        .end()
                        .face(Direction.UP)
                        .uvs(6, 6, 9, 9)
                        .texture("#all")
                        .end()
                        .face(Direction.DOWN)
                        .uvs(9, 6, 6, 9)
                        .texture("#all")
                        .end()
                        .end();

        ModelFile top =
                prov.models()
                        .withExistingParent(path + "_top", prov.mcLoc("block/block"))
                        .texture("all", texture)
                        .texture("particle", particle)
                        .renderType("translucent")
                        .element()
                        .from(2, 8, 2)
                        .to(14, 14, 14)
                        .face(Direction.NORTH)
                        .uvs(6, 0, 12, 3)
                        .texture("#all")
                        .end()
                        .face(Direction.EAST)
                        .uvs(6, 3, 12, 6)
                        .texture("#all")
                        .end()
                        .face(Direction.SOUTH)
                        .uvs(6, 0, 12, 3)
                        .texture("#all")
                        .end()
                        .face(Direction.WEST)
                        .uvs(6, 3, 12, 6)
                        .texture("#all")
                        .end()
                        .face(Direction.UP)
                        .uvs(6, 6, 0, 0)
                        .texture("#all")
                        .end()
                        .face(Direction.DOWN)
                        .uvs(6, 6, 0, 12)
                        .texture("#all")
                        .end()
                        .end();

        prov.getMultipartBuilder(block)
                .part()
                .modelFile(bottom)
                .addModel()
                .end()
                .part()
                .modelFile(top)
                .addModel()
                .end();
    }

    /**
     * 半透明 cubeAll（发光太初菌伞）：与 {@link #base} 的唯一区别是 {@code translucent} 渲染层。
     * <p>
     * 注意这里只出方块状态，物品模型仍交给 {@code .simpleItem()}（parent 指向同一个模型）。
     */
    public static <B extends Block> void translucentCubeAll(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        prov.simpleBlock(
                block,
                prov.models()
                        .cubeAll(MiaUtil.getBlockPath(block), prov.blockTexture(block))
                        .renderType("translucent"));
    }

    /**
     * 旧 {@code createBrushableBlock}：按 {@code DUSTED} 切 {@code cubeAll} 模型。
     */
    public static <B extends Block> void brushable(
            DataGenContext<Block, B> ctx, ReginthBlockstateProvider prov) {
        Block block = ctx.getEntry();
        prov.getVariantBuilder(block)
                .forAllStates(
                        state -> {
                            String suffix = "_" + state.getValue(BlockStateProperties.DUSTED);
                            ModelFile model =
                                    prov.models()
                                            .cubeAll(
                                                    MiaUtil.getBlockPath(block) + suffix,
                                                    prov.modLoc(
                                                            "block/"
                                                                    + MiaUtil.getBlockPath(block)
                                                                    + suffix));
                            return ConfiguredModel.builder().modelFile(model).build();
                        });
    }

    private record MultifaceFace(BooleanProperty property, int rotationX, int rotationY) {}

    /**
     * 旧 {@code makeCropBlock} / {@code states}：按 {@code AGE} 切 {@code block/crop} 模型。
     */
    public static void crop(
            DataGenContext<Block, CropBlock> ctx, ReginthBlockstateProvider prov, String texture) {
        CropBlock cropBlock = ctx.getEntry();
        Function<BlockState, ConfiguredModel[]> function =
                state ->
                        new ConfiguredModel[] {
                            new ConfiguredModel(
                                    prov.models()
                                            .crop(
                                                    ctx.getName() + state.getValue(CropBlock.AGE),
                                                    MiaUtil.miaId(
                                                            "block/"
                                                                    + texture
                                                                    + state.getValue(
                                                                            CropBlock.AGE)))
                                            .renderType("cutout"))
                        };
        prov.getVariantBuilder(cropBlock).forAllStates(function);
    }

    private static void facing(
            ReginthBlockstateProvider prov, Block block, ResourceLocation model) {
        ModelFile file = prov.models().getExistingFile(model);
        prov.getVariantBuilder(block)
                .partialState()
                .with(BlockStateProperties.FACING, Direction.DOWN)
                .modelForState()
                .modelFile(file)
                .rotationX(180)
                .addModel()
                .partialState()
                .with(BlockStateProperties.FACING, Direction.UP)
                .modelForState()
                .modelFile(file)
                .addModel()
                .partialState()
                .with(BlockStateProperties.FACING, Direction.NORTH)
                .modelForState()
                .modelFile(file)
                .rotationX(90)
                .addModel()
                .partialState()
                .with(BlockStateProperties.FACING, Direction.SOUTH)
                .modelForState()
                .modelFile(file)
                .rotationX(90)
                .rotationY(180)
                .addModel()
                .partialState()
                .with(BlockStateProperties.FACING, Direction.WEST)
                .modelForState()
                .modelFile(file)
                .rotationX(90)
                .rotationY(270)
                .addModel()
                .partialState()
                .with(BlockStateProperties.FACING, Direction.EAST)
                .modelForState()
                .modelFile(file)
                .rotationX(90)
                .rotationY(90)
                .addModel();
    }

    private static ConfiguredModel configuredModel(
            ReginthBlockstateProvider prov, ResourceLocation model) {
        return configuredModel(prov, model, 0);
    }

    private static ConfiguredModel configuredModel(
            ReginthBlockstateProvider prov, ResourceLocation model, int rotationY) {
        return new ConfiguredModel(prov.models().getExistingFile(model), 0, rotationY, false);
    }

    // ------------------------------------------------------------------
    // 物品模型（只有这几类不能靠 .simpleItem() 自动生成）
    // ------------------------------------------------------------------

    /**
     * 栅栏物品：{@code minecraft:block/fence_inventory} + texture。
     */
    public static void fenceItem(
            DataGenContext<Item, ? extends Item> ctx, ReginthItemModelProvider prov, Block base) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("block/fence_inventory"))
                .texture("texture", blockTexture(prov, base));
    }

    /**
     * 墙物品：{@code minecraft:block/wall_inventory} + wall 贴图槽。
     */
    public static void wallItem(
            DataGenContext<Item, ? extends Item> ctx, ReginthItemModelProvider prov, Block base) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("block/wall_inventory"))
                .texture("wall", blockTexture(prov, base));
    }

    /**
     * 按钮物品：{@code minecraft:block/button_inventory} + texture。
     */
    public static void buttonItem(
            DataGenContext<Item, ? extends Item> ctx, ReginthItemModelProvider prov, Block base) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("block/button_inventory"))
                .texture("texture", blockTexture(prov, base));
    }

    /**
     * 门物品：平面贴图（{@code mia:item/<name>}），不是方块模型。
     */
    public static <T extends Item> void doorItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                .texture("layer0", prov.modLoc("item/" + ctx.getName()));
    }

    /**
     * ItemModelProvider 没有 {@code blockTexture(Block)}（那是 BlockStateProvider 的），自己按注册名拼。
     */
    private static ResourceLocation blockTexture(ReginthItemModelProvider prov, Block block) {
        return prov.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath());
    }

    /**
     * 树苗/植物/晶簇物品：平面贴图，但用的是**方块**贴图 {@code mia:block/<name>}。
     */
    public static <T extends Item> void bushItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        bushItem(ctx, prov, "");
    }

    /**
     * 同上，但贴图带后缀（双格植物用 {@code _top}）。
     */
    public static <T extends Item> void bushItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov, String suffix) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                .texture("layer0", prov.modLoc("block/" + ctx.getName() + suffix));
    }

    /**
     * 等价于旧 {@code blockItem(block)}：parent 指向同名方块模型。
     */
    public static <T extends Item> void itemParent(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        itemParent(ctx, prov, "");
    }

    /**
     * 等价于旧 {@code blockItem(block, suffix)}：parent 指向 {@code mia:block/<name><suffix>}。
     */
    public static <T extends Item> void itemParent(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov, String suffix) {
        prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName() + suffix));
    }

    /**
     * 等价于旧 {@code aloneItem(block)}：平面贴图 {@code mia:item/<name>}。
     */
    public static <T extends Item> void aloneItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        // parent is minecraft:item/generated -- the old code used MiaUtil.id(), which is the
        // vanilla namespace, NOT modLoc().
        prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                .texture("layer0", prov.modLoc("item/" + ctx.getName()));
    }

    /**
     * 技能物品：{@code minecraft:item/generated} + {@code mia:item/skill/<name>}。
     */
    public static <T extends Item> void skillItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                .texture("layer0", prov.modLoc("item/skill/" + ctx.getName()));
    }

    /**
     * 手持物品（剑/镐/锄等）：parent 用 {@code minecraft:item/handheld}（不是 item/generated）。
     */
    public static <T extends Item> void handheldItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), prov.mcLoc("item/handheld"))
                .texture("layer0", prov.modLoc("item/" + ctx.getName()));
    }

    /**
     * 用 {@code mia:item/template/<template>} 当 parent 的物品（例如抓钩），贴图取自身。
     */
    public static <T extends Item> void templateItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov, String template) {
        prov.withExistingParent(ctx.getName(), prov.modLoc("item/template/" + template))
                .texture("layer0", prov.modLoc("item/" + ctx.getName()));
    }

    /**
     * 活板门物品：parent 指向 {@code mia:block/<name>_bottom}。
     * <p>
     * <b>不能靠 {@code .simpleItem()} 自动推导</b>：实测它在活板门上会走进回退分支，生成
     * {@code {"parent":"mia:block/skyfog_trapdoor"}} —— 而活板门的模型叫 {@code _bottom}/{@code _top}…，
     * 这个名字不存在，datagen 直接抛
     * {@code IllegalStateException: Model at mia:block/<name> does not exist}。
     */
    public static <T extends Item> void trapdoorItem(
            DataGenContext<Item, T> ctx, ReginthItemModelProvider prov) {
        prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName() + "_bottom"));
    }
}
