package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

import java.util.Map;

public class MiaStateProvider {
    public void baseBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block).partialState().addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block))));
    }

    public void rotationYBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block))),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 90),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 180),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 270)
                );
    }

    public void mirroredBlockState(BlockStateProvider provider, Block block) {
        provider.getVariantBuilder(block)
                .partialState().addModels(
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block))),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored")),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 180),
                        getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored"), 180)
                );
    }

    public void moistureBlockState(BlockStateProvider provider, Block block) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(block);
        IntegerProperty moisture = BlockStateProperties.MOISTURE;

        for (int i = 0; i < 7; i++) {
            builder.partialState()
                    .with(moisture, i)
                    .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block))));
        }

        builder.partialState()
                .with(moisture, 7)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block) + "_moist")));
    }

    public void lampTubeBlockState(BlockStateProvider provider, Block block) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(block);
        DirectionProperty facing = BlockStateProperties.FACING;

        builder.partialState().with(facing, Direction.DOWN)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 180, 0));
        builder.partialState().with(facing, Direction.UP)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block))));
        builder.partialState().with(facing, Direction.NORTH)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 90, 0));
        builder.partialState().with(facing, Direction.SOUTH)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 90, 180));
        builder.partialState().with(facing, Direction.WEST)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 90, 270));
        builder.partialState().with(facing, Direction.EAST)
                .addModels(getVariantBuilder(provider, provider.modLoc("block/" + MiaUtil.getBlockPath(block)), 90, 90));
    }

    public void abyssSpawnerBlockState(BlockStateProvider provider, Block block) {
        VariantBlockStateBuilder builder = provider.getVariantBuilder(block);
        String blockPath = MiaUtil.getBlockPath(block);

        Map<TrialSpawnerState, String> stateSuffixMap = Map.of(
                TrialSpawnerState.ACTIVE, "_active",
                TrialSpawnerState.COOLDOWN, "",
                TrialSpawnerState.EJECTING_REWARD, "_ejecting_reward",
                TrialSpawnerState.INACTIVE, "",
                TrialSpawnerState.WAITING_FOR_PLAYERS, "_active",
                TrialSpawnerState.WAITING_FOR_REWARD_EJECTION, "_active"
        );

        for (boolean ominous : new boolean[]{false, true}) {
            for (TrialSpawnerState state : stateSuffixMap.keySet()) {
                String suffix = stateSuffixMap.get(state);
                String modelName = blockPath + suffix + (ominous ? "_ominous" : "");

                builder.partialState()
                        .with(BlockStateProperties.TRIAL_SPAWNER_STATE, state)
                        .with(BlockStateProperties.OMINOUS, ominous)
                        .modelForState()
                        .modelFile(provider.models().getExistingFile(provider.modLoc("block/" + modelName)))
                        .addModel();
            }
        }
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider provider, ResourceLocation state) {
        return getVariantBuilder(provider, state, 0);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider provider, ResourceLocation state, int rotationY) {
        return getVariantBuilder(provider, state, 0, rotationY);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider provider, ResourceLocation state, int rotationX, int rotationY) {
        return getVariantBuilder(provider, state, rotationX, rotationY, false);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider provider, ResourceLocation state, int rotationX, int rotationY, boolean uvLock) {
        return new ConfiguredModel(provider.models().getExistingFile(state), rotationX, rotationY, uvLock);
    }

}