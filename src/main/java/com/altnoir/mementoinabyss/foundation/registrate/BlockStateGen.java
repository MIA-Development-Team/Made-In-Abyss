package com.altnoir.mementoinabyss.foundation.registrate;

import com.altnoir.mementoinabyss.content.abyss.column.ColumnBlock;
import com.altnoir.mementoinabyss.content.abyss.column.ColumnSide;
import com.altnoir.mementoinabyss.content.abyss.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.DoubleBerryBlock;
import com.altnoir.mementoinabyss.content.rope.RopeConnectorBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

public class BlockStateGen {
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    abyssPortal() {
        return (ctx, prov) -> {
            var portal = TextureSlot.create("portal");
            var texture = prov.modBlockTexture(ctx.getName());
            var model =
                    prov.getBuilder()
                            .texture(portal, texture)
                            .texture(TextureSlot.PARTICLE, texture)
                            .transformTemplate(
                                    template ->
                                            template.ambientOcclusion(false)
                                                    .element(
                                                            element ->
                                                                    element.from(0, 1, 0)
                                                                            .to(16, 15, 16)
                                                                            .face(
                                                                                    net.minecraft
                                                                                            .core
                                                                                            .Direction
                                                                                            .UP,
                                                                                    face ->
                                                                                            face.uvs(
                                                                                                            0,
                                                                                                            0,
                                                                                                            16,
                                                                                                            16)
                                                                                                    .texture(
                                                                                                            portal))
                                                                            .face(
                                                                                    net.minecraft
                                                                                            .core
                                                                                            .Direction
                                                                                            .DOWN,
                                                                                    face ->
                                                                                            face.uvs(
                                                                                                            0,
                                                                                                            0,
                                                                                                            16,
                                                                                                            16)
                                                                                                    .texture(
                                                                                                            portal))))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    abyssPortalCore() {
        return (ctx, prov) -> {
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/abyss_portal_core"))
                            .texture(
                                    TextureSlot.ALL,
                                    prov.modBlockTexture("model/abyss_portal_core"))
                            .texture(TextureSlot.BOTTOM, prov.modBlockTexture("abyss_portal_frame"))
                            .texture(
                                    TextureSlot.PARTICLE,
                                    prov.modBlockTexture("abyss_portal_frame"))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    caveExplorerBeacon() {
        return (ctx, prov) -> {
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/abyss_beacon"))
                            .texture(
                                    TextureSlot.ALL,
                                    prov.modBlockTexture("model/cave_explorer_beacon"))
                            .texture(
                                    TextureSlot.PARTICLE,
                                    prov.modBlockTexture("cave_explorer_beacon_particle"))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> pedestal() {
        return (ctx, prov) -> {
            var pedestal = TextureSlot.create("pedestal");
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/pedestal"))
                            .texture(pedestal, prov.modBlockTexture("model/pedestal"))
                            .texture(TextureSlot.PARTICLE, prov.modBlockTexture("abyss_andesite"))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    endlessCup() {
        return (ctx, prov) -> {
            var all = TextureSlot.create("all");
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/endless_cup"))
                            .texture(all, prov.modBlockTexture("model/endless_cup"))
                            .texture(
                                    TextureSlot.PARTICLE,
                                    prov.modBlockTexture("endless_cup_particle"))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    hopperFarmland() {
        return (ctx, prov) -> {
            var top = TextureSlot.create("top");
            var side = TextureSlot.create("side");
            var bottom = TextureSlot.create("bottom");
            var sideTex = prov.modBlockTexture("abyss_andesite");
            var bottomTex = prov.modBlockTexture(ctx.getName() + "_bottom");
            var dry =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/hopper_farmland"))
                            .texture(top, prov.modBlockTexture(ctx.getName()))
                            .texture(side, sideTex)
                            .texture(bottom, bottomTex)
                            .texture(TextureSlot.PARTICLE, bottomTex)
                            .build(ctx.get());
            var moist =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/hopper_farmland"))
                            .texture(top, prov.modBlockTexture(ctx.getName() + "_moist"))
                            .texture(side, sideTex)
                            .texture(bottom, bottomTex)
                            .texture(TextureSlot.PARTICLE, bottomTex)
                            .build(prov.modLoc("block/" + ctx.getName() + "_moist"));
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    BlockModelGenerators.createEmptyOrFullDispatch(
                                            BlockStateProperties.MOISTURE,
                                            7,
                                            BlockModelGenerators.plainVariant(moist),
                                            BlockModelGenerators.plainVariant(dry))));
            prov.registerSimpleItemModel(ctx.get(), dry);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    artifactSmithingTable() {
        return (ctx, prov) -> {
            var bottom = prov.modBlockTexture("chiseled_abyss_andesite");
            var northSouth = prov.modBlockTexture("artifact_smithing_table_1");
            var eastWest = prov.modBlockTexture("artifact_smithing_table_2");
            var top = prov.modBlockTexture("artifact_smithing_table_top");
            var textures =
                    new TextureMapping()
                            .put(TextureSlot.PARTICLE, bottom)
                            .put(TextureSlot.DOWN, bottom)
                            .put(TextureSlot.UP, top)
                            .put(TextureSlot.NORTH, northSouth)
                            .put(TextureSlot.SOUTH, northSouth)
                            .put(TextureSlot.EAST, eastWest)
                            .put(TextureSlot.WEST, eastWest);
            var model = ModelTemplates.CUBE.create(ctx.get(), textures, prov.modelOutput);
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    whistleWorkbench() {
        return (ctx, prov) -> {
            var bottom = prov.modBlockTexture("chiseled_abyss_andesite");
            var side = prov.modBlockTexture("artifact_smithing_table_2");
            var top = prov.modBlockTexture("artifact_smithing_table_top");
            var textures =
                    new TextureMapping()
                            .put(TextureSlot.PARTICLE, bottom)
                            .put(TextureSlot.DOWN, bottom)
                            .put(TextureSlot.UP, top)
                            .put(TextureSlot.NORTH, side)
                            .put(TextureSlot.SOUTH, side)
                            .put(TextureSlot.EAST, side)
                            .put(TextureSlot.WEST, side);
            var model = ModelTemplates.CUBE.create(ctx.get(), textures, prov.modelOutput);
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    ropeConnector() {
        return (ctx, prov) -> {
            var idle = BlockModelGenerators.plainVariant(prov.modLoc("block/rope_connector"));
            var connected =
                    BlockModelGenerators.plainVariant(
                            prov.modLoc("block/rope_connector_connected"));
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(RopeConnectorBlock.CONNECTED)
                                            .select(false, idle)
                                            .select(true, connected)));
            prov.registerSimpleItemModel(ctx.get(), prov.modLoc("block/rope_connector"));
        };
    }

    public static <B extends CoverGrassBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    coverGrass() {
        return (ctx, prov) -> {
            var block = ctx.getEntry();
            var model =
                    ModelTemplates.CUBE_BOTTOM_TOP.create(
                            block,
                            sideBottomTop(
                                    prov.modBlockTexture(ctx.getName() + "_side"),
                                    prov.blockTexture(block.defaultBlock),
                                    prov.modBlockTexture("abyss_grass_block_top")),
                            prov.modelOutput);

            var variants =
                    BlockModelGenerators.createRotatedVariants(
                            BlockModelGenerators.plainModel(model));
            prov.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variants));
            prov.registerSimpleItemModel(block, model);
        };
    }

    public static NonNullBiConsumer<
                    DataGenContext<Block, BrushableBlock>, RegistrateBlockModelGenerator>
            suspiciousAbyssAndesite() {
        return (ctx, prov) -> {
            var models = new MultiVariant[4];
            for (int dusted = 0; dusted < models.length; dusted++) {
                var suffix = "_" + dusted;
                var model =
                        ModelTemplates.CUBE_ALL.create(
                                prov.modLoc("block/" + ctx.getName() + suffix),
                                TextureMapping.cube(prov.modBlockTexture(ctx.getName() + suffix)),
                                prov.modelOutput);
                models[dusted] = BlockModelGenerators.plainVariant(model);
            }
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(BlockStateProperties.DUSTED)
                                            .generate(dusted -> models[dusted])));
        };
    }

    public static <B extends RotatedPillarBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> variantLog(
                    int... weights) {
        return variantAxisBlock(null, weights);
    }

    public static <B extends RotatedPillarBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> variantWood(
                    BlockEntry<? extends Block> barkSource, int... weights) {
        return variantAxisBlock(barkSource, weights);
    }

    /** Generates weighted axis models and uses the first vertical model for the block item. */
    private static <B extends RotatedPillarBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    variantAxisBlock(
                            @Nullable BlockEntry<? extends Block> barkSource, int[] weights) {
        if (weights.length == 0)
            throw new IllegalArgumentException("At least one variant is required");
        int[] variantWeights = weights.clone();
        for (int weight : variantWeights) {
            if (weight < 1) throw new IllegalArgumentException("Weights must be positive");
        }
        return (ctx, prov) -> {
            var blockPath = ctx.getName();
            WeightedList.Builder<Variant> verticalBuilder = WeightedList.builder();
            WeightedList.Builder<Variant> horizontalBuilder = WeightedList.builder();

            for (int i = 0; i < variantWeights.length; i++) {
                var sideTexture =
                        barkSource != null
                                ? prov.blockTexture(barkSource.get(), Integer.toString(i))
                                : prov.modBlockTexture(blockPath + i);
                var endTexture =
                        barkSource != null ? sideTexture : prov.modBlockTexture(blockPath + "_top");
                var mapping = TextureMapping.column(sideTexture, endTexture);
                var verticalModel =
                        ModelTemplates.CUBE_COLUMN.create(
                                prov.modLoc("block/" + blockPath + i), mapping, prov.modelOutput);
                var horizontalModel =
                        ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(
                                prov.modLoc("block/" + blockPath + "_horizontal" + i),
                                mapping,
                                prov.modelOutput);
                verticalBuilder.add(
                        BlockModelGenerators.plainModel(verticalModel), variantWeights[i]);
                horizontalBuilder.add(
                        BlockModelGenerators.plainModel(horizontalModel), variantWeights[i]);
                if (i == 0) prov.registerSimpleItemModel(ctx.get(), verticalModel);
            }
            prov.generateAxisBlock(
                    ctx.get(),
                    new MultiVariant(verticalBuilder.build()),
                    new MultiVariant(horizontalBuilder.build()));
        };
    }

    public static NonNullBiConsumer<
                    DataGenContext<Block, ColumnBlock>, RegistrateBlockModelGenerator>
            column(BlockEntry<? extends Block> pillar, BlockEntry<? extends Block> decoration) {
        return (ctx, prov) -> {
            var side = prov.blockTexture(pillar.get());
            var top = prov.blockTexture(pillar.get(), "_top");
            var dec = prov.blockTexture(decoration.get());
            var decSlot = TextureSlot.create("dec");
            var baseName = "block/" + ctx.getName();

            var single =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/column"))
                            .texture(TextureSlot.SIDE, side)
                            .texture(TextureSlot.TOP, top)
                            .texture(decSlot, dec)
                            .build(prov.modLoc(baseName));
            var bottom =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/column_bottom"))
                            .texture(TextureSlot.SIDE, side)
                            .texture(TextureSlot.TOP, top)
                            .texture(decSlot, dec)
                            .build(prov.modLoc(baseName + "_bottom"));
            var middle =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/column_middle"))
                            .texture(TextureSlot.SIDE, side)
                            .build(prov.modLoc(baseName + "_middle"));
            var columnTop =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/column_top"))
                            .texture(TextureSlot.SIDE, side)
                            .texture(TextureSlot.TOP, top)
                            .texture(decSlot, dec)
                            .build(prov.modLoc(baseName + "_top"));

            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(ColumnBlock.COLUMN)
                                            .select(
                                                    ColumnSide.NONE,
                                                    BlockModelGenerators.plainVariant(single))
                                            .select(
                                                    ColumnSide.BOTTOM,
                                                    BlockModelGenerators.plainVariant(bottom))
                                            .select(
                                                    ColumnSide.MIDDLE,
                                                    BlockModelGenerators.plainVariant(middle))
                                            .select(
                                                    ColumnSide.TOP,
                                                    BlockModelGenerators.plainVariant(columnTop))));
            prov.registerSimpleItemModel(ctx.get(), single);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    crossPlant() {
        return (ctx, prov) ->
                prov.createCrossBlock(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    doublePlant() {
        return (ctx, prov) ->
                prov.createDoublePlant(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> flowerBed() {
        return (ctx, prov) -> prov.createFlowerBed(ctx.get());
    }

    public static <B extends DoubleBerryBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    doubleBerry() {
        return (ctx, prov) -> {
            var models = new Identifier[2][DoubleBerryBlock.MAX_AGE + 1];
            for (int age = 0; age <= DoubleBerryBlock.MAX_AGE; age++) {
                String bottomSuffix = "_bottom" + age;
                models[0][age] =
                        BlockModelGenerators.PlantType.NOT_TINTED
                                .getCross()
                                .createWithSuffix(
                                        ctx.get(),
                                        bottomSuffix,
                                        TextureMapping.cross(
                                                prov.modBlockTexture(ctx.getName() + bottomSuffix)),
                                        prov.modelOutput);
                if (age >= 2) {
                    String topSuffix = "_top" + age;
                    models[1][age] =
                            BlockModelGenerators.PlantType.NOT_TINTED
                                    .getCross()
                                    .createWithSuffix(
                                            ctx.get(),
                                            topSuffix,
                                            TextureMapping.cross(
                                                    prov.modBlockTexture(
                                                            ctx.getName() + topSuffix)),
                                            prov.modelOutput);
                }
            }
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(
                                                    DoubleBerryBlock.HALF, DoubleBerryBlock.AGE)
                                            .generate(
                                                    (half, age) -> {
                                                        var model =
                                                                half == DoubleBlockHalf.UPPER
                                                                                && age >= 2
                                                                        ? models[1][age]
                                                                        : models[0][age];
                                                        return BlockModelGenerators.plainVariant(
                                                                model);
                                                    })));
        };
    }

    public static <B extends StairBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> stairs(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) -> prov.generateStairsBlock(ctx.get(), prov.blockTexture(base.get()));
    }

    public static <B extends SlabBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> slab(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.generateSlabBlock(
                        ctx.get(),
                        BlockModelGenerators.plainVariant(
                                ModelLocationUtils.getModelLocation(base.get())),
                        prov.blockTexture(base.get()));
    }

    public static <B extends WallBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> wall(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) -> {
            prov.generateWallBlock(ctx.get(), prov.blockTexture(base.get()));
            prov.registerSimpleItemModel(
                    ctx.get(),
                    ModelTemplates.WALL_INVENTORY.create(
                            ctx.get().asItem(),
                            TextureMapping.singleSlot(
                                    TextureSlot.WALL, prov.blockTexture(base.get())),
                            prov.modelOutput));
        };
    }

    public static <B extends FenceBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> fence(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).fence(ctx.get());
    }

    public static <B extends FenceGateBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> fenceGate(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).fenceGate(ctx.get());
    }

    public static <B extends PressurePlateBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    pressurePlate(BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get()))
                        .pressurePlate(ctx.get());
    }

    public static <B extends ButtonBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> button(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).button(ctx.get());
    }

    /** cube_all with four Y-axis rotations (mycelium block). */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    rotationYCubeAll() {
        return (ctx, prov) -> prov.createRotatedVariantBlock(ctx.get());
    }

    /**
     * Multiface mat: thin north-face model (like glow lichen) + multipart blockstates. Item model
     * is registered separately as a flat item.
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    multifaceMat() {
        return (ctx, prov) -> {
            var multiface = TextureSlot.create("multiface");
            var texture = prov.modBlockTexture(ctx.getName());
            prov.getBuilder()
                    .texture(multiface, texture)
                    .texture(TextureSlot.PARTICLE, texture)
                    .transformTemplate(
                            template ->
                                    template.ambientOcclusion(false)
                                            .element(
                                                    element ->
                                                            element.from(0, 0, 0.1F)
                                                                    .to(16, 16, 0.1F)
                                                                    .face(
                                                                            Direction.NORTH,
                                                                            face ->
                                                                                    face.uvs(
                                                                                                    16,
                                                                                                    0,
                                                                                                    0,
                                                                                                    16)
                                                                                            .texture(
                                                                                                    multiface))
                                                                    .face(
                                                                            Direction.SOUTH,
                                                                            face ->
                                                                                    face.uvs(
                                                                                                    0,
                                                                                                    0,
                                                                                                    16,
                                                                                                    16)
                                                                                            .texture(
                                                                                                    multiface))))
                    .build(ctx.get());
            prov.createMultifaceBlockStates(ctx.get());
        };
    }

    /** Primo fungus: hand-authored mushroom parent + per-block texture. */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    mushroomFungus() {
        return (ctx, prov) -> {
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/mushroom"))
                            .texture(
                                    TextureSlot.ALL,
                                    prov.modBlockTexture("mushroom/" + ctx.getName()))
                            .texture(TextureSlot.PARTICLE, prov.modBlockTexture("primo_planks"))
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    /**
     * Glow primo fungus: opaque stem + translucent cap multipart using hand-written models in
     * main resources.
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    glowMushroomFungus() {
        return (ctx, prov) -> {
            Identifier bottom = prov.modLoc("block/glow_primo_fungus_bottom");
            Identifier top = prov.modLoc("block/glow_primo_fungus_top");
            prov.blockStateOutput.accept(
                    MultiPartGenerator.multiPart(ctx.get())
                            .with(BlockModelGenerators.plainVariant(bottom))
                            .with(BlockModelGenerators.plainVariant(top)));
        };
    }

    /** Translucent cube_all referencing a hand-written model. */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator>
                    translucentCubeAll() {
        return (ctx, prov) -> {
            Identifier model = ModelLocationUtils.getModelLocation(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    private static TextureMapping sideBottomTop(Material side, Material bottom, Material top) {
        return new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top);
    }
}
