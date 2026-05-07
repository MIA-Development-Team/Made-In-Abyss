package com.altnoir.mementoinabyss.impl.registrate;

import com.altnoir.mementoinabyss.content.block.cover_grass.CoverGrassBlock;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class BlockStateGen {
    private static boolean tsbTemplateEmitted = false;

    public static <B extends CoverGrassBlock> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockModelGenerator> coverGrass() {
        return (ctx, prov) -> {
            templateTSBModel(prov);
            var block = ctx.getEntry();
            var model = prov.getBuilder()
                    .parent(prov.modLoc("block/template/cube_tsb"))
                    .texture(TextureSlot.TOP, prov.modBlockTexture("abyss_grass_block_top"))
                    .texture(TextureSlot.SIDE, prov.modBlockTexture(ctx.getName() + "_side"))
                    .texture(TextureSlot.BOTTOM, prov.blockTexture(block.defaultBlock))
                    .build(block);

            var variants = BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(model));
            prov.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, variants));
            prov.registerSimpleItemModel(block, model);
        };
    }

    public static Identifier createTSBModel(RegistrateBlockModelGenerator prov, String blockPath, String suffix, String topSuffix, String sideSuffix, String bottomSuffix) {
        templateTSBModel(prov);
        return prov.getBuilder()
                .parent(prov.modLoc("block/template/cube_tsb"))
                .texture(TextureSlot.TOP, prov.modBlockTexture(blockPath + topSuffix))
                .texture(TextureSlot.SIDE, prov.modBlockTexture(blockPath + sideSuffix))
                .texture(TextureSlot.BOTTOM, prov.modBlockTexture(blockPath + bottomSuffix))
                .build(prov.modLoc("block/" + blockPath + suffix));
    }

    public static Identifier templateTSBModel(RegistrateBlockModelGenerator prov) {
        if (tsbTemplateEmitted) {
            return prov.modLoc("block/template/cube_tsb");
        }
        tsbTemplateEmitted = true;

        Identifier modelId = prov.modLoc("block/template/cube_tsb");
        prov.modelOutput.accept(modelId, () -> {
            JsonObject root = new JsonObject();
            root.addProperty("parent", prov.mcLoc("block/block").toString());

            JsonObject textures = new JsonObject();
            textures.addProperty("particle", "#bottom");
            root.add("textures", textures);

            JsonObject element = new JsonObject();
            element.add("from", vec(0, 0, 0));
            element.add("to", vec(16, 16, 16));

            JsonObject faces = new JsonObject();
            for (Direction dir : Direction.values()) {
                String textureKey = dir == Direction.UP ? "#top" : dir == Direction.DOWN ? "#bottom" : "#side";
                faces.add(dir.getSerializedName(), face(textureKey, dir));
            }
            element.add("faces", faces);

            JsonArray elements = new JsonArray();
            elements.add(element);
            root.add("elements", elements);

            return root;
        });

        return modelId;
    }

    private static JsonObject face(String texture, Direction cullface) {
        JsonObject face = new JsonObject();
        face.addProperty("texture", texture);
        face.addProperty("cullface", cullface.getSerializedName());
        face.add("uv", uv(0, 0, 16, 16));
        return face;
    }

    private static JsonArray vec(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static JsonArray uv(int u1, int v1, int u2, int v2) {
        JsonArray array = new JsonArray();
        array.add(u1);
        array.add(v1);
        array.add(u2);
        array.add(v2);
        return array;
    }
}
