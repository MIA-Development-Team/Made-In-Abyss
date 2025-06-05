package com.altnoir.mia.worldgen.noisesetting;

import com.altnoir.mia.MIA;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

public class MIANoiseSettings {
    public static final ResourceKey<NoiseGeneratorSettings> ABYSS_BRINK = ResourceKey.create(
            Registries.NOISE_SETTINGS, ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "abyss_brink")
    );

    protected static final NoiseSettings ABYSS_BRINK_NOISE_SETTINGS = create(-64, 384, 1, 2);

//    public static NoiseGeneratorSettings abyssBrink(BootstrapContext<?> context, boolean large, boolean amplified) {
//        return new NoiseGeneratorSettings(
//                ABYSS_BRINK_NOISE_SETTINGS,
//                MIABlocks.ABYSS_ANDESITE.get().defaultBlockState(),
//                Blocks.WATER.defaultBlockState(),
//                NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), amplified, large),
//                SurfaceRuleData.overworld(),
//            List.of(), // 生成目标
//            -64, // 海平面高度
//            false, // 禁用生物生成
//            true, // 启用含水层
//            true, // 启用矿脉
//            false // 使用旧版随机源
//        );
//    }
    private static DataResult<NoiseSettings> guardY(NoiseSettings settings) {
        if (settings.minY() + settings.height() > DimensionType.MAX_Y + 1) {
            return DataResult.error(() -> "min_y + height cannot be higher than: " + (DimensionType.MAX_Y + 1));
        } else if (settings.height() % 16 != 0) {
            return DataResult.error(() -> "height has to be a multiple of 16");
        } else {
            return settings.minY() % 16 != 0 ? DataResult.error(() -> "min_y has to be a multiple of 16") : DataResult.success(settings);
        }
    }
    public static NoiseSettings create(int minY, int height, int noiseSizeHorizontal, int noiseSizeVertical) {
        NoiseSettings noisesettings = new NoiseSettings(minY, height, noiseSizeHorizontal, noiseSizeVertical);
        guardY(noisesettings).error().ifPresent(noiseSettingsError -> {
            throw new IllegalStateException(noiseSettingsError.message());
        });
        return noisesettings;
    }
}