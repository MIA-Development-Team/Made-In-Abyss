package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.MIABlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MIABlockStateProvider extends BlockStateProvider {
    public static final String PARTICLE = "particle";
    public static final String TEXTURE = "texture";
    public static final String SIDE = "side";
    public static final String TOP = "top";
    public static final String BOTTOM = "bottom";
    public static final String TEXTURE3 = "#texture";
    public static final String SIDE_3 = "#side";
    public static final String TOP_3 = "#top";
    public static final String BOTTOM_3 = "#bottom";
    public MIABlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MIA.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //block models
        models().withExistingParent("abyss_grass_block", mcLoc("block/block"))
                .texture(SIDE, modLoc("block/abyss_grass_block_side"))
                .texture(TOP, modLoc("block/abyss_grass_block_top"))
                .texture(BOTTOM, mcLoc("block/dirt"))
                .texture(PARTICLE, mcLoc("block/dirt"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((face, faceBuilder) ->
                        faceBuilder
                                .texture(face == Direction.UP ? TOP_3 :
                                        face == Direction.DOWN ? BOTTOM_3 : SIDE_3)
                                .uvs(0, 0, 16, 16)
                );

        //block states
        getVariantBuilder(MIABlocks.ABYSS_GRASS_BLOCK.get())
                .partialState().addModels(
                        new ConfiguredModel(models().getExistingFile(modLoc("block/abyss_grass_block")), 0, 0, false),
                        new ConfiguredModel(models().getExistingFile(modLoc("block/abyss_grass_block")), 0, 90, false),
                        new ConfiguredModel(models().getExistingFile(modLoc("block/abyss_grass_block")), 0, 180, false),
                        new ConfiguredModel(models().getExistingFile(modLoc("block/abyss_grass_block")), 0, 270, false)
                );

        simpleBlockItem(MIABlocks.ABYSS_GRASS_BLOCK.get(), models().getExistingFile(modLoc("block/abyss_grass_block")));
    }
}
