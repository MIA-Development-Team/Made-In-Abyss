package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeResolver;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jspecify.annotations.Nullable;

public final class TheAbyssBiomeSource extends BiomeSource {
    public static final MapCodec<TheAbyssBiomeSource> CODEC =
            Definition.CODEC.xmap(TheAbyssBiomeSource::new, TheAbyssBiomeSource::definition);

    private final Definition definition;
    private final @Nullable Layer2Layout layout;
    private final Layer2BiomeResolver layer2Resolver;
    private final AbyssClimateSampler climate;
    private final long layer1MaxDepth, invertedMinDepth;

    public TheAbyssBiomeSource(Definition definition) {
        this(definition, null, new NativeLayer2Resolver(definition), new AbyssClimateSampler());
    }

    private TheAbyssBiomeSource(
            Definition definition,
            @Nullable Layer2Layout layout,
            Layer2BiomeResolver layer2Resolver,
            AbyssClimateSampler climate) {
        this.definition = definition;
        this.layout = layout;
        this.layer2Resolver = layer2Resolver;
        this.climate = climate;
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
            Layer2Layout layout, Climate.Sampler sampler, NoiseRouter router) {
        if (!layout.bound()) throw new IllegalArgumentException("Layer2 layout has no world seed");
        var climate = new AbyssClimateSampler();
        climate.bind(sampler, router);
        return new TheAbyssBiomeSource(
                definition, layout, new NativeLayer2Resolver(definition), climate);
    }

    /** Runtime palettes are reinstalled at generator setup; only native definitions are serialized. */
    public TheAbyssBiomeSource withLayer2Resolver(Layer2BiomeResolver resolver) {
        return new TheAbyssBiomeSource(
                definition, layout(), Objects.requireNonNull(resolver), climate);
    }

    public Holder<Biome> layer2SurfaceBiome(int blockX, int blockZ) {
        var layout = layout();
        if (Math.hypot(blockX, blockZ) < layout.openingRadius() + 48)
            return definition.innerBiome();
        var region = layout.sample(blockX, blockZ);
        return layer2Resolver.resolve(region, region.edgeDistance() < layout.riverBankDistance());
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
                        layer2Resolver.possibleBiomes())
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
            List<Group> layer2Groups,
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
                                                Group.CODEC
                                                        .listOf()
                                                        .fieldOf("layer2_groups")
                                                        .forGetter(Definition::layer2Groups),
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
            layer2Groups = List.copyOf(layer2Groups);
            if (layer2Groups.isEmpty())
                throw new IllegalArgumentException("Layer2 needs a biome group");
        }
    }

    public record Group(Holder<Biome> primary, List<Holder<Biome>> companions) {
        private static final Codec<List<Holder<Biome>>> COMPANIONS =
                Codec.either(
                                Biome.CODEC,
                                Biome.CODEC
                                        .listOf()
                                        .validate(
                                                values ->
                                                        values.size() == 4
                                                                ? DataResult.success(values)
                                                                : DataResult.error(
                                                                        () ->
                                                                                "Expected four"
                                                                                    + " companion"
                                                                                    + " sectors")))
                        .xmap(
                                choice ->
                                        choice.map(
                                                biome -> List.of(biome, biome, biome, biome),
                                                List::copyOf),
                                values ->
                                        values.stream().allMatch(values.getFirst()::equals)
                                                ? Either.left(values.getFirst())
                                                : Either.right(values));
        public static final Codec<Group> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                Biome.CODEC
                                                        .fieldOf("primary")
                                                        .forGetter(Group::primary),
                                                COMPANIONS
                                                        .fieldOf("companions")
                                                        .forGetter(Group::companions))
                                        .apply(i, Group::new));

        public Group {
            companions = List.copyOf(companions);
            if (companions.size() != 4)
                throw new IllegalArgumentException("Expected four companion sectors");
        }

        public Group(Holder<Biome> primary, Holder<Biome> companion) {
            this(primary, List.of(companion, companion, companion, companion));
        }
    }

    private record NativeLayer2Resolver(Definition definition) implements Layer2BiomeResolver {
        @Override
        public Holder<Biome> resolve(Layer2Layout.Region region, boolean river) {
            if (river) return definition.layer2River();
            var group =
                    definition
                            .layer2Groups()
                            .get(Math.floorMod(region.id(), definition.layer2Groups().size()));
            return region.primary() ? group.primary() : group.companions().get(region.sector());
        }

        @Override
        public Stream<Holder<Biome>> possibleBiomes() {
            return Stream.concat(
                    Stream.of(definition.layer2River()),
                    definition.layer2Groups().stream()
                            .flatMap(
                                    group ->
                                            Stream.concat(
                                                    Stream.of(group.primary()),
                                                    group.companions().stream())));
        }
    }
}
