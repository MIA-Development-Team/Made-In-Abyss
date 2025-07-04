package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import com.altnoir.mia.datagen.blockstate.MiaStateProvider;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class MiaBlockStateProvider extends BlockStateProvider {
    public MiaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var modelProvider = new MiaModelProvider();
        var stateProvider = new MiaStateProvider();

        // block models
        modelProvider.abyssGrassBlockModel(this, MiaBlocks.ABYSS_GRASS_BLOCK.get());
        modelProvider.coverGrassBlockModel(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), Blocks.ANDESITE);
        modelProvider.mirroredBlockModel(this, MiaBlocks.ABYSS_ANDESITE.get());
        modelProvider.lampTubeBlockModel(this, MiaBlocks.LAMP_TUBE.get());
        modelProvider.abyssPortalBlockModel(this, MiaBlocks.ABYSS_PORTAL.get());
        modelProvider.abyssSpawnerBlockModel(this, MiaBlocks.ABYSS_SPAWNER.get());
        modelProvider.endlessCupBlockModel(this, MiaBlocks.ENDLESS_CUP.get());

        // block states
        stateProvider.rotationYBlockState(this, MiaBlocks.ABYSS_GRASS_BLOCK.get());
        stateProvider.rotationYBlockState(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
        stateProvider.mirroredBlockState(this, MiaBlocks.ABYSS_ANDESITE.get());
        stateProvider.lampTubeBlockState(this, MiaBlocks.LAMP_TUBE.get());
        stateProvider.baseBlockState(this, MiaBlocks.ABYSS_PORTAL.get());
        stateProvider.abyssSpawnerBlockState(this, MiaBlocks.ABYSS_SPAWNER.get());
        stateProvider.baseBlockState(this, MiaBlocks.ENDLESS_CUP.get());

        // items
        abyssBlockItem(MiaBlocks.ABYSS_GRASS_BLOCK);
        abyssBlockItem(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
        abyssBlockItem(MiaBlocks.ABYSS_ANDESITE);
        abyssBlockItem(MiaBlocks.ABYSS_PORTAL);
        abyssBlockItem(MiaBlocks.LAMP_TUBE);
        abyssBlockItem(MiaBlocks.ABYSS_SPAWNER);


        //vanilla
        blockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE);

        logBlock((RotatedPillarBlock) MiaBlocks.SKYFOG_LOG.get());
        axisBlock((RotatedPillarBlock) MiaBlocks.SKYFOG_WOOD.get(), blockTexture(MiaBlocks.SKYFOG_LOG.get()), blockTexture(MiaBlocks.SKYFOG_LOG.get()));
        logBlock((RotatedPillarBlock) MiaBlocks.STRIPPED_SKYFOG_LOG.get());
        axisBlock((RotatedPillarBlock) MiaBlocks.STRIPPED_SKYFOG_WOOD.get(), blockTexture(MiaBlocks.STRIPPED_SKYFOG_LOG.get()), blockTexture(MiaBlocks.STRIPPED_SKYFOG_LOG.get()));
        blockItem(MiaBlocks.SKYFOG_LOG);
        blockItem(MiaBlocks.SKYFOG_WOOD);
        blockItem(MiaBlocks.STRIPPED_SKYFOG_LOG);
        blockItem(MiaBlocks.STRIPPED_SKYFOG_WOOD);
        blockWithItem(MiaBlocks.SKYFOG_PLANKS);
        sapingAndCrossBlock(MiaBlocks.SKYFOG_SAPLING);
        leavesBlock(MiaBlocks.SKYFOG_LEAVES);

        sapingAndCrossBlock(MiaBlocks.FORTITUDE_FLOWER);
    }

    protected void sapingAndCrossBlock(DeferredBlock<?> block) {
        simpleBlock(block.get(),
                models().cross(MiaUtil.getBlockPath(block.get()), blockTexture(block.get())).renderType("cutout"));
    }

    protected void leavesBlock(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(),
                models().singleTexture(MiaUtil.getBlockPath(block.get()), mcLoc("block/leaves"),
                        "all", blockTexture(block.get())).renderType("cutout"));
    }

    protected void abyssBlockItem(DeferredBlock<?> block) {
        simpleBlockItem(block.get(),
                models().getExistingFile(modLoc("block/" + MiaUtil.getBlockPath(block.get()))));
    }

    protected void blockItem(DeferredBlock<?> block) {
        itemModels().withExistingParent(MiaUtil.getBlockPath(block.get()), modLoc("block/" + MiaUtil.getBlockPath(block.get())));
    }

    protected void blockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
