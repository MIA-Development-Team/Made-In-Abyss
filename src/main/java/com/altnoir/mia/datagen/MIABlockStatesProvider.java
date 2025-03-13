package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.core.registries.BlocksRegister;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class MIABlockStatesProvider extends BlockStateProvider {
    public MIABlockStatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    public void registerStatesAndModels() {
        blockWithItem(BlocksRegister.EXAMPLE_BLOCK);
        blockWithItem(BlocksRegister.EXAMPLE_PORTAL_BLOCK);
        blockWithItem(BlocksRegister.CHLOROPHTRE_ORE);

        simpleGrassBlock(BlocksRegister.COVERGRASS_ANDESITE,
                modLoc("block/grass_block_top_d1"),
                mcLoc("block/andesite"),
                modLoc("block/covergrass_andesite"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_DEEPSLATE,
                modLoc("block/grass_block_top_d1"),
                mcLoc("block/deepslate"),
                modLoc("block/covergrass_deepslate"));

        simpleGrassBlock(BlocksRegister.COVERGRASS_COBBLESTONE,
                modLoc("block/grass_block_top_d1"),
                mcLoc("block/cobblestone"),
                modLoc("block/covergrass_cobblestone"));

        grassBlockWithUV(BlocksRegister.GRASS_BLOCK_D1,
                modLoc("block/grass_block_top_d1"),
                mcLoc("block/dirt"),
                modLoc("block/grass_block_side_d1"),
                mcLoc("block/dirt"));
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
}
