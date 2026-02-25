package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class MiaModelProvider {
    public void hopperFarmBlockModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.modLoc("block/template/hopper_farmland"))
                .texture("top", p.modLoc("block/" + blockPath))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())))
                .texture("bottom", p.modLoc("block/" + blockPath + "_bottom"));

        p.models().withExistingParent(blockPath + "_moist", p.modLoc("block/template/hopper_farmland"))
                .texture("top", p.modLoc("block/" + blockPath + "_moist"))
                .texture("side", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())))
                .texture("bottom", p.modLoc("block/" + blockPath + "_bottom"));
    }

    public void ropeBlockModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.mcLoc("block/block"))
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
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().getBuilder(blockPath).renderType("translucent")
                .texture("particle", p.modLoc("block/" + blockPath))
                .texture("portal1", p.modLoc("block/" + blockPath))
                .texture("portal2", p.modLoc("block/" + blockPath))
                .element().from(0, 1, 0).to(16, 15, 16)
                .face(Direction.UP).uvs(0, 0, 16, 16).texture("#portal1").end()
                .face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#portal2").end();
    }

    public ModelFile abyssPortalFrameModel(BlockStateProvider p, Block block, boolean isOpen) {
        String suffix = isOpen ? "_open" : "";
        String blockPath = MiaUtil.getBlockPath(block);

        return createTSBModel(p, blockPath, suffix, "_top" + suffix, "_side", "_bottom");
    }

    public void artifactSmithingTableBlockModel(BlockStateProvider p, Block block, Block bottom) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.mcLoc("block/block"))
                .texture("ns", p.modLoc("block/" + blockPath + "_1"))
                .texture("ew", p.modLoc("block/" + blockPath + "_2"))
                .texture("top", p.modLoc("block/" + blockPath + "_top"))
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

    public ResourceLocation lampTubeBlockModel(BlockStateProvider p, Block block, Block particle) {
        String blockPath = MiaUtil.getBlockPath(block);
        ResourceLocation modelPath = p.modLoc("block/" + blockPath);

        p.models().withExistingParent(blockPath, p.modLoc("block/template/lamp_tube")).renderType("cutout")
                .texture("lamp_tube", p.modLoc("block/tube/" + blockPath))
                .texture("particle", MiaUtil.getBlockLoc("block/", particle));

        return modelPath;
    }

    public void pedestalBlockModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.modLoc("block/template/pedestal"))
                .renderType("cutout")
                .texture("pedestal", p.modLoc("block/tube/" + blockPath))
                .texture("particle", p.modLoc("block/" + MiaUtil.getBlockPath(MiaBlocks.ABYSS_ANDESITE.get())));
    }

    public void columnBlockModel(BlockStateProvider p, Block block, Block pillarBlock, Block decBlock) {

        String blockPath = MiaUtil.getBlockPath(block);
        String pillarPath = MiaUtil.getBlockPath(pillarBlock);
        String decPath = MiaUtil.getBlockPath(decBlock);

        // 创建 none 模型
        p.models().withExistingParent(blockPath, p.modLoc("block/template/column"))
                .texture("top", p.modLoc("block/" + pillarPath + "_top"))
                .texture("side", p.modLoc("block/" + pillarPath))
                .texture("dec", p.modLoc("block/" + decPath));
        // 创建 top 模型
        p.models().withExistingParent(blockPath + "_top", p.modLoc("block/template/column_top"))
                .texture("top", p.modLoc("block/" + pillarPath + "_top"))
                .texture("side", p.modLoc("block/" + pillarPath))
                .texture("dec", p.modLoc("block/" + decPath));
        // 创建 middle 模型
        p.models().withExistingParent(blockPath + "_middle", p.modLoc("block/template/column_middle"))
                .texture("side", p.modLoc("block/" + pillarPath));
        // 创建 bottom 模型
        p.models().withExistingParent(blockPath + "_bottom", p.modLoc("block/template/column_bottom"))
                .texture("top", p.modLoc("block/" + pillarPath + "_top"))
                .texture("side", p.modLoc("block/" + pillarPath))
                .texture("dec", p.modLoc("block/" + decPath));
    }

    public void templateAllBlockModel(BlockStateProvider p, Block block, String templateName) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.modLoc("block/template/" + templateName)).renderType("cutout")
                .texture("all", p.modLoc("block/" + blockPath))
                .texture("particle", p.modLoc("block/" + blockPath + "_particle"));
    }

    public void abyssSpawnerBlockModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);

        createCBTIFModel(p, blockPath, "", "_top_inactive", "_side_inactive", "_bottom");
        createCBTIFModel(p, blockPath, "_active", "_top_active", "_side_active", "_bottom_active");
        createCBTIFModel(p, blockPath, "_ejecting_reward", "_top_ejecting_reward", "_side_active", "_bottom_active");
    }

    public ResourceLocation crossModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.mcLoc("block/cross")).renderType("cutout")
                .texture("cross", p.modLoc("block/" + blockPath));
        return p.modLoc("block/" + blockPath);
    }

    public ResourceLocation crossModel(BlockStateProvider p, Block block, String suffix) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath + suffix, p.mcLoc("block/cross")).renderType("cutout")
                .texture("cross", p.modLoc("block/" + blockPath + suffix));
        return p.modLoc("block/" + blockPath + suffix);
    }

    public ResourceLocation[] flowerbedModels(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        ResourceLocation[] locations = new ResourceLocation[4];
        for (int i = 1; i <= 4; i++) {
            locations[i - 1] = p.modLoc("block/" + blockPath + "_" + i);
            p.models().withExistingParent(blockPath + "_" + i, p.mcLoc("block/flowerbed_" + i))
                    .renderType("cutout")
                    .texture("flowerbed", p.modLoc("block/" + blockPath))
                    .texture("stem", p.modLoc("block/" + blockPath + "_stem"));
        }
        return locations;
    }

    public void mirroredBlockModel(BlockStateProvider p, Block block) {
        String blockPath = MiaUtil.getBlockPath(block);
        p.models().withExistingParent(blockPath, p.mcLoc("block/cube_all"))
                .texture("all", p.modLoc("block/" + blockPath));
        p.models().withExistingParent(blockPath + "_mirrored", p.mcLoc("block/cube_mirrored_all"))
                .texture("all", p.modLoc("block/" + blockPath));
    }

    // Template models
    private ModelFile createCBTIFModel(BlockStateProvider p, String blockPath, String suffix, String topSuffix, String sideSuffix, String bottomSuffix) {
        return p.models()
                .withExistingParent(blockPath + suffix, p.mcLoc("block/cube_bottom_top_inner_faces"))
                .renderType("cutout")
                .texture("top", p.modLoc("block/" + blockPath + topSuffix))
                .texture("side", p.modLoc("block/" + blockPath + sideSuffix))
                .texture("bottom", p.modLoc("block/" + blockPath + bottomSuffix));
    }

    public ModelFile createTSBModel(BlockStateProvider p, String blockPath, String suffix, String topSuffix, String sideSuffix, String bottomSuffix) {
        return p.models()
                .withExistingParent(blockPath + suffix, p.modLoc("block/template/cube_tsb"))
                .texture("top", p.modLoc("block/" + blockPath + topSuffix))
                .texture("side", p.modLoc("block/" + blockPath + sideSuffix))
                .texture("bottom", p.modLoc("block/" + blockPath + bottomSuffix));
    }

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