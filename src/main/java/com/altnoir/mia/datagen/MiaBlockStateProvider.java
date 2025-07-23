package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import com.altnoir.mia.datagen.blockstate.MiaStateProvider;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Objects;

public class MiaBlockStateProvider extends BlockStateProvider {
    public MiaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var modelProvider = new MiaModelProvider();
        var stateProvider = new MiaStateProvider();

        // block models
        modelProvider.coverGrassBlockModel(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), Blocks.ANDESITE);
        modelProvider.mirroredBlockModel(this, MiaBlocks.ABYSS_ANDESITE.get());
        modelProvider.lampTubeBlockModel(this, MiaBlocks.LAMP_TUBE.get());
        modelProvider.abyssPortalBlockModel(this, MiaBlocks.ABYSS_PORTAL.get());
        modelProvider.abyssSpawnerBlockModel(this, MiaBlocks.ABYSS_SPAWNER.get());
        modelProvider.endlessCupBlockModel(this, MiaBlocks.ENDLESS_CUP.get());
        modelProvider.ropeBlockModel(this, MiaBlocks.ROPE.get());

        // block states
        stateProvider.rotationYBlockState(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
        stateProvider.mirroredBlockState(this, MiaBlocks.ABYSS_ANDESITE.get());
        stateProvider.lampTubeBlockState(this, MiaBlocks.LAMP_TUBE.get());
        stateProvider.baseBlockState(this, MiaBlocks.ABYSS_PORTAL.get());
        stateProvider.abyssSpawnerBlockState(this, MiaBlocks.ABYSS_SPAWNER.get());
        stateProvider.baseBlockState(this, MiaBlocks.ENDLESS_CUP.get());
        stateProvider.baseBlockState(this, MiaBlocks.ROPE.get());

        // items
        blockItem(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
        blockItem(MiaBlocks.ABYSS_ANDESITE);
        blockItem(MiaBlocks.ABYSS_PORTAL);
        blockItem(MiaBlocks.LAMP_TUBE);
        blockItem(MiaBlocks.ABYSS_SPAWNER);
        basicItem(MiaBlocks.ENDLESS_CUP);
        basicItem(MiaBlocks.ROPE);


        // base
        stairsBlockWithItem(MiaBlocks.ABYSS_ANDESITE_STAIRS, MiaBlocks.ABYSS_ANDESITE);
        slabBlockWithItem(MiaBlocks.ABYSS_ANDESITE_SLAB, MiaBlocks.ABYSS_ANDESITE);
        wallBlockWithItem(MiaBlocks.ABYSS_ANDESITE_WALL, MiaBlocks.ABYSS_ANDESITE);

        blockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE);
        stairsBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS, MiaBlocks.ABYSS_COBBLED_ANDESITE);
        slabBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB, MiaBlocks.ABYSS_COBBLED_ANDESITE);
        wallBlockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL, MiaBlocks.ABYSS_COBBLED_ANDESITE);

        blockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        stairsBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        slabBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
        wallBlockWithItem(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL, MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);

        blockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE);
        stairsBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        slabBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        wallBlockWithItem(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL, MiaBlocks.POLISHED_ABYSS_ANDESITE);
        pillarBlockWithItem(MiaBlocks.ABYSS_ANDESITE_PILLAR);

        blockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS);
        blockWithItem(MiaBlocks.CHISLED_ABYSS_ANDESITE);
        stairsBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        slabBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        wallBlockWithItem(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL, MiaBlocks.ABYSS_ANDESITE_BRICKS);
        blockWithItem(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS);

        blockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        stairsBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        slabBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
        wallBlockWithItem(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL, MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);

        logBlockWithItem(MiaBlocks.SKYFOG_LOG);
        woodBlockWithItem(MiaBlocks.SKYFOG_WOOD, MiaBlocks.SKYFOG_LOG);
        logBlockWithItem(MiaBlocks.STRIPPED_SKYFOG_LOG);
        woodBlockWithItem(MiaBlocks.STRIPPED_SKYFOG_WOOD, MiaBlocks.STRIPPED_SKYFOG_LOG);
        blockWithItem(MiaBlocks.SKYFOG_PLANKS);
        stairsBlockWithItem(MiaBlocks.SKYFOG_STARIS, MiaBlocks.SKYFOG_PLANKS);
        slabBlockWithItem(MiaBlocks.SKYFOG_SLAB, MiaBlocks.SKYFOG_PLANKS);
        fenceBlockWithItem(MiaBlocks.SKYFOG_FENCE, MiaBlocks.SKYFOG_PLANKS);
        doorBlockWithItem(MiaBlocks.SKYFOG_DOOR);
        trapdoorBlockWithItem(MiaBlocks.SKYFOG_TRAPDOOR);
        fenceGateBlockWithItem(MiaBlocks.SKYFOG_FENCE_GATE, MiaBlocks.SKYFOG_PLANKS);
        sapingAndCrossBlock(MiaBlocks.SKYFOG_SAPLING);
        leavesBlock(MiaBlocks.SKYFOG_LEAVES);
        leavesBlock(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS);

        sapingAndCrossBlock(MiaBlocks.FORTITUDE_FLOWER);

        // 压力板和按钮单独分列
        pressurePlateBlockWithItem(MiaBlocks.SKYFOG_PRESSURE_PLATE, MiaBlocks.SKYFOG_PLANKS);
        buttonBlockWithItem(MiaBlocks.SKYFOG_BUTTON, MiaBlocks.SKYFOG_PLANKS);
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
        doorBlockWithRenderType((DoorBlock) block.get(), extend(blockTexture(block.get()), "_bottom"), extend(blockTexture(block.get()), "_top"), renderType);
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
        axisBlock((RotatedPillarBlock) block.get(), blockTexture(block.get()), extend(blockTexture(block.get()), "_top"));
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
