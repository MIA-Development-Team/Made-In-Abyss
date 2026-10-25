package com.altnoir.mementoinabyss.infrastructure.worldgen.density;

import com.altnoir.mementoinabyss.infrastructure.worldgen.river.AbyssRiverProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

public record AbyssRiverMask(
        DensityFunction support,
        DensityFunction bank,
        DensityFunction bed,
        int supportY,
        int clearanceY,
        @Nullable AbyssRiverProfile profile)
        implements DensityFunction {
    public static final KeyDispatchDataCodec<AbyssRiverMask> CODEC =
            KeyDispatchDataCodec.of(
                    RecordCodecBuilder.mapCodec(
                            instance ->
                                    instance.group(
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("support")
                                                            .forGetter(AbyssRiverMask::support),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("bank")
                                                            .forGetter(AbyssRiverMask::bank),
                                                    DensityFunction.HOLDER_HELPER_CODEC
                                                            .fieldOf("bed")
                                                            .forGetter(AbyssRiverMask::bed),
                                                    Codec.INT
                                                            .fieldOf("support_y")
                                                            .forGetter(AbyssRiverMask::supportY),
                                                    Codec.INT
                                                            .fieldOf("clearance_y")
                                                            .forGetter(AbyssRiverMask::clearanceY))
                                            .apply(
                                                    instance,
                                                    (support, bank, bed, supportY, clearanceY) ->
                                                            new AbyssRiverMask(
                                                                    support,
                                                                    bank,
                                                                    bed,
                                                                    supportY,
                                                                    clearanceY,
                                                                    null))));

    @Override
    public double compute(FunctionContext context) {
        return profile == null ? 0 : profile.column(context.blockX(), context.blockZ()).weight();
    }

    @Override
    public void fillArray(double[] output, ContextProvider provider) {
        provider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // Once bound, these children are metadata for the world-local sampler. Rewrapping
        // their entire cave graphs per chunk adds unused interpolators and cell caches.
        if (profile != null) return visitor.apply(this);
        return visitor.apply(
                new AbyssRiverMask(
                        support.mapAll(visitor),
                        bank.mapAll(visitor),
                        bed.mapAll(visitor),
                        supportY,
                        clearanceY,
                        profile));
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
