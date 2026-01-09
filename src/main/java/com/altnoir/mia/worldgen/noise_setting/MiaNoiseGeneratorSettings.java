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
    private static final NoiseSettings THE_ABYSS_NOISE_SETTINGS = NoiseSettings.create(-256, 768, 2, 1);

    public static final ResourceKey<NoiseGeneratorSettings> THE_ABYSS = ResourceKey.create(
            Registries.NOISE_SETTINGS, MiaUtil.miaId("the_abyss")
    );

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(THE_ABYSS, theAbyss(context));
    }

    public static NoiseGeneratorSettings theAbyss(BootstrapContext<?> context) {
        return new NoiseGeneratorSettings(
                THE_ABYSS_NOISE_SETTINGS,
                MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.theAbyss(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)),
                MiaSurfaceRuleData.theAbyss(),
                List.of(), // 生成目标
                -256, // 海平面高度
                false, // 禁用生物生成
                true, // 启用含水层
                false, // 启用矿脉
                false // 使用旧版随机源
        );
    }
}