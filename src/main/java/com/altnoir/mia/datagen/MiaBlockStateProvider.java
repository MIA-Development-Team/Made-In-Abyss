package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.AbyssPortalFrameBlock;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import com.altnoir.mia.datagen.blockstate.MiaStateProvider;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class MiaBlockStateProvider extends BlockStateProvider {
    public MiaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    private static final MiaModelProvider modelP = new MiaModelProvider();
    private static final MiaStateProvider stateP = new MiaStateProvider();

    @Override
    protected void registerStatesAndModels() {
        // 模板模型
        modelP.templateTSBModel(this);
        modelP.templateHopperFarmland(this);

        // props
        ropeBlock();
        lampTubeBlock(MiaBlocks.AMETHYST_LAMPTUBE, Blocks.AMETHYST_BLOCK);
        lampTubeBlock(MiaBlocks.PRASIOLITE_LAMPTUBE, MiaBlocks.PRASIOLITE_BLOCK.get());
        pedestalBlock();
        abyssSpawnerBlock();
        abyssPortalBlock();
        abyssPortalFrameBlock();

        // artifact
        artifactSmithingTableBlock();
        hopperFarmBlock();
        endlessCupBlock();

        // decoration blocks
        coverGrassBlock(MiaBlocks.COVERGRASS_ABYSS_ANDESITE, MiaBlocks.ABYSS_ANDESITE);
        coverGrassBlock(MiaBlocks.COVERGRASS_TUFF, Blocks.TUFF);
        mirroredBlock(MiaBlocks.ABYSS_ANDESITE);
        // 深界安山岩
        stairsBlockWithItem(MiaBlocks.ABYSS_ANDESITE_STAIRS, MiaBlocks.ABYSS_ANDESITE);
        slabBlockWithItem(MiaBlocks.ABYSS_ANDESITE_SLAB, MiaBlocks.ABYSS_ANDESITE);
        wallBlockWithItem(MiaBlocks.ABYSS_ANDESITE_WALL, MiaBlocks.ABYSS_ANDESITE);
        // 圆石
        blockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE);
        stairsBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS, MiaBlocks.ABYSS_COBBLED_ANDESITE);
        slabBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB, MiaBlocks.ABYSS_COBBLED_ANDESITE);
        wallBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL, MiaBlocks.ABYSS_COBBLED_ANDESITE);
        // 苔石
        blockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        stairsBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        slabBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        wallBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        //  磨制
        blockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE);
        stairsBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        slabBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        wallBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        pillarBlockWithItem(MiaBlocks.ABYSS_ANDESITE_PILLAR);
        // 石砖
        blockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS);
        blockWithItem(MiaBlocks.CHISLED_ABYSS_ANDESITE);
        stairsBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        slabBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        wallBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        blockWithItem(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS);
        // 苔石砖
        blockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        stairsBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        slabBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        wallBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);

        // 化石树
        variantPillarBlockWithItem(MiaBlocks.FOSSILIZED_LOG, 3);
        variantAxisBlockWithItem(MiaBlocks.FOSSILIZED_WOOD, MiaBlocks.FOSSILIZED_LOG, 3);
        variantPillarBlockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_LOG, 5, new int[]{12, 1, 1, 1, 1});
        variantAxisBlockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_WOOD, MiaBlocks.STRIPPED_FOSSILIZED_LOG, 5, new int[]{12, 1, 1, 1, 1});

        blockWithItem(MiaBlocks.POLISHED_FOSSILIZED_WOOD);
        stairsBlockWithItem(MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS, MiaBlocks.POLISHED_FOSSILIZED_WOOD);
        slabBlockWithItem(MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB, MiaBlocks.POLISHED_FOSSILIZED_WOOD);
        wallBlockWithItem(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL, MiaBlocks.POLISHED_FOSSILIZED_WOOD);
        blockWithItem(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
        stairsBlockWithItem(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
        slabBlockWithItem(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
        wallBlockWithItem(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL, MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
        blockWithItem(MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD);

        blockWithItem(MiaBlocks.FOSSILIZED_WOOD_BRICKS);
        stairsBlockWithItem(MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS, MiaBlocks.FOSSILIZED_WOOD_BRICKS);
        slabBlockWithItem(MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB, MiaBlocks.FOSSILIZED_WOOD_BRICKS);
        wallBlockWithItem(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL, MiaBlocks.FOSSILIZED_WOOD_BRICKS);
        blockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);
        stairsBlockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);
        slabBlockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);
        wallBlockWithItem(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL, MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);

        // 天雾树
        logBlockWithItem(MiaBlocks.SKYFOG_LOG);
        woodBlockWithItem(MiaBlocks.SKYFOG_WOOD, MiaBlocks.SKYFOG_LOG);
        logBlockWithItem(MiaBlocks.STRIPPED_SKYFOG_LOG);
        woodBlockWithItem(MiaBlocks.STRIPPED_SKYFOG_WOOD, MiaBlocks.STRIPPED_SKYFOG_LOG);
        blockWithItem(MiaBlocks.SKYFOG_PLANKS);
        stairsBlockWithItem(MiaBlocks.SKYFOG_STAIRS, MiaBlocks.SKYFOG_PLANKS);
        slabBlockWithItem(MiaBlocks.SKYFOG_SLAB, MiaBlocks.SKYFOG_PLANKS);
        fenceBlockWithItem(MiaBlocks.SKYFOG_FENCE, MiaBlocks.SKYFOG_PLANKS);
        doorBlockWithItem(MiaBlocks.SKYFOG_DOOR);
        trapdoorBlockWithItem(MiaBlocks.SKYFOG_TRAPDOOR);
        fenceGateBlockWithItem(MiaBlocks.SKYFOG_FENCE_GATE, MiaBlocks.SKYFOG_PLANKS);
        sapingAndCrossBlock(MiaBlocks.SKYFOG_SAPLING);
        leavesBlock(MiaBlocks.SKYFOG_LEAVES);
        leavesBlock(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS);

        // 晶石
        blockWithItem(MiaBlocks.PRASIOLITE_BLOCK);
        blockWithItem(MiaBlocks.BUDDING_PRASIOLITE);
        clusterBlock(MiaBlocks.PRASIOLITE_CLUSTER);
        clusterBlock(MiaBlocks.LARGE_PRASIOLITE_BUD);
        clusterBlock(MiaBlocks.MEDIUM_PRASIOLITE_BUD);
        clusterBlock(MiaBlocks.SMALL_PRASIOLITE_BUD);

        // 植物
        sapingAndCrossBlock(MiaBlocks.FORTITUDE_FLOWER);
        //遗物
        blockWithItem(MiaBlocks.SUN_STONE);

        // 红石
        pressurePlateBlockWithItem(MiaBlocks.SKYFOG_PRESSURE_PLATE, MiaBlocks.SKYFOG_PLANKS);
        buttonBlockWithItem(MiaBlocks.SKYFOG_BUTTON, MiaBlocks.SKYFOG_PLANKS);


    }

    private void coverGrassBlock(DeferredBlock<?> modelBlock, DeferredBlock<?> block) {
        models().withExistingParent(MiaUtil.getBlockPath(modelBlock.get()), modLoc("block/template/cube_tsb"))
                .texture("top", this.modLoc("block/abyss_grass_block_top"))
                .texture("side", this.modLoc("block/" + MiaUtil.getBlockPath(modelBlock.get()) + "_side"))
                .texture("bottom", this.modLoc("block/" + MiaUtil.getBlockPath(block.get())));
        stateP.rotationYBlockState(this, modelBlock.get());
        blockItem(modelBlock);
    }

    private void coverGrassBlock(DeferredBlock<?> modelBlock, Block block) {
        models().withExistingParent(MiaUtil.getBlockPath(modelBlock.get()), modLoc("block/template/cube_tsb"))
                .texture("top", this.modLoc("block/abyss_grass_block_top"))
                .texture("side", this.modLoc("block/" + MiaUtil.getBlockPath(modelBlock.get()) + "_side"))
                .texture("bottom", this.mcLoc("block/" + MiaUtil.getBlockPath(block)));
        stateP.rotationYBlockState(this, modelBlock.get());
        blockItem(modelBlock);
    }

    private void mirroredBlock(DeferredBlock<?> block) {
        modelP.mirroredBlockModel(this, block.get());
        stateP.mirroredBlockState(this, block.get());
        blockItem(block);
    }

    private void hopperFarmBlock() {
        modelP.hopperFarmBlockModel(this, MiaBlocks.HOPPER_FARMLAND.get());
        stateP.moistureBlockState(this, MiaBlocks.HOPPER_FARMLAND.get());
        blockItem(MiaBlocks.HOPPER_FARMLAND);
    }

    private void ropeBlock() {
        modelP.ropeBlockModel(this, MiaBlocks.ROPE.get());
        stateP.baseBlockState(this, MiaBlocks.ROPE.get());
        basicItem(MiaBlocks.ROPE);
    }

    private void lampTubeBlock(DeferredBlock<?> block, Block particle) {
        modelP.lampTubeBlockModel(this, block.get(), particle);
        stateP.ColumnWithFacingState(this, block.get());
        blockItem(block);
    }

    private void pedestalBlock() {
        modelP.pedestalBlockModel(this, MiaBlocks.PEDESTAL.get());
        stateP.baseBlockState(this, MiaBlocks.PEDESTAL.get());
        blockItem(MiaBlocks.PEDESTAL);
    }


    private void abyssSpawnerBlock() {
        modelP.abyssSpawnerBlockModel(this, MiaBlocks.ABYSS_SPAWNER.get());
        stateP.abyssSpawnerBlockState(this, MiaBlocks.ABYSS_SPAWNER.get());
        blockItem(MiaBlocks.ABYSS_SPAWNER);
    }

    private void abyssPortalBlock() {
        modelP.abyssPortalBlockModel(this, MiaBlocks.ABYSS_PORTAL.get());
        stateP.baseBlockState(this, MiaBlocks.ABYSS_PORTAL.get());
        crossItem(MiaBlocks.ABYSS_PORTAL);
    }

    private void abyssPortalFrameBlock() {
        ModelFile defaultModel = modelP.abyssPortalFrameModel(this, MiaBlocks.ABYSS_PORTAL_FRAME.get(), false);
        ModelFile openModel = modelP.abyssPortalFrameModel(this, MiaBlocks.ABYSS_PORTAL_FRAME.get(), true);

        getVariantBuilder(MiaBlocks.ABYSS_PORTAL_FRAME.get())
                .partialState().with(AbyssPortalFrameBlock.COMPASS, false)
                .modelForState().modelFile(defaultModel).addModel()
                .partialState().with(AbyssPortalFrameBlock.COMPASS, true)
                .modelForState().modelFile(openModel).addModel();

        blockItem(MiaBlocks.ABYSS_PORTAL_FRAME);
    }

    private void artifactSmithingTableBlock() {
        modelP.artifactSmithingTableBlockModel(this, MiaBlocks.ARTIFACT_SMITHING_TABLE.get(), MiaBlocks.CHISLED_ABYSS_ANDESITE.get());
        stateP.baseBlockState(this, MiaBlocks.ARTIFACT_SMITHING_TABLE.get());
        blockItem(MiaBlocks.ARTIFACT_SMITHING_TABLE);
    }

    private void endlessCupBlock() {
        modelP.endlessCupBlockModel(this, MiaBlocks.ENDLESS_CUP.get());
        stateP.baseBlockState(this, MiaBlocks.ENDLESS_CUP.get());
        basicItem(MiaBlocks.ENDLESS_CUP);
    }

    private void clusterBlock(DeferredBlock<?> block) {
        modelP.crossModel(this, block.get());
        stateP.ColumnWithFacingState(this, block.get());
        crossItem(block);
    }

    protected void sapingAndCrossBlock(DeferredBlock<?> block) {
        simpleBlock(block.get(),
                models().cross(MiaUtil.getBlockPath(block.get()), blockTexture(block.get())).renderType("cutout"));
        crossItem(block);
    }

    protected void leavesBlock(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(),
                models().singleTexture(MiaUtil.getBlockPath(block.get()), mcLoc("block/leaves"),
                        "all", blockTexture(block.get())).renderType("cutout"));
    }

    protected void logBlockWithItem(DeferredBlock<?> block) {
        logBlock((RotatedPillarBlock) block.get());
        blockItem(block);
    }

    protected void woodBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> log) {
        axisBlock((RotatedPillarBlock) block.get(), blockTexture(log.get()), blockTexture(log.get()));
        blockItem(block);
    }

    protected void stairsBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        stairsBlock((StairBlock) block.get(), blockTexture(texture.get()), blockTexture(texture.get()), blockTexture(texture.get()));
        blockItem(block);
    }

    protected void slabBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        slabBlockWithItem(block, blockTexture(texture.get()), texture);
    }

    protected void slabBlockWithItem(DeferredBlock<?> block, ResourceLocation doubleslab, DeferredBlock<?> texture) {
        slabBlock((SlabBlock) block.get(), doubleslab, blockTexture(texture.get()), blockTexture(texture.get()), blockTexture(texture.get()));
        blockItem(block);
    }

    protected void fenceBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        fenceBlock((FenceBlock) block.get(), blockTexture(texture.get()));
        fenceItem(block, texture);
    }

    protected void fenceGateBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        fenceGateBlock((FenceGateBlock) block.get(), blockTexture(texture.get()));
        blockItem(block);
    }

    protected void wallBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        wallBlock((WallBlock) block.get(), blockTexture(texture.get()));
        wallItem(block, texture);
    }

    protected void doorBlockWithItem(DeferredBlock<?> block) {
        doorBlockWithItem(block, "cutout");
    }

    protected void doorBlockWithItem(DeferredBlock<?> block, String renderType) {
        doorBlockWithRenderType((DoorBlock) block.get(), extend(blockTexture(block.get()), "_bottom"),
                extend(blockTexture(block.get()), "_top"), renderType);
        basicItem(block);
    }

    protected void trapdoorBlockWithItem(DeferredBlock<?> block) {
        trapdoorBlockWithItem(block, "cutout");
    }

    protected void trapdoorBlockWithItem(DeferredBlock<?> block, String renderType) {
        trapdoorBlockWithRenderType((TrapDoorBlock) block.get(), blockTexture(block.get()), true, renderType);
        blockItem(block, "_bottom");
    }

    protected void pressurePlateBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        pressurePlateBlock((PressurePlateBlock) block.get(), blockTexture(texture.get()));
        blockItem(block);
    }

    protected void buttonBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        buttonBlock((ButtonBlock) block.get(), blockTexture(texture.get()));
        buttonItem(block, texture);
    }

    protected void pillarBlockWithItem(DeferredBlock<?> block) {
        pillarBlock(block);
        blockItem(block);
    }

    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        return MiaUtil.id(rl.getNamespace(), rl.getPath() + suffix);
    }

    protected void pillarBlock(DeferredBlock<?> block) {
        axisBlock((RotatedPillarBlock) block.get(), blockTexture(block.get()),
                extend(blockTexture(block.get()), "_top"));
    }

    protected void variantPillarBlockWithItem(DeferredBlock<?> block, int variants) {
        variantAxisBlock(block, variants, null);
        blockItem(block, "0");
    }

    protected void variantPillarBlockWithItem(DeferredBlock<?> block, int variants, int[] weights) {
        variantAxisBlock(block, variants, weights);
        blockItem(block, "0");
    }

    protected void variantAxisBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> variant, int variants) {
        variantAxisBlock(block, variant, variants, null);
        blockItem(block, "0");
    }

    protected void variantAxisBlockWithItem(DeferredBlock<?> block, DeferredBlock<?> variant, int variants, int[] weights) {
        variantAxisBlock(block, variant, variants, weights);
        blockItem(block, "0");
    }

    protected void variantAxisBlock(DeferredBlock<?> block, int variants, int[] weights) {
        variantAxisBlock((RotatedPillarBlock) block.get(), block.get(), extend(blockTexture(block.get()), "_top"), variants, weights);
    }

    protected void variantAxisBlock(DeferredBlock<?> block, DeferredBlock<?> variant, int variants, int[] weights) {
        variantAxisBlock((RotatedPillarBlock) block.get(), variant.get(), null, variants, weights);
    }

    protected void variantAxisBlock(RotatedPillarBlock block, Block side, ResourceLocation end, int variants, int[] weights) {
        ConfiguredModel[] yModels = new ConfiguredModel[variants];
        ConfiguredModel[] zModels = new ConfiguredModel[variants];
        ConfiguredModel[] xModels = new ConfiguredModel[variants];

        for (int i = 0; i < variants; i++) {
            ResourceLocation variantSide = MiaUtil.miaId("block/" + MiaUtil.getBlockPath(side) + i);
            ResourceLocation topTexture = end != null ? end : variantSide;

            ModelFile model = models().cubeColumn(MiaUtil.getBlockPath(block) + i, variantSide, topTexture);
            ModelFile horizontalModel = models().cubeColumnHorizontal(MiaUtil.getBlockPath(block) + "_horizontal" + i, variantSide, topTexture);

            int weight = weights != null ? weights[i] : 1;

            yModels[i] = new ConfiguredModel(model, 0, 0, false, weight);
            zModels[i] = new ConfiguredModel(horizontalModel, 90, 0, false, weight);
            xModels[i] = new ConfiguredModel(horizontalModel, 90, 90, false, weight);
        }

        getVariantBuilder(block)
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                .setModels(yModels)
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
                .setModels(zModels)
                .partialState().with(RotatedPillarBlock.AXIS, Direction.Axis.X)
                .setModels(xModels);
    }

    protected void fenceItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        vanillaBlockItem(block, "fence_inventory")
                .texture("texture", modLoc("block/" + MiaUtil.getBlockPath(texture.get())));
    }

    protected void wallItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        vanillaBlockItem(block, "wall_inventory")
                .texture("wall", modLoc("block/" + MiaUtil.getBlockPath(texture.get())));
    }

    protected void buttonItem(DeferredBlock<?> block, DeferredBlock<?> texture) {
        vanillaBlockItem(block, "button_inventory")
                .texture("texture", modLoc("block/" + MiaUtil.getBlockPath(texture.get())));
    }

    protected void blockItem(DeferredBlock<?> block) {
        baseBlockItem(block, MiaUtil.getBlockPath(block.get()));
    }

    protected void blockItem(DeferredBlock<?> block, String suffix) {
        baseBlockItem(block, MiaUtil.getBlockPath(block.get()) + suffix);
    }

    protected ItemModelBuilder baseBlockItem(DeferredBlock<?> block, String suffix) {
        return itemModels().withExistingParent(MiaUtil.getBlockPath(block.get()), modLoc("block/" + suffix));
    }


    protected ItemModelBuilder vanillaBlockItem(DeferredBlock<?> block, String suffix) {
        return itemModels().withExistingParent(MiaUtil.getBlockPath(block.get()), mcLoc("block/" + suffix));
    }

    protected ItemModelBuilder basicItem(DeferredBlock<?> block) {
        return itemModels().withExistingParent(block.getId().getPath(),
                MiaUtil.id("item/generated")).texture("layer0",
                MiaUtil.miaId("item/" + block.getId().getPath()));
    }

    protected ItemModelBuilder crossItem(DeferredBlock<?> block) {
        return itemModels().withExistingParent(block.getId().getPath(),
                MiaUtil.id("item/generated")).texture("layer0",
                MiaUtil.miaId("block/" + block.getId().getPath()));
    }

    protected void blockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}