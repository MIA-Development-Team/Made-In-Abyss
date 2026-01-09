package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class MiaModelProvider {
    public void hopperFarmBlockModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.modLoc("block/template/hopper_farmland"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())))
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"));

        p.models().withExistingParent(MiaUtil.getBlockPath(block) + "_moist", p.modLoc("block/template/hopper_farmland"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_moist"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())))
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"));
    }

    public void ropeBlockModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.mcLoc("block/block"))
                .renderType("cutout")
                .texture("rope", p.modLoc("block/rope"))
                .texture("particle", p.modLoc("block/rope"))
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

    public void abyssPortalBlockModel(BlockStateProvider p, Block block) {
        p.models().getBuilder(MiaUtil.getBlockPath(block)).renderType("translucent")
                .texture("particle", p.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .texture("portal1", p.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .texture("portal2", p.modLoc("block/" + MiaUtil.getBlockPath(block)))
                .element().from(0, 1, 0).to(16, 15, 16)
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#portal1").end()
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#portal2").end();
    }

    public ModelFile abyssPortalFrameModel(BlockStateProvider p, Block block, boolean isOpen) {
        String suffix = isOpen ? "_open" : "";
        String blockPath = MiaUtil.getBlockPath(block);

        return p.models().withExistingParent(blockPath + suffix, p.modLoc("block/template/cube_tsb"))
                .texture("top", p.modLoc("block/" + blockPath + "_top" + suffix))
                .texture("side", p.modLoc("block/" + blockPath + "_side"))
                .texture("bottom", p.modLoc("block/" + blockPath + "_bottom"));
    }

    public void artifactSmithingTableBlockModel(BlockStateProvider p, Block block, Block bottom) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.mcLoc("block/block"))
                .texture("ns", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_1"))
                .texture("ew", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_2"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top"))
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(bottom)))
                .texture("particle", p.modLoc("block/" + MiaUtil.getBlockPath(bottom)))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) -> faceBuilder
                        .texture(switch (face) {
                            case UP -> "#top";
                            case DOWN -> "#bottom";
                            case NORTH, SOUTH -> "#ns";
                            case EAST, WEST -> "#ew";
                        })
                        .cullface(face)
                        .uvs(0, 0, 16, 16));
    }

    public void lampTubeBlockModel(BlockStateProvider p, Block block, Block particle) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.modLoc("block/template/lamp_tube"))
                .renderType("cutout")
                .texture("lamp_tube", p.modLoc("block/tube/" + MiaUtil.getBlockPath(block)))
                .texture("particle", MiaUtil.getBlockLoc("block/", particle));
    }

    public void pedestalBlockModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.modLoc("block/template/pedestal"))
                .renderType("cutout")
                .texture("pedestal", p.modLoc("block/tube/" + MiaUtil.getBlockPath(block)))
                .texture("particle", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())));
    }

    public void endlessCupBlockModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.modLoc("block/template/endless_cup"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side"))
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"));
    }

    public void abyssSpawnerBlockModel(BlockStateProvider p, Block block) {
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block), p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_inactive"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_inactive"));
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block) + "_active",
                        p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_active"));
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block) + "_active_ominous",
                        p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active_ominous"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_active_ominous"));
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block) + "_ejecting_reward",
                        p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_ejecting_reward"));
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block) + "_ejecting_reward_ominous",
                        p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_active_ominous"))
                .texture("top",
                        p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_ejecting_reward_ominous"));
        p.models()
                .withExistingParent(MiaUtil.getBlockPath(block) + "_ominous",
                        p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("bottom", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_bottom"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_side_inactive_ominous"))
                .texture("top", p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_top_inactive_ominous"));
    }

    public void crossModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.mcLoc("block/cross")).renderType("cutout")
                .texture("cross", p.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }

    public void mirroredBlockModel(BlockStateProvider p, Block block) {
        p.models().withExistingParent(MiaUtil.getBlockPath(block), p.mcLoc("block/cube_all"))
                .texture("all", p.modLoc("block/" + MiaUtil.getBlockPath(block)));
        p.models().withExistingParent(MiaUtil.getBlockPath(block) + "_mirrored", p.mcLoc("block/cube_mirrored_all"))
                .texture("all", p.modLoc("block/" + MiaUtil.getBlockPath(block)));
    }

    // Template models
    public void templateTSBModel(BlockStateProvider p) {
        p.models().withExistingParent("block/template/cube_tsb", p.mcLoc("block/block"))
                .texture("particle", "#bottom")
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) -> faceBuilder
                        .texture(face == Direction.UP ? "#top" : face == Direction.DOWN ? "#bottom" : "#side")
                        .cullface(face)
                        .uvs(0, 0, 16, 16)).end();
    }

    public void templateHopperFarmland(BlockStateProvider p) {
        p.models().withExistingParent("block/template/hopper_farmland", p.mcLoc("block/block"))
                .texture("particle", p.mcLoc("block/dirt"))
                .element().from(0, 0, 0).to(16, 15, 16)
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(Direction.DOWN).end()
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(Direction.NORTH).uvs(0, 1, 16, 16).texture("#side").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).uvs(0, 1, 16, 16).texture("#side").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).uvs(0, 1, 16, 16).texture("#side").cullface(Direction.WEST).end()
                .face(Direction.EAST).uvs(0, 1, 16, 16).texture("#side").cullface(Direction.EAST).end();
    }
}