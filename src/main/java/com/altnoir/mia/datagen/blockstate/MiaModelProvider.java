package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;

public class MiaModelProvider {
    public void abyssGrassBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top"))
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
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_GRASS_BLOCK.get()) + "_top"))
                .texture("bottom", provider.mcLoc("block/" + MiaUtil.getBlockPath(bottom)))
                .texture("particle", provider.mcLoc("block/" + MiaUtil.getBlockPath(bottom)))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) ->
                        faceBuilder
                                .texture(face == Direction.UP ? "#top" :
                                        face == Direction.DOWN ? "#bottom" : "#side")
                                .cullface(face)
                                .uvs(0, 0, 16, 16)
                );
    }
    public void abyssPortalBlockModel(BlockStateProvider provider, Block block) {
        provider.models().getBuilder(MiaUtil.getBlockPath(block))
                .texture("particle", provider.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .texture("portal1", provider.modLoc("block/" + MiaUtil.getBlockPath(block))+ "_dark")
                .texture("portal2", provider.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .element().from(0, 1, 0).to(16, 15, 16)
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#portal1").end()
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#portal2").end();
    }
    public void lampTubeBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.modLoc("block/template_lamp_tube")).renderType("cutout")
                .texture("lamp_tube", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }

    public void mirroredBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/cube_all"))
                .texture("all", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_mirrored", provider.mcLoc("block/cube_mirrored_all"))
                .texture("all", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }
}