package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaPort;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;

public class MiaModelProvider {
    public void abyssGrassBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaPort.getBlockPath(block), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + MiaPort.getBlockPath(block) + "_side"))
                .texture("top", provider.modLoc("block/" + MiaPort.getBlockPath(block) + "_top"))
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
        provider.models().withExistingParent(MiaPort.getBlockPath(block), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + MiaPort.getBlockPath(block) + "_side"))
                .texture("top", provider.modLoc("block/" + MiaPort.getBlockPath(MiaBlocks.ABYSS_GRASS_BLOCK.get()) + "_top"))
                .texture("bottom", provider.mcLoc("block/" + MiaPort.getBlockPath(bottom)))
                .texture("particle", provider.mcLoc("block/" + MiaPort.getBlockPath(bottom)))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) ->
                        faceBuilder
                                .texture(face == Direction.UP ? "#top" :
                                        face == Direction.DOWN ? "#bottom" : "#side")
                                .cullface(face)
                                .uvs(0, 0, 16, 16)
                );
    }
    public void lampTubeBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaPort.getBlockPath(block), provider.modLoc("block/template_lamp_tube")).renderType("cutout")
                .texture("lamp_tube", provider.modLoc("block/" + MiaPort.getBlockPath(block)));
    }

    public void mirroredBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaPort.getBlockPath(block), provider.mcLoc("block/cube_all"))
                .texture("all", provider.modLoc("block/" + MiaPort.getBlockPath(block)));
        provider.models().withExistingParent(MiaPort.getBlockPath(block) + "_mirrored", provider.mcLoc("block/cube_mirrored_all"))
                .texture("all", provider.modLoc("block/" + MiaPort.getBlockPath(block)));
    }
}