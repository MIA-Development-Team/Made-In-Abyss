package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.datagen.blockstate.MIAStateProvider;
import com.altnoir.mia.datagen.blockstate.MIAModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class MIABlockStateProvider extends BlockStateProvider {
    public MIABlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MIAModelProvider modelProvider = new MIAModelProvider();
        MIAStateProvider stateProvider = new MIAStateProvider();

        // block models
        modelProvider.registerAbyssGrassBlockModel(this, MIABlocks.ABYSS_GRASS_BLOCK.get());

        // block states
        stateProvider.registerAbyssGrassBlockState(this, MIABlocks.ABYSS_GRASS_BLOCK.get());

        crossBlock(MIABlocks.FORTITUDE_FLOWER);
        simpleBlockItem(MIABlocks.ABYSS_GRASS_BLOCK.get(), models().getExistingFile(modLoc("block/abyss_grass_block")));
        blockWithItem(MIABlocks.ABYSS_ANDESITE);
        blockWithItem(MIABlocks.ABYSS_COBBLED_ANDESITE);
    }

    protected void crossBlock(DeferredBlock<?> block) {
        simpleBlock(block.get(), models()
                .cross(BuiltInRegistries.BLOCK.getKey(block.get()).getPath(), blockTexture(block.get())).renderType("cutout"));
    }
    protected void blockItem(DeferredBlock<?> block) {
        itemModels().withExistingParent(BuiltInRegistries.BLOCK.getKey(block.get()).getPath(), modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath()));
    }
    protected void blockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
