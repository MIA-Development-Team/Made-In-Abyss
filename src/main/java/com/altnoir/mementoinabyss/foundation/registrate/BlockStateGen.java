package com.altnoir.mementoinabyss.foundation.registrate;

import com.altnoir.abysslib.reginth.providers.DataGenContext;
import com.altnoir.abysslib.reginth.providers.generators.ReginthBlockModelGenerator;
import com.altnoir.abysslib.reginth.util.entry.BlockEntry;
import com.altnoir.abysslib.reginth.util.nullness.NonNullBiConsumer;
import com.altnoir.mementoinabyss.content.abyss.column.ColumnBlock;
import com.altnoir.mementoinabyss.content.abyss.column.ColumnSide;
import com.altnoir.mementoinabyss.content.abyss.cover_grass.CoverGrassBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.DoubleBerryBlock;
import com.altnoir.mementoinabyss.content.abyss.plant.DuskveilIchorBlock;
import com.altnoir.mementoinabyss.content.rope.RopeConnectorBlock;
import java.util.Optional;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

public class BlockStateGen {
    public static <B extends DuskveilIchorBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    duskveilIchor() {
        return (ctx, prov) -> {
            var idle =
                    ModelTemplates.CUBE_COLUMN.create(
                            prov.modLoc("block/duskveil_ichor"),
                            TextureMapping.column(
                                    prov.modBlockTexture("duskveil_ichor"),
                                    prov.modBlockTexture("duskveil_ichor_top")),
                            prov.modelOutput);
            var awake =
                    ModelTemplates.CUBE_COLUMN.create(
                            prov.modLoc("block/duskveil_ichor_awake"),
                            TextureMapping.column(
                                    prov.modBlockTexture("duskveil_ichor_awake"),
                                    prov.modBlockTexture("duskveil_ichor_top_awake")),
                            prov.modelOutput);
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(DuskveilIchorBlock.READY)
                                            .select(false, BlockModelGenerators.plainVariant(idle))
                                            .select(true, BlockModelGenerators.plainVariant(awake)))
                            .with(BlockModelGenerators.createRotatedPillar()));
            prov.registerSimpleItemModel(ctx.get(), awake);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> abyssPortal() {
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
                                                                                    Direction.UP,
                                                                                    face ->
                                                                                            face.uvs(
                                                                                                            0,
                                                                                                            0,
                                                                                                            16,
                                                                                                            16)
                                                                                                    .texture(
                                                                                                            portal))
                                                                            .face(
                                                                                    Direction.DOWN,
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> pedestal() {
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> endlessCup() {
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    artifactSmithingTable() {
        return (ctx, prov) -> {
            var texture = prov.modBlockTexture("model/" + ctx.getName());
            var particle = prov.modBlockTexture("model/" + ctx.getName() + "_particle");
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/artifact_smithing_table"))
                            .texture(TextureSlot.ALL, texture)
                            .texture(TextureSlot.PARTICLE, particle)
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    whistleWorkbench() {
        return (ctx, prov) -> {
            var texture = prov.modBlockTexture("model/" + ctx.getName());
            var particle = prov.modBlockTexture("chiseled_abyss_andesite");
            var model =
                    prov.getBuilder()
                            .parent(prov.modLoc("block/template/whistle_workbench"))
                            .texture(TextureSlot.ALL, texture)
                            .texture(TextureSlot.PARTICLE, particle)
                            .build(ctx.get());
            prov.create(ctx.get(), model);
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> lampTube(
                    Identifier particle) {
        return (ctx, prov) -> {
            var lampTube = TextureSlot.create("lamp_tube");
            prov.getBuilder()
                    .parent(prov.modLoc("block/template/lamp_tube"))
                    .texture(lampTube, prov.modBlockTexture("model/" + ctx.getName()))
                    .texture(TextureSlot.PARTICLE, new Material(particle))
                    .build(ctx.get());
            var model =
                    BlockModelGenerators.plainVariant(
                            ModelLocationUtils.getModelLocation(ctx.get()));
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get(), model)
                            .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
            prov.registerSimpleItemModel(ctx.get(), ModelLocationUtils.getModelLocation(ctx.get()));
        };
    }

    public static <B extends CoverGrassBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    grassyAbyssBlock() {
        return (ctx, prov) -> {
            var overlay = TextureSlot.create("overlay");
            var template =
                    new ModelTemplate(
                            Optional.of(Identifier.withDefaultNamespace("block/grass_block")),
                            Optional.empty(),
                            TextureSlot.PARTICLE,
                            TextureSlot.SIDE,
                            TextureSlot.BOTTOM,
                            TextureSlot.TOP,
                            overlay);
            var block = ctx.getEntry();
            var textures =
                    sideBottomTop(
                                    prov.modBlockTexture(ctx.getName() + "_side"),
                                    prov.blockTexture(block.defaultBlock),
                                    prov.modBlockTexture("abyss_grass_block_top"))
                            .put(TextureSlot.PARTICLE, prov.blockTexture(block.defaultBlock))
                            .put(overlay, prov.modBlockTexture("grassy_abyss_side_overlay"));
            var model = template.create(block, textures, prov.modelOutput);
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(
                            block,
                            BlockModelGenerators.createRotatedVariants(
                                    BlockModelGenerators.plainModel(model))));
            prov.itemModelOutput.accept(
                    block.asItem(), ItemModelUtils.tintedModel(model, new Constant(11335504)));
        };
    }

    public static NonNullBiConsumer<
                    DataGenContext<Block, BrushableBlock>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> variantLog(
                    int... weights) {
        return variantAxisBlock(null, weights);
    }

    public static <B extends RotatedPillarBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> variantWood(
                    BlockEntry<? extends Block> barkSource, int... weights) {
        return variantAxisBlock(barkSource, weights);
    }

    /**
     * Generates weighted axis models and uses the first vertical model for the block item.
     */
    private static <B extends RotatedPillarBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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

    public static NonNullBiConsumer<DataGenContext<Block, ColumnBlock>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> crossPlant() {
        return (ctx, prov) ->
                prov.createCrossBlock(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    duskveilVines() {
        return (ctx, prov) -> {
            var empty =
                    BlockModelGenerators.PlantType.NOT_TINTED
                            .getCross()
                            .create(
                                    prov.modLoc("block/" + ctx.getName()),
                                    TextureMapping.cross(prov.modBlockTexture(ctx.getName())),
                                    prov.modelOutput);
            var berries =
                    BlockModelGenerators.PlantType.NOT_TINTED
                            .getCross()
                            .create(
                                    prov.modLoc("block/" + ctx.getName() + "_berries"),
                                    TextureMapping.cross(
                                            prov.modBlockTexture(ctx.getName() + "_berries")),
                                    prov.modelOutput);
            prov.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(ctx.get())
                            .with(
                                    PropertyDispatch.initial(BlockStateProperties.BERRIES)
                                            .select(false, BlockModelGenerators.plainVariant(empty))
                                            .select(
                                                    true,
                                                    BlockModelGenerators.plainVariant(berries))));
        };
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> doublePlant() {
        return (ctx, prov) ->
                prov.createDoublePlant(ctx.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> flowerBed() {
        return (ctx, prov) -> prov.createFlowerBed(ctx.get());
    }

    public static <B extends DoubleBerryBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> doubleBerry() {
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> stairs(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) -> prov.generateStairsBlock(ctx.get(), prov.blockTexture(base.get()));
    }

    public static <B extends SlabBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> slab(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.generateSlabBlock(
                        ctx.get(),
                        BlockModelGenerators.plainVariant(
                                ModelLocationUtils.getModelLocation(base.get())),
                        prov.blockTexture(base.get()));
    }

    public static <B extends WallBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> wall(
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> fence(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).fence(ctx.get());
    }

    public static <B extends FenceGateBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> fenceGate(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).fenceGate(ctx.get());
    }

    public static <B extends PressurePlateBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> pressurePlate(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get()))
                        .pressurePlate(ctx.get());
    }

    public static <B extends ButtonBlock>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> button(
                    BlockEntry<? extends Block> base) {
        return (ctx, prov) ->
                prov.new BlockFamilyProvider(TextureMapping.cube(base.get())).button(ctx.get());
    }

    /**
     * cube_all with four Y-axis rotations (mycelium block).
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
                    rotationYCubeAll() {
        return (ctx, prov) -> prov.createRotatedVariantBlock(ctx.get());
    }

    /**
     * Multiface mat: thin north-face model (like glow lichen) + multipart blockstates. Item model
     * is registered separately as a flat item.
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator> multifaceMat() {
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

    /**
     * Primo fungus: hand-authored mushroom parent + per-block texture.
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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

    /**
     * Translucent cube_all referencing a hand-written model.
     */
    public static <B extends Block>
            NonNullBiConsumer<DataGenContext<Block, B>, ReginthBlockModelGenerator>
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
