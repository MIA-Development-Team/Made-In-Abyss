package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.util.MiaPort;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

public class MiaStateProvider {
    public void rotationYBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 90, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 180, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 270, false)
                );
    }
    public void mirroredBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block) + "_mirrored")), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))), 0, 180, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block) + "_mirrored")), 0, 180, false)
                );
    }
    public void lampTubeBlockState(BlockStateProvider provider, Block block) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(block);
        DirectionProperty facing = BlockStateProperties.FACING;

        builder.partialState().with(facing, Direction.DOWN)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        180, 0, false
                ));
        builder.partialState().with(facing, Direction.UP)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        0, 0, false
                ));
        builder.partialState().with(facing, Direction.NORTH)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        90, 0, false
                ));
        builder.partialState().with(facing, Direction.SOUTH)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        90, 180, false
                ));
        builder.partialState().with(facing, Direction.WEST)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        90, 270, false
                ));
        builder.partialState().with(facing, Direction.EAST)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.modLoc("block/" + MiaPort.getBlockPath(block))),
                        90, 90, false
                ));
    }

}