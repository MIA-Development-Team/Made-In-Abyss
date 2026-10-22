package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Climate;

public final class AbyssClimateCodecs {
    public static final Codec<Climate.Parameter> DEPTH =
            ExtraCodecs.intervalCodec(
                    Codec.floatRange(-4.0F, 4.0F),
                    "min",
                    "max",
                    (min, max) ->
                            min.compareTo(max) > 0
                                    ? DataResult.error(
                                            () ->
                                                    "Depth minimum exceeds maximum: "
                                                            + min
                                                            + " > "
                                                            + max)
                                    : DataResult.success(Climate.Parameter.span(min, max)),
                    parameter -> Climate.unquantizeCoord(parameter.min()),
                    parameter -> Climate.unquantizeCoord(parameter.max()));

    private static final Codec<Climate.ParameterPoint> PARAMETER_POINT =
            RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                            Climate.Parameter.CODEC
                                                    .fieldOf("temperature")
                                                    .forGetter(Climate.ParameterPoint::temperature),
                                            Climate.Parameter.CODEC
                                                    .fieldOf("humidity")
                                                    .forGetter(Climate.ParameterPoint::humidity),
                                            Climate.Parameter.CODEC
                                                    .fieldOf("continentalness")
                                                    .forGetter(
                                                            Climate.ParameterPoint
                                                                    ::continentalness),
                                            Climate.Parameter.CODEC
                                                    .fieldOf("erosion")
                                                    .forGetter(Climate.ParameterPoint::erosion),
                                            DEPTH.fieldOf("depth")
                                                    .forGetter(Climate.ParameterPoint::depth),
                                            Climate.Parameter.CODEC
                                                    .fieldOf("weirdness")
                                                    .forGetter(Climate.ParameterPoint::weirdness),
                                            Codec.floatRange(0.0F, 1.0F)
                                                    .fieldOf("offset")
                                                    .xmap(
                                                            Climate::quantizeCoord,
                                                            Climate::unquantizeCoord)
                                                    .forGetter(Climate.ParameterPoint::offset))
                                    .apply(instance, Climate.ParameterPoint::new));

    public static <T> Codec<Climate.ParameterList<T>> parameterList(MapCodec<T> entryCodec) {
        return ExtraCodecs.nonEmptyList(
                        RecordCodecBuilder.<Pair<Climate.ParameterPoint, T>>create(
                                        instance ->
                                                instance.group(
                                                                PARAMETER_POINT
                                                                        .fieldOf("parameters")
                                                                        .forGetter(Pair::getFirst),
                                                                entryCodec.forGetter(
                                                                        Pair::getSecond))
                                                        .apply(instance, Pair::of))
                                .listOf())
                .xmap(Climate.ParameterList::new, Climate.ParameterList::values);
    }

    private AbyssClimateCodecs() {}
}
