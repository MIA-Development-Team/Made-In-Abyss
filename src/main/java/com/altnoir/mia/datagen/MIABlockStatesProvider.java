package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.content.block.BlocksRegister;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class MIABlockStatesProvider extends BlockStateProvider {
    public MIABlockStatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MODID, exFileHelper);
    }

    @Override
    public void registerStatesAndModels() {
        blockWithItem(BlocksRegister.CHLOROPHTRE_ORE);

        grassBlockWithUV(BlocksRegister.ABYSS_GRASS_BLOCK,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/dirt"),
                modLoc("block/abyss_grass_block_side"),
                mcLoc("block/dirt"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_COBBLESTONE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/cobblestone"),
                modLoc("block/covergrass_cobblestone"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_STONE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/stone"),
                modLoc("block/covergrass_stone"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_DEEPSLATE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/deepslate"),
                modLoc("block/covergrass_deepslate"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_GRANITE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/granite"),
                modLoc("block/covergrass_granite"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_DIORITE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/diorite"),
                modLoc("block/covergrass_diorite"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_ANDESITE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/andesite"),
                modLoc("block/covergrass_andesite"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_CALCITE,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/calcite"),
                modLoc("block/covergrass_calcite"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_TUFF,
                modLoc("block/abyss_grass_block_top"),
                mcLoc("block/tuff"),
                modLoc("block/covergrass_tuff"));

        suspiciousBlock(BlocksRegister.SUSPICOUS_ANDESITE);
    }

    private void blockWithItem(RegistryObject<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    private void simpleGrassBlock(RegistryObject<Block> block, ResourceLocation top, ResourceLocation bottom, ResourceLocation side) {
        var model = models().cubeBottomTop(
                block.getId().getPath(),
                side,
                bottom,
                top
        );

        simpleBlockWithItem(block.get(), model);
    }

    private void grassBlockWithUV(RegistryObject<Block> block, ResourceLocation top, ResourceLocation bottom, ResourceLocation side, ResourceLocation particle) {
        var model = models().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("block/block"))
                .texture("particle", particle)
                .texture("bottom", bottom)
                .texture("top", top)
                .texture("side", side);

        model.element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .allFaces((direction, faceBuilder) -> {
                    faceBuilder
                            .texture(direction == Direction.UP ? "#top" :
                                    direction == Direction.DOWN ? "#bottom" : "#side")
                            .uvs(0, 0, 16, 16)
                            .cullface(direction);
                });

        simpleBlockWithItem(block.get(), model);
    }

    private void suspiciousBlock(RegistryObject<Block> block) {
        var basePath = block.getId().getPath();

        var models = new ModelFile[4];
        for (int i = 0; i < 4; i++) {
            models[i] = models().cubeAll(
                    basePath + "_" + i,
                    modLoc("block/" + basePath + "_" + i)
            );
        }

        getVariantBuilder(block.get())
                .forAllStates(state -> {
                    int dusted = state.getValue(BlockStateProperties.DUSTED);
                    return ConfiguredModel.builder()
                            .modelFile(models[dusted])
                            .build();
                });

        simpleBlockItem(block.get(), models[0]);
    }
}
