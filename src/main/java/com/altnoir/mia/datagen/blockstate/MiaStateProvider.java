package com.altnoir.mia.datagen.blockstate;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

import java.util.Map;

public class MiaStateProvider {
    public void baseBlockState(BlockStateProvider p, Block block) {
        p.getVariantBuilder(block).partialState().addModels(getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block))));
    }

    public void rotationYBlockState(BlockStateProvider p, Block block) {
        p.getVariantBuilder(block)
                .partialState().addModels(
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block))),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block)), 90),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block)), 180),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block)), 270)
                );
    }

    public void mirroredBlockState(BlockStateProvider p, Block block) {
        p.getVariantBuilder(block)
                .partialState().addModels(
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block))),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored")),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block)), 180),
                        getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_mirrored"), 180)
                );
    }

    public void moistureBlockState(BlockStateProvider p, Block block) {
        VariantBlockStateBuilder builder = p.getVariantBuilder(block);
        IntegerProperty moisture = BlockStateProperties.MOISTURE;

        for (int i = 0; i < 7; i++) {
            builder.partialState()
                    .with(moisture, i)
                    .addModels(getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block))));
        }

        builder.partialState()
                .with(moisture, 7)
                .addModels(getVariantBuilder(p, p.modLoc("block/" + MiaUtil.getBlockPath(block) + "_moist")));
    }

    public void abyssSpawnerBlockState(BlockStateProvider p, Block block) {
        VariantBlockStateBuilder builder = p.getVariantBuilder(block);
        String blockPath = MiaUtil.getBlockPath(block);

        Map<TrialSpawnerState, String> stateSuffixMap = Map.of(
                TrialSpawnerState.ACTIVE, "_active",
                TrialSpawnerState.COOLDOWN, "",
                TrialSpawnerState.EJECTING_REWARD, "_ejecting_reward",
                TrialSpawnerState.INACTIVE, "",
                TrialSpawnerState.WAITING_FOR_PLAYERS, "_active",
                TrialSpawnerState.WAITING_FOR_REWARD_EJECTION, "_ejecting_reward"
        );

        for (TrialSpawnerState state : stateSuffixMap.keySet()) {
            String suffix = stateSuffixMap.get(state);
            String modelName = blockPath + suffix;

            builder.partialState()
                    .with(BlockStateProperties.TRIAL_SPAWNER_STATE, state)
                    .modelForState()
                    .modelFile(p.models().getExistingFile(p.modLoc("block/" + modelName)))
                    .addModel();
        }
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider P, ResourceLocation state) {
        return getVariantBuilder(P, state, 0);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider p, ResourceLocation state, int rotationY) {
        return getVariantBuilder(p, state, 0, rotationY);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider p, ResourceLocation state, int rotationX, int rotationY) {
        return getVariantBuilder(p, state, rotationX, rotationY, false);
    }

    private ConfiguredModel getVariantBuilder(BlockStateProvider p, ResourceLocation state, int rotationX, int rotationY, boolean uvLock) {
        return new ConfiguredModel(p.models().getExistingFile(state), rotationX, rotationY, uvLock);
    }

}