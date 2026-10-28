package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeGroup;
import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeSelection;
import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jspecify.annotations.Nullable;

public final class TheAbyssBiomeSource extends BiomeSource {
    public static final MapCodec<TheAbyssBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Definition.CODEC.forGetter(
                                                    TheAbyssBiomeSource::definition),
                                            RegistryOps.retrieveRegistryLookup(
                                                            Layer2Biomes.REGISTRY_KEY)
                                                    .forGetter(source -> null))
                                    .apply(i, TheAbyssBiomeSource::new));

    private final Definition definition;
    private final @Nullable Layer2Layout layout;
    private final Supplier<Layer2BiomePalette> palette;
    private final AbyssClimateSampler climate;
    private final @Nullable Layer2BiomeSampler layer2Sampler;
    private final long layer1MaxDepth, invertedMinDepth;

    public TheAbyssBiomeSource(Definition definition) {
        this(definition, null);
    }

    private TheAbyssBiomeSource(
            Definition definition, @Nullable RegistryLookup<Layer2BiomeGroup> lookup) {
        this(definition, null, lookup, new AbyssClimateSampler());
    }

    private TheAbyssBiomeSource(
            Definition definition,
            @Nullable Layer2Layout layout,
            @Nullable RegistryLookup<Layer2BiomeGroup> lookup,
            AbyssClimateSampler climate) {
        this.definition = definition;
        this.layout = layout;
        this.climate = climate;
        // Registry holders may still be unbound while dimensions are being decoded or datagenned.
        palette =
                Suppliers.memoize(
                        () -> {
                            if (lookup == null)
                                throw new IllegalStateException(
                                        "Layer2 biome registry is not installed");
                            return new Layer2BiomePalette(lookup, definition.layer2River());
                        });
        layer2Sampler =
                layout == null
                        ? null
                        : new Layer2BiomeSampler(layout, palette.get(), definition.innerBiome());
        layer1MaxDepth = Climate.quantizeCoord(definition.layer1MaxDepth());
        invertedMinDepth = Climate.quantizeCoord(definition.invertedMinDepth());
    }

    public Definition definition() {
        return definition;
    }

    public Layer2Layout layout() {
        if (layout == null)
            throw new IllegalStateException("The abyss Layer2 biome layout is not installed");
        return layout;
    }

    public TheAbyssBiomeSource bind(
            Layer2Layout layout,
            Climate.Sampler sampler,
            NoiseRouter router,
            RegistryLookup<Layer2BiomeGroup> groups) {
        if (!layout.bound()) throw new IllegalArgumentException("Layer2 layout has no world seed");
        var climate = new AbyssClimateSampler();
        climate.bind(sampler, router);
        return new TheAbyssBiomeSource(definition, layout, groups, climate);
    }

    public Layer2BiomeSampler layer2Sampler() {
        if (layer2Sampler == null)
            throw new IllegalStateException("Layer2 biome sampler is not initialized");
        return layer2Sampler;
    }

    public Holder<Biome> layer2SurfaceBiome(int blockX, int blockZ) {
        return layer2Sampler().biome(blockX, blockZ);
    }

    public Layer2BiomeSelection layer2Selection(int blockX, int blockZ) {
        return layer2Sampler().query(blockX, blockZ);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        layout();
        long depth = AbyssClimateSampler.depth(sampler, x, y, z);
        if (depth >= invertedMinDepth) return definition.invertedBiome();
        if (depth >= layer1MaxDepth)
            return layer2SurfaceBiome(QuartPos.toBlock(x), QuartPos.toBlock(z));
        int sectionX = SectionPos.blockToSectionCoord(QuartPos.toBlock(x));
        int sectionZ = SectionPos.blockToSectionCoord(QuartPos.toBlock(z));
        if ((long) sectionX * sectionX + (long) sectionZ * sectionZ
                <= definition.layer1RadiusSquaredSections()) return definition.innerBiome();
        return definition.layer1Biomes().findValue(climate.sample(sampler, x, y, z, depth));
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                        Stream.concat(
                                Stream.of(definition.innerBiome(), definition.invertedBiome()),
                                definition.layer1Biomes().values().stream()
                                        .map(entry -> entry.getSecond())),
                        palette.get().possibleBiomes())
                .distinct();
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public record Definition(
            float layer1MaxDepth,
            float invertedMinDepth,
            long layer1RadiusSquaredSections,
            Holder<Biome> innerBiome,
            Climate.ParameterList<Holder<Biome>> layer1Biomes,
            Holder<Biome> layer2River,
            Holder<Biome> invertedBiome) {
        public static final MapCodec<Definition> CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                Codec.floatRange(-4, 4)
                                                        .fieldOf("layer1_max_depth")
                                                        .forGetter(Definition::layer1MaxDepth),
                                                Codec.floatRange(-4, 4)
                                                        .fieldOf("inverted_min_depth")
                                                        .forGetter(Definition::invertedMinDepth),
                                                Codec.LONG
                                                        .fieldOf("layer1_radius_squared_sections")
                                                        .forGetter(
                                                                Definition
                                                                        ::layer1RadiusSquaredSections),
                                                Biome.CODEC
                                                        .fieldOf("inner_biome")
                                                        .forGetter(Definition::innerBiome),
                                                AbyssClimateCodecs.parameterList(
                                                                Biome.CODEC.fieldOf("biome"))
                                                        .fieldOf("layer1_biomes")
                                                        .forGetter(Definition::layer1Biomes),
                                                Biome.CODEC
                                                        .fieldOf("layer2_river")
                                                        .forGetter(Definition::layer2River),
                                                Biome.CODEC
                                                        .fieldOf("inverted_biome")
                                                        .forGetter(Definition::invertedBiome))
                                        .apply(i, Definition::new));

        public Definition {
            if (Climate.quantizeCoord(layer1MaxDepth) >= Climate.quantizeCoord(invertedMinDepth))
                throw new IllegalArgumentException("Abyss depth boundaries must increase");
        }
    }
}
