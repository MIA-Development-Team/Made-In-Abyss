package com.altnoir.mia.worldgen.noise_setting;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.List;

public class MiaNoiseGeneratorSettings {
    public static final NoiseSettings ABYSS_BRINK_NOISE_SETTINGS = NoiseSettings.create(0, 368, 4, 4);

    public static final ResourceKey<NoiseGeneratorSettings> ABYSS_BRINK = ResourceKey.create(
            Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink")
    );

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(ABYSS_BRINK, abyssBrink(context));
    }

    public static NoiseGeneratorSettings abyssBrink(BootstrapContext<?> context) {
        return new NoiseGeneratorSettings(
                ABYSS_BRINK_NOISE_SETTINGS,
                MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.abyssBrink(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE)),
                MiaSurfaceRuleData.abyssBrink(false),
                List.of(), // 生成目标
                0, // 海平面高度
                false, // 禁用生物生成
                true, // 启用含水层
                true, // 启用矿脉
                false // 使用旧版随机源
        );
    }
}