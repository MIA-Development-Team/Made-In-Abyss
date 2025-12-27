package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.List;

public class MiaNoiseGeneratorSettings {
    public static final NoiseSettings ABYSS_EDGE_NOISE_SETTINGS = NoiseSettings.create(0, 512, 4, 4);
    public static final NoiseSettings TEMPTATION_FOREST_NOISE_SETTINGS = NoiseSettings.create(-64, 320, 4, 4);

    public static final ResourceKey<NoiseGeneratorSettings> ABYSS_EDGE = ResourceKey.create(
            Registries.NOISE_SETTINGS, MiaUtil.miaId("abyss_edge")
    );
    public static final ResourceKey<NoiseGeneratorSettings> TEMPTATION_FOREST = ResourceKey.create(
            Registries.NOISE_SETTINGS, MiaUtil.miaId("temptation_forest")
    );

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(ABYSS_EDGE, abyssEdge(context));
        context.register(TEMPTATION_FOREST, temptationForest(context));
    }

    public static NoiseGeneratorSettings abyssEdge(BootstrapContext<?> context) {
        return new NoiseGeneratorSettings(
                ABYSS_EDGE_NOISE_SETTINGS,
                MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.abyssEdge(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)),
                MiaSurfaceRuleData.abyssEdge(),
                List.of(), // 生成目标
                0, // 海平面高度
                false, // 禁用生物生成
                true, // 启用含水层
                true, // 启用矿脉
                false // 使用旧版随机源
        );
    }

    public static NoiseGeneratorSettings temptationForest(BootstrapContext<?> context) {
        return new NoiseGeneratorSettings(
                TEMPTATION_FOREST_NOISE_SETTINGS,
                MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.temptationForest(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)),
                MiaSurfaceRuleData.temptationForest(),
                List.of(),
                -128,
                false,
                true,
                true,
                false
        );
    }
}