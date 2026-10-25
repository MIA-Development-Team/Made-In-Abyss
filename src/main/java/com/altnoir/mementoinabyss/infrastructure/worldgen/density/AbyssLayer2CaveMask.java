package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.infrastructure.worldgen.river.AbyssRiverProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

public record AbyssLayer2CaveMask(
        int surfaceDepth, int riverClearance, @Nullable AbyssRiverProfile profile)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<AbyssLayer2CaveMask> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    Codec.intRange(4, 32)
                                                            .fieldOf("surface_depth")
                                                            .forGetter(
                                                                    AbyssLayer2CaveMask
                                                                            ::surfaceDepth),
                                                    Codec.intRange(8, 32)
                                                            .fieldOf("river_clearance")
                                                            .forGetter(
                                                                    AbyssLayer2CaveMask
                                                                            ::riverClearance))
                                            .apply(
                                                    instance,
                                                    (depth, clearance) ->
                                                            new AbyssLayer2CaveMask(
                                                                    depth, clearance, null))));

    @Override
    public double compute(FunctionContext context) {
        return profile == null ? 0 : profile.caveWeight(context, surfaceDepth, riverClearance);
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 1;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
