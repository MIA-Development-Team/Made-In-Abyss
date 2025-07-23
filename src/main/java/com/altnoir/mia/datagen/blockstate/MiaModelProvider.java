package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;

public class MiaModelProvider {
    public void coverGrassBlockModel(BlockStateProvider provider, Block block, Block bottom) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/block"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side"))
                .texture("top", provider.modLoc("block/" + "abyss_grass_block_top"))
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

    public void ropeBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/block")).renderType("cutout")
                .texture("rope", provider.modLoc("block/rope"))
                .texture("particle", provider.modLoc("block/rope"))
                .element().from(6, 0, 6).to(10, 16, 10)
                .allFaces((face, faceBuilder) -> {
                    faceBuilder.texture("#rope");
                    if (face == Direction.UP || face == Direction.DOWN) {
                        faceBuilder.uvs(12, 4, 8, 0).cullface(face);
                    } else {
                        faceBuilder.uvs(0, 0, 4, 16);
                    }
                })
                .end()
                .element().from(5.75F, 0.0F, 5.75F).to(10.25F, 16.0F, 10.25F)
                .face(Direction.NORTH).uvs(4, 0, 8, 16).texture("#rope").end()
                .face(Direction.SOUTH).uvs(4, 0, 8, 16).texture("#rope").end()
                .face(Direction.WEST).uvs(4, 0, 8, 16).texture("#rope").end()
                .face(Direction.EAST).uvs(4, 0, 8, 16).texture("#rope").end();
    }

    public void abyssPortalBlockModel(BlockStateProvider provider, Block block) {
        provider.models().getBuilder(MiaUtil.getBlockPath(block))
                .texture("particle", provider.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .texture("portal1", provider.modLoc("block/" + MiaUtil.getBlockPath(block)) + "_dark")
                .texture("portal2", provider.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .element().from(0, 1, 0).to(16, 15, 16)
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#portal1").end()
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#portal2").end();
    }

    public void lampTubeBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.modLoc("block/template_lamp_tube")).renderType("cutout")
                .texture("lamp_tube", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }

    public void endlessCupBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.modLoc("template_endless_cup"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side"))
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"));
    }

    public void abyssSpawnerBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_inactive"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_inactive"));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_active", provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_active"));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_active_ominous", provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active_ominous"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_active_ominous"));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_ejecting_reward", provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_ejecting_reward"));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_ejecting_reward_ominous", provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active_ominous"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_ejecting_reward_ominous"));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_ominous", provider.mcLoc("block/cube_bottom_top_inner_faces")).renderType("cutout")
                .texture("bottom", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_inactive_ominous"))
                .texture("top", provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_inactive_ominous"));
    }

    public void mirroredBlockModel(BlockStateProvider provider, Block block) {
        provider.models().withExistingParent(MiaUtil.getBlockPath(block), provider.mcLoc("block/cube_all"))
                .texture("all", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
        provider.models().withExistingParent(MiaUtil.getBlockPath(block) + "_mirrored", provider.mcLoc("block/cube_mirrored_all"))
                .texture("all", provider.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }
}