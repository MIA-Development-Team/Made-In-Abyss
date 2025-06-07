package com.altnoir.mia.worldgen.noisesetting;

import com.altnoir.mia.MIA;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MIADensityFunctionType {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MIA.MOD_ID);
    public static MapCodec<? extends DensityFunction> bootstrap(Registry<MapCodec<? extends DensityFunction>> registry) {
        return register(registry, "abyss_hole", AbyssHoleDensityFunction.CODEC);
    }
    private static MapCodec<? extends DensityFunction> register(
            Registry<MapCodec<? extends DensityFunction>> registry, String name, KeyDispatchDataCodec<? extends DensityFunction> codec
    ) {
        return Registry.register(registry, name, codec.codec());
    }
//    public static final MapCodec<? extends DensityFunction> ABYSS = register(registry, "abyss",
//            MIADensityFunctions.EndIslandDensityFunction.CODEC);

    public static DensityFunction abyssHole(long seed) {
        return new AbyssHoleDensityFunction(seed);
    }

    protected static final class AbyssHoleDensityFunction implements DensityFunction.SimpleFunction {
        public static final KeyDispatchDataCodec<AbyssHoleDensityFunction> CODEC = KeyDispatchDataCodec.of(
                MapCodec.unit(new AbyssHoleDensityFunction(0L))
        );
        private static final float ISLAND_THRESHOLD = -0.9F;
        private final SimplexNoise islandNoise;

        public AbyssHoleDensityFunction(long seed) {
            RandomSource randomsource = new LegacyRandomSource(seed);
            randomsource.consumeCount(17292);
            this.islandNoise = new SimplexNoise(randomsource);
        }

        private static float getHeightValue(SimplexNoise noise, int x, int z) {
            int i = x / 2;
            int j = z / 2;
            int k = x % 2;
            int l = z % 2;
            float f = 100.0F - Mth.sqrt((float)(x * x + z * z)) * 8.0F;
            f = Mth.clamp(f, -100.0F, 80.0F);

            for (int i1 = -12; i1 <= 12; i1++) {
                for (int j1 = -12; j1 <= 12; j1++) {
                    long k1 = (long)(i + i1);
                    long l1 = (long)(j + j1);
                    if (k1 * k1 + l1 * l1 > 4096L && noise.getValue((double)k1, (double)l1) < -0.9F) {
                        float f1 = (Mth.abs((float)k1) * 3439.0F + Mth.abs((float)l1) * 147.0F) % 13.0F + 9.0F;
                        float f2 = (float)(k - i1 * 2);
                        float f3 = (float)(l - j1 * 2);
                        float f4 = 100.0F - Mth.sqrt(f2 * f2 + f3 * f3) * f1;
                        f4 = Mth.clamp(f4, -100.0F, 80.0F);
                        f = Math.max(f, f4);
                    }
                }
            }

            return f;
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return ((double)getHeightValue(this.islandNoise, context.blockX() / 8, context.blockZ() / 8) - 8.0) / 128.0;
        }

        @Override
        public double minValue() {
            return -0.84375;
        }

        @Override
        public double maxValue() {
            return 0.5625;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }

    public static void register(IEventBus eventBus) {
        DENSITY_FUNCTION_TYPE.register(eventBus);
    }
}
