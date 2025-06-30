package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import com.altnoir.mia.datagen.blockstate.MiaStateProvider;
import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
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

        // block states
        stateProvider.rotationYBlockState(this, MiaBlocks.ABYSS_GRASS_BLOCK.get());
        stateProvider.rotationYBlockState(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
        stateProvider.mirroredBlockState(this, MiaBlocks.ABYSS_ANDESITE.get());
        stateProvider.lampTubeBlockState(this, MiaBlocks.LAMP_TUBE.get());
        stateProvider.baseBlockState(this, MiaBlocks.ABYSS_PORTAL.get());
        stateProvider.abyssSpawnerBlockState(this, MiaBlocks.ABYSS_SPAWNER.get());

        // items
        abyssBlockItem(MiaBlocks.ABYSS_GRASS_BLOCK);
        abyssBlockItem(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
        abyssBlockItem(MiaBlocks.ABYSS_ANDESITE);
        abyssBlockItem(MiaBlocks.ABYSS_PORTAL);
        abyssBlockItem(MiaBlocks.LAMP_TUBE);
        abyssBlockItem(MiaBlocks.ABYSS_SPAWNER);


        blockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE);

        logBlock((RotatedPillarBlock) MiaBlocks.SKYFOGWOOD_LOG.get());
        axisBlock((RotatedPillarBlock) MiaBlocks.SKYFOGWOOD.get(), blockTexture(MiaBlocks.SKYFOGWOOD_LOG.get()), blockTexture(MiaBlocks.SKYFOGWOOD_LOG.get()));
        logBlock((RotatedPillarBlock) MiaBlocks.STRIPPED_SKYFOGWOOD_LOG.get());
        axisBlock((RotatedPillarBlock) MiaBlocks.STRIPPED_SKYFOGWOOD.get(), blockTexture(MiaBlocks.STRIPPED_SKYFOGWOOD_LOG.get()), blockTexture(MiaBlocks.STRIPPED_SKYFOGWOOD_LOG.get()));
        blockItem(MiaBlocks.SKYFOGWOOD_LOG);
        blockItem(MiaBlocks.SKYFOGWOOD);
        blockItem(MiaBlocks.STRIPPED_SKYFOGWOOD_LOG);
        blockItem(MiaBlocks.STRIPPED_SKYFOGWOOD);
        blockWithItem(MiaBlocks.SKYFOGWOO_PLANKS);

        crossBlock(MiaBlocks.FORTITUDE_FLOWER);
    }

    protected void crossBlock(DeferredBlock<?> block) {
        simpleBlock(block.get(), models()
                .cross(BuiltInRegistries.BLOCK.getKey(block.get()).getPath(), blockTexture(block.get())).renderType("cutout"));
    }

    protected void abyssBlockItem(DeferredBlock<?> block) {
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath())));
    }

    protected void blockItem(DeferredBlock<?> block) {
        itemModels().withExistingParent(BuiltInRegistries.BLOCK.getKey(block.get()).getPath(), modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath()));
    }

    protected void blockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
