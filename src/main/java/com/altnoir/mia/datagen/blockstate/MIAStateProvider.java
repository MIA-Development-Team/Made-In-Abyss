package com.altnoir.mia.datagen.blockstate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

public class MIAStateProvider {
    public void registerAbyssGrassBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 0, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 90, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 180, false),
                        new ConfiguredModel(provider.models().getExistingFile(provider.modLoc("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath())), 0, 270, false)
                );
    }
}