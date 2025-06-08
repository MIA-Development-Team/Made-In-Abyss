package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.block.MIABlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;

public class MIAModelProvider {
    public void abyssGrassBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + "_side"))
                .texture("top", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + "_top"))
                .texture("bottom", provider.mcLoc("block/dirt"))
                .texture("particle", provider.mcLoc("block/dirt"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) ->
                        faceBuilder
                                .texture(face == Direction.UP ? "#top" :
                                        face == Direction.DOWN ? "#bottom" : "#side")
                                .cullface(face)
                                .uvs(0, 0, 16, 16)
                );
    }
    public void coverGrassBlockModel(BlockStateProvider provider, Block block, Block bottom) {
        provider.models().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + "_side"))
                .texture("top", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(MIABlocks.ABYSS_GRASS_BLOCK.get()).getPath() + "_top"))
                .texture("bottom", provider.mcLoc("block/" + BuiltInRegistries.BLOCK.getKey(bottom).getPath()))
                .texture("particle", provider.mcLoc("block/" + BuiltInRegistries.BLOCK.getKey(bottom).getPath()))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) ->
                        faceBuilder
                                .texture(face == Direction.UP ? "#top" :
                                        face == Direction.DOWN ? "#bottom" : "#side")
                                .cullface(face)
                                .uvs(0, 0, 16, 16)
                );
    }
    public void mirroredBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), provider.mcLoc("block/cube_all"))
                .texture("all", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath()));
        provider.models().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath() + "_mirrored", provider.mcLoc("block/cube_mirrored_all"))
                .texture("all", provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath()));
    }
}