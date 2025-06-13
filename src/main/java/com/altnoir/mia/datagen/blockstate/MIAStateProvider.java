package com.altnoir.mia.datagen.blockstate;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

public class MIAStateProvider {
    public void rotationYBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 90, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 180, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 270, false)
                );
    }
    public void mirroredBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + "_mirrored")), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 180, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath() + "_mirrored")), 0, 180, false)
                );
    }
    public void rodBlockState(BlockStateProvider provider, Block block) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(block);
        DirectionProperty facing = BlockStateProperties.FACING;

        builder.partialState().with(facing, Direction.DOWN)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        180, 0, false
                ));
        builder.partialState().with(facing, Direction.UP)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        0, 0, false
                ));
        builder.partialState().with(facing, Direction.NORTH)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        90, 0, false
                ));
        builder.partialState().with(facing, Direction.SOUTH)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        90, 180, false
                ));
        builder.partialState().with(facing, Direction.WEST)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        90, 270, false
                ));
        builder.partialState().with(facing, Direction.EAST)
                .addModels(new ConfiguredModel(
                        provider.models().getExistingFile(provider.mcLoc("block/end_rod")),
                        90, 90, false
                ));
    }

}