package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.structure.Layer2LandmarkPlacement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;

public final class TheAbyssChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<TheAbyssChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(
                                                            TheAbyssChunkGenerator::getBiomeSource),
                                            NoiseGeneratorSettings.CODEC
                                                    .fieldOf("settings")
                                                    .forGetter(
                                                            TheAbyssChunkGenerator
                                                                    ::generatorSettings))
                                    .apply(instance, TheAbyssChunkGenerator::new));

    private volatile Extension extension = new Extension() {};
    private volatile BiomeSource featureSource;
    private List<FeatureSorter.StepFeatureData> layerFeatures;
    private boolean stateCreated;

    public TheAbyssChunkGenerator(
            BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    public synchronized void setExtension(Extension extension) {
        if (stateCreated)
            throw new IllegalStateException(
                    "The abyss extension must be installed before createState");
        this.extension = Objects.requireNonNull(extension);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public synchronized ChunkGeneratorStructureState createState(
            HolderLookup<StructureSet> structureSets, RandomState randomState, long seed) {
        stateCreated = true;
        HolderLookup<StructureSet> filtered =
                new HolderLookup<>() {
                    @Override
                    public Stream<Holder.Reference<StructureSet>> listElements() {
                        return structureSets.listElements().filter(extension::includeStructureSet);
                    }

                    @Override
                    public Stream<HolderSet.Named<StructureSet>> listTags() {
                        return structureSets.listTags();
                    }

                    @Override
                    public Optional<Holder.Reference<StructureSet>> get(
                            ResourceKey<StructureSet> key) {
                        return structureSets.get(key).filter(extension::includeStructureSet);
                    }

                    @Override
                    public Optional<HolderSet.Named<StructureSet>> get(TagKey<StructureSet> tag) {
                        return structureSets.get(tag);
                    }
                };
        return super.createState(filtered, randomState, seed);
    }

    private synchronized List<FeatureSorter.StepFeatureData> layerFeatures() {
        if (featureSource != getBiomeSource()) {
            layerFeatures =
                    FeatureSorter.buildFeaturesPerStep(
                            List.copyOf(getBiomeSource().possibleBiomes()),
                            biome -> getBiomeGenerationSettings(biome).features(),
                            true);
            featureSource = getBiomeSource();
        }
        return layerFeatures;
    }

    @Override
    public synchronized void refreshFeaturesPerStep() {
        super.refreshFeaturesPerStep();
        featureSource = null;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(
            RandomState randomState,
            Blender blender,
            StructureManager structureManager,
            ChunkAccess chunk) {
        var terrain = extension.structureTerrain(structureManager, chunk);
        if (terrain != null) {
            var settings = generatorSettings().value();
            // Install local structure terrain before vanilla/C2ME cache the chunk's sampler.
            chunk.getOrCreateNoiseChunk(
                    c ->
                            NoiseChunk.forChunk(
                                    c,
                                    randomState,
                                    terrain,
                                    settings,
                                    fluidPicker(settings),
                                    blender));
        }
        return super.createBiomes(randomState, blender, structureManager, chunk);
    }

    @Override
    public void buildSurface(
            ChunkAccess chunk,
            WorldGenerationContext context,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            Registry<Biome> biomes,
            Blender blender) {
        var settings = generatorSettings().value();
        var rule = extension.surfaceRule(settings.surfaceRule());
        if (rule == settings.surfaceRule()) {
            super.buildSurface(
                    chunk, context, randomState, structureManager, biomeManager, biomes, blender);
            return;
        }
        var noise =
                chunk.getOrCreateNoiseChunk(
                        c ->
                                NoiseChunk.forChunk(
                                        c,
                                        randomState,
                                        Beardifier.forStructuresInChunk(
                                                structureManager, c.getPos()),
                                        settings,
                                        fluidPicker(settings),
                                        blender));
        randomState
                .surfaceSystem()
                .buildSurface(
                        randomState,
                        biomeManager,
                        biomes,
                        settings.useLegacyRandomSource(),
                        context,
                        chunk,
                        noise,
                        rule);
    }

    private static Aquifer.FluidPicker fluidPicker(NoiseGeneratorSettings settings) {
        var lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        var sea = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        var empty =
                new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        return (x, y, z) ->
                SharedConstants.DEBUG_DISABLE_FLUID_GENERATION
                        ? empty
                        : y < Math.min(-54, settings.seaLevel()) ? lava : sea;
    }

    public record GenerationContext(ChunkGenerator generator, LevelHeightAccessor heightAccessor) {}

    public record PlacementContext(WorldGenLevel level, ChunkGenerator generator) {}

    public interface Scope extends AutoCloseable {
        Scope NOOP = () -> {};

        @Override
        void close();
    }

    public interface Extension {
        default Scope structureScope(Holder<Structure> selected, boolean placing) {
            return Scope.NOOP;
        }

        default SurfaceRules.RuleSource surfaceRule(SurfaceRules.RuleSource original) {
            return original;
        }

        default DensityFunctions.@Nullable BeardifierOrMarker structureTerrain(
                StructureManager structureManager, ChunkAccess chunk) {
            return null;
        }

        default boolean includeStructureSet(Holder<StructureSet> set) {
            return true;
        }

        default Predicate<Holder<Biome>> validBiome(
                Holder<Structure> selected, Predicate<Holder<Biome>> vanilla, ChunkPos candidate) {
            return vanilla;
        }

        default @Nullable GenerationContext generationContext(
                Holder<Structure> structure,
                ChunkAccess chunk,
                RandomState randomState,
                TheAbyssChunkGenerator generator) {
            return new GenerationContext(generator, chunk);
        }

        default StructureStart adaptStart(
                Holder<Structure> structure,
                StructureStart start,
                ChunkAccess chunk,
                RandomState randomState) {
            return start;
        }

        default @Nullable PlacementContext structurePlacement(
                Holder<Structure> structure,
                StructureStart start,
                WorldGenLevel level,
                TheAbyssChunkGenerator generator) {
            return new PlacementContext(level, generator);
        }

        default @Nullable PlacementContext featurePlacement(
                PlacedFeature feature, WorldGenLevel level, TheAbyssChunkGenerator generator) {
            return new PlacementContext(level, generator);
        }
    }

    @Override
    public @Nullable Pair<BlockPos, Holder<Structure>> findNearestMapStructure(
            ServerLevel level,
            HolderSet<Structure> wantedStructures,
            BlockPos pos,
            int maxSearchRadius,
            boolean createReference) {
        if (SharedConstants.DEBUG_DISABLE_FEATURES) return null;
        var nearest =
                super.findNearestMapStructure(
                        level, wantedStructures, pos, maxSearchRadius, createReference);
        var state = level.getChunkSource().getGeneratorState();
        Map<NativePlacementKey, NativeTargets> targets = new LinkedHashMap<>();
        for (Holder<Structure> holder : wantedStructures) {
            for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                if (!(placement instanceof Layer2LandmarkPlacement nativePlacement)) continue;
                var key =
                        new NativePlacementKey(
                                nativePlacement.primary(), nativePlacement.placementSalt());
                targets.computeIfAbsent(
                                key,
                                ignored ->
                                        new NativeTargets(nativePlacement, new ObjectArraySet<>()))
                        .structures()
                        .add(holder);
            }
        }
        if (targets.isEmpty()) return nearest;
        double bestDistance =
                nearest == null ? Double.POSITIVE_INFINITY : nearest.getFirst().distSqr(pos);
        List<NativeCandidate> candidates = new ArrayList<>();
        for (NativeTargets target : targets.values()) {
            for (ChunkPos chunk :
                    target.placement()
                            .candidates(state, ChunkPos.containing(pos), maxSearchRadius)) {
                if (target.placement().getLocatePos(chunk).distSqr(pos) < bestDistance)
                    candidates.add(new NativeCandidate(target, chunk));
            }
        }
        candidates.sort(
                Comparator.comparingDouble(
                        candidate ->
                                candidate
                                        .targets()
                                        .placement()
                                        .getLocatePos(candidate.chunk())
                                        .distSqr(pos)));
        var manager = level.structureManager();
        for (NativeCandidate candidate : candidates) {
            var target = candidate.targets();
            var found =
                    getNativeStructureGeneratingAt(
                            target.structures(),
                            level,
                            manager,
                            createReference,
                            target.placement(),
                            candidate.chunk());
            if (found != null) return found;
        }
        return nearest;
    }

    private @Nullable Pair<BlockPos, Holder<Structure>> getNativeStructureGeneratingAt(
            Set<Holder<Structure>> structures,
            ServerLevel level,
            StructureManager manager,
            boolean createReference,
            Layer2LandmarkPlacement placement,
            ChunkPos candidate) {
        for (Holder<Structure> holder : structures) {
            var result =
                    manager.checkStructurePresence(
                            candidate, holder.value(), placement, createReference);
            if (result == StructureCheckResult.START_NOT_PRESENT
                    && !matchesNativeBiome(holder, level, candidate, placement)) continue;
            if (!createReference && result == StructureCheckResult.START_PRESENT)
                return Pair.of(placement.getLocatePos(candidate), holder);
            ChunkAccess chunk =
                    level.getChunk(candidate.x(), candidate.z(), ChunkStatus.STRUCTURE_STARTS);
            StructureStart start =
                    manager.getStartForStructure(SectionPos.bottomOf(chunk), holder.value(), chunk);
            if (start != null && start.isValid() && (!createReference || start.canBeReferenced())) {
                if (createReference) manager.addReference(start);
                return Pair.of(placement.getLocatePos(start.getChunkPos()), holder);
            }
        }
        return null;
    }

    private boolean matchesNativeBiome(
            Holder<Structure> holder,
            ServerLevel level,
            ChunkPos candidate,
            StructurePlacement placement) {
        // StructureCheck's unsaved-chunk fallback uses vanilla biome validation, bypassing this
        // extension.
        try (var scope = extension.structureScope(holder, false)) {
            var structure = holder.value();
            var validBiome = extension.validBiome(holder, structure.biomes()::contains, candidate);
            var biome =
                    getBiomeSource()
                            .getNoiseBiome(
                                    QuartPos.fromBlock(candidate.getMiddleBlockX()),
                                    QuartPos.fromBlock(placement.getLocatePos(candidate).getY()),
                                    QuartPos.fromBlock(candidate.getMiddleBlockZ()),
                                    level.getChunkSource().randomState().sampler());
            return validBiome.test(biome);
        }
    }

    private record NativePlacementKey(boolean primary, int salt) {}

    private record NativeTargets(
            Layer2LandmarkPlacement placement, Set<Holder<Structure>> structures) {}

    private record NativeCandidate(NativeTargets targets, ChunkPos chunk) {}

    @Override
    public void applyBiomeDecoration(
            WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos centerPos = chunk.getPos();
        if (!SharedConstants.debugVoidTerrain(centerPos)) {
            SectionPos sectionPos = SectionPos.of(centerPos, level.getMinSectionY());
            BlockPos origin = sectionPos.origin();
            Registry<Structure> structuresRegistry =
                    level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            Map<Integer, List<Structure>> structuresByStep =
                    structuresRegistry.stream()
                            .collect(
                                    Collectors.groupingBy(structure -> structure.step().ordinal()));
            List<FeatureSorter.StepFeatureData> featureList = this.layerFeatures();
            WorldgenRandom random =
                    new WorldgenRandom(
                            new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
            long decorationSeed =
                    random.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());
            Set<Holder<Biome>> possibleBiomes = new ObjectArraySet<>();
            ChunkPos.rangeClosed(sectionPos.chunk(), 1)
                    .forEach(
                            chunkPos -> {
                                ChunkAccess chunkInRange =
                                        level.getChunk(chunkPos.x(), chunkPos.z());

                                for (LevelChunkSection section : chunkInRange.getSections()) {
                                    section.getBiomes().getAll(possibleBiomes::add);
                                }
                            });
            possibleBiomes.retainAll(this.biomeSource.possibleBiomes());
            int featureStepCount = featureList.size();

            try {
                Registry<PlacedFeature> featureRegistry =
                        level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
                int generationSteps =
                        Math.max(GenerationStep.Decoration.values().length, featureStepCount);

                for (int stepIndex = 0; stepIndex < generationSteps; stepIndex++) {
                    int index = 0;
                    if (structureManager.shouldGenerateStructures()) {
                        for (Structure structure :
                                structuresByStep.getOrDefault(stepIndex, Collections.emptyList())) {
                            random.setFeatureSeed(decorationSeed, index, stepIndex);
                            Supplier<String> currentlyGenerating =
                                    () ->
                                            structuresRegistry
                                                    .getResourceKey(structure)
                                                    .map(Object::toString)
                                                    .orElseGet(structure::toString);

                            try {
                                level.setCurrentlyGenerating(currentlyGenerating);
                                structureManager
                                        .startsForStructure(sectionPos, structure)
                                        .forEach(
                                                start -> {
                                                    var holder =
                                                            structuresRegistry.wrapAsHolder(
                                                                    structure);
                                                    try (var scope =
                                                            this.extension.structureScope(
                                                                    holder, true)) {
                                                        var context =
                                                                this.extension.structurePlacement(
                                                                        holder, start, level, this);
                                                        if (context != null)
                                                            start.placeInChunk(
                                                                    context.level(),
                                                                    structureManager,
                                                                    context.generator(),
                                                                    random,
                                                                    getWritableArea(chunk),
                                                                    centerPos);
                                                    }
                                                });
                            } catch (Exception var29) {
                                CrashReport report =
                                        CrashReport.forThrowable(var29, "Feature placement");
                                report.addCategory("Feature")
                                        .setDetail("Description", currentlyGenerating::get);
                                throw new ReportedException(report);
                            }

                            index++;
                        }
                    }

                    if (stepIndex < featureStepCount) {
                        IntSet possibleFeaturesThisStep = new IntArraySet();

                        for (Holder<Biome> biome : possibleBiomes) {
                            List<HolderSet<PlacedFeature>> featuresInBiome =
                                    this.getBiomeGenerationSettings(biome).features();
                            if (stepIndex < featuresInBiome.size()) {
                                HolderSet<PlacedFeature> featuresInBiomeThisStep =
                                        featuresInBiome.get(stepIndex);
                                FeatureSorter.StepFeatureData stepFeatureData =
                                        featureList.get(stepIndex);
                                featuresInBiomeThisStep.stream()
                                        .map(Holder::value)
                                        .forEach(
                                                featurex ->
                                                        possibleFeaturesThisStep.add(
                                                                stepFeatureData
                                                                        .indexMapping()
                                                                        .applyAsInt(featurex)));
                            }
                        }

                        int numberOfFeaturesInStep = possibleFeaturesThisStep.size();
                        int[] indexArray = possibleFeaturesThisStep.toIntArray();
                        Arrays.sort(indexArray);
                        FeatureSorter.StepFeatureData stepFeatureData = featureList.get(stepIndex);

                        for (int featureIndex = 0;
                                featureIndex < numberOfFeaturesInStep;
                                featureIndex++) {
                            int globalIndexOfFeature = indexArray[featureIndex];
                            PlacedFeature feature =
                                    stepFeatureData.features().get(globalIndexOfFeature);
                            Supplier<String> currentlyGenerating =
                                    () ->
                                            featureRegistry
                                                    .getResourceKey(feature)
                                                    .map(Object::toString)
                                                    .orElseGet(feature::toString);
                            random.setFeatureSeed(decorationSeed, globalIndexOfFeature, stepIndex);

                            try {
                                level.setCurrentlyGenerating(currentlyGenerating);
                                var context = this.extension.featurePlacement(feature, level, this);
                                if (context != null)
                                    feature.placeWithBiomeCheck(
                                            context.level(), context.generator(), random, origin);
                            } catch (Exception var30) {
                                CrashReport report =
                                        CrashReport.forThrowable(var30, "Feature placement");
                                report.addCategory("Feature")
                                        .setDetail("Description", currentlyGenerating::get);
                                throw new ReportedException(report);
                            }
                        }
                    }
                }

                level.setCurrentlyGenerating(null);
                if (SharedConstants.DEBUG_FEATURE_COUNT) {
                    FeatureCountTracker.chunkDecorated(level.getLevel());
                }
            } catch (Exception var31) {
                CrashReport report = CrashReport.forThrowable(var31, "Biome decoration");
                report.addCategory("Generation")
                        .setDetail("CenterX", centerPos.x())
                        .setDetail("CenterZ", centerPos.z())
                        .setDetail("Decoration Seed", decorationSeed);
                throw new ReportedException(report);
            }
        }
    }

    private static BoundingBox getWritableArea(ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int targetBlockX = chunkPos.getMinBlockX();
        int targetBlockZ = chunkPos.getMinBlockZ();
        LevelHeightAccessor heightAccessor = chunk.getHeightAccessorForGeneration();
        int minY = heightAccessor.getMinY() + 1;
        int maxY = heightAccessor.getMaxY();
        return new BoundingBox(
                targetBlockX, minY, targetBlockZ, targetBlockX + 15, maxY, targetBlockZ + 15);
    }

    @Override
    public void createStructures(
            RegistryAccess registryAccess,
            ChunkGeneratorStructureState state,
            StructureManager structureManager,
            ChunkAccess centerChunk,
            StructureTemplateManager structureTemplateManager,
            ResourceKey<Level> level) {
        if (!SharedConstants.DEBUG_DISABLE_STRUCTURES) {
            ChunkPos sourceChunkPos = centerChunk.getPos();
            SectionPos sectionPos = SectionPos.bottomOf(centerChunk);
            RandomState randomState = state.randomState();
            state.possibleStructureSets()
                    .forEach(
                            set -> {
                                StructurePlacement featurePlacement = set.value().placement();
                                List<StructureSet.StructureSelectionEntry> structures =
                                        set.value().structures();

                                for (StructureSet.StructureSelectionEntry structure : structures) {
                                    StructureStart existingStart =
                                            structureManager.getStartForStructure(
                                                    sectionPos,
                                                    structure.structure().value(),
                                                    centerChunk);
                                    if (existingStart != null && existingStart.isValid()) {
                                        return;
                                    }
                                }

                                if (featurePlacement.isStructureChunk(
                                        state, sourceChunkPos.x(), sourceChunkPos.z())) {
                                    if (structures.size() == 1) {
                                        this.tryGenerateStructure(
                                                structures.get(0),
                                                structureManager,
                                                registryAccess,
                                                randomState,
                                                structureTemplateManager,
                                                state.getLevelSeed(),
                                                centerChunk,
                                                sourceChunkPos,
                                                sectionPos,
                                                level);
                                    } else {
                                        ArrayList<StructureSet.StructureSelectionEntry> options =
                                                new ArrayList<>(structures.size());
                                        options.addAll(structures);
                                        WorldgenRandom random =
                                                new WorldgenRandom(new LegacyRandomSource(0L));
                                        random.setLargeFeatureSeed(
                                                state.getLevelSeed(),
                                                sourceChunkPos.x(),
                                                sourceChunkPos.z());
                                        int total = 0;

                                        for (StructureSet.StructureSelectionEntry option :
                                                options) {
                                            total += option.weight();
                                        }

                                        while (!options.isEmpty()) {
                                            int choice = random.nextInt(total);
                                            int index = 0;

                                            for (StructureSet.StructureSelectionEntry option :
                                                    options) {
                                                choice -= option.weight();
                                                if (choice < 0) {
                                                    break;
                                                }

                                                index++;
                                            }

                                            StructureSet.StructureSelectionEntry selected =
                                                    options.get(index);
                                            if (this.tryGenerateStructure(
                                                    selected,
                                                    structureManager,
                                                    registryAccess,
                                                    randomState,
                                                    structureTemplateManager,
                                                    state.getLevelSeed(),
                                                    centerChunk,
                                                    sourceChunkPos,
                                                    sectionPos,
                                                    level)) {
                                                return;
                                            }

                                            options.remove(index);
                                            total -= selected.weight();
                                        }
                                    }
                                }
                            });
        }
    }

    private boolean tryGenerateStructure(
            StructureSet.StructureSelectionEntry selected,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager structureTemplateManager,
            long seed,
            ChunkAccess centerChunk,
            ChunkPos sourceChunkPos,
            SectionPos sectionPos,
            ResourceKey<Level> level) {
        try (var scope = this.extension.structureScope(selected.structure(), false)) {
            var context =
                    this.extension.generationContext(
                            selected.structure(), centerChunk, randomState, this);
            if (context == null) return false;
            Structure structure = selected.structure().value();
            int references = fetchReferences(structureManager, centerChunk, sectionPos, structure);
            HolderSet<Biome> biomeAllowedForStructure = structure.biomes();
            Predicate<Holder<Biome>> biomePredicate =
                    this.extension.validBiome(
                            selected.structure(),
                            biomeAllowedForStructure::contains,
                            sourceChunkPos);
            StructureStart start =
                    structure.generate(
                            selected.structure(),
                            level,
                            registryAccess,
                            context.generator(),
                            context.generator().getBiomeSource(),
                            randomState,
                            structureTemplateManager,
                            seed,
                            sourceChunkPos,
                            references,
                            context.heightAccessor(),
                            biomePredicate);
            start =
                    this.extension.adaptStart(
                            selected.structure(), start, centerChunk, randomState);
            if (start.isValid()) {
                structureManager.setStartForStructure(sectionPos, structure, start, centerChunk);
                return true;
            } else {
                return false;
            }
        }
    }

    private static int fetchReferences(
            StructureManager structureManager,
            ChunkAccess centerChunk,
            SectionPos sectionPos,
            Structure structure) {
        StructureStart prevEntry =
                structureManager.getStartForStructure(sectionPos, structure, centerChunk);
        return prevEntry != null ? prevEntry.getReferences() : 0;
    }
}
