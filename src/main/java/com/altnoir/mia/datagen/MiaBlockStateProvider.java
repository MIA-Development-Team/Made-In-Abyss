package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.datagen.blockstate.MiaStateProvider;
import com.altnoir.mia.datagen.blockstate.MiaModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class MiaBlockStateProvider extends BlockStateProvider {
    public MiaBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MiaModelProvider modelProvider = new MiaModelProvider();
        MiaStateProvider stateProvider = new MiaStateProvider();

        // block models
        modelProvider.abyssGrassBlockModel(this, MiaBlocks.ABYSS_GRASS_BLOCK.get());
        modelProvider.coverGrassBlockModel(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), Blocks.ANDESITE);
        modelProvider.mirroredBlockModel(this, MiaBlocks.ABYSS_ANDESITE.get());
        modelProvider.lampTubeBlockModel(this, MiaBlocks.LAMP_TUBE.get());

        // block states
        stateProvider.rotationYBlockState(this, MiaBlocks.ABYSS_GRASS_BLOCK.get());
        stateProvider.rotationYBlockState(this, MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get());
        stateProvider.mirroredBlockState(this, MiaBlocks.ABYSS_ANDESITE.get());
        stateProvider.lampTubeBlockState(this, MiaBlocks.LAMP_TUBE.get());



        abyssBlockItem(MiaBlocks.ABYSS_GRASS_BLOCK);
        abyssBlockItem(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
        abyssBlockItem(MiaBlocks.ABYSS_ANDESITE);
        blockWithItem(MiaBlocks.ABYSS_COBBLED_ANDESITE);

        crossBlock(MiaBlocks.FORTITUDE_FLOWER);
        abyssBlockItem(MiaBlocks.LAMP_TUBE);
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
