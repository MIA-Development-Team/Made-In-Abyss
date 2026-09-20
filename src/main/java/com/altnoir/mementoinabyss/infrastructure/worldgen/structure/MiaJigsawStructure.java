package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.init.MiaStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class MiaJigsawStructure extends Structure {
    private static final int MAX_RANGE = 256;
    private static final Codec<JigsawStructure.MaxDistance> MAX_DISTANCE_CODEC =
            Codec.intRange(1, MAX_RANGE)
                    .xmap(
                            JigsawStructure.MaxDistance::new,
                            JigsawStructure.MaxDistance::horizontal);

    public static final MapCodec<MiaJigsawStructure> CODEC =
            RecordCodecBuilder.<MiaJigsawStructure>mapCodec(
                            instance ->
                                    instance.group(
                                                    settingsCodec(instance),
                                                    StructureTemplatePool.CODEC
                                                            .fieldOf("start_pool")
                                                            .forGetter(s -> s.startPool),
                                                    Identifier.CODEC
                                                            .optionalFieldOf("start_jigsaw_name")
                                                            .forGetter(s -> s.startJigsawName),
                                                    Codec.intRange(0, 20)
                                                            .fieldOf("size")
                                                            .forGetter(s -> s.maxDepth),
                                                    HeightProvider.CODEC
                                                            .fieldOf("start_height")
                                                            .forGetter(s -> s.startHeight),
                                                    Codec.BOOL
                                                            .fieldOf("use_expansion_hack")
                                                            .forGetter(s -> s.useExpansionHack),
                                                    Heightmap.Types.CODEC
                                                            .optionalFieldOf(
                                                                    "project_start_to_heightmap")
                                                            .forGetter(
                                                                    s -> s.projectStartToHeightmap),
                                                    CaveFloorSearch.CODEC
                                                            .optionalFieldOf("cave_floor_search")
                                                            .forGetter(s -> s.caveFloorSearch),
                                                    VerticalAnchor.CODEC
                                                            .optionalFieldOf(
                                                                    "max_start_y_exclusive")
                                                            .forGetter(s -> s.maxStartYExclusive),
                                                    MAX_DISTANCE_CODEC
                                                            .fieldOf("max_distance_from_center")
                                                            .forGetter(s -> s.maxDistance),
                                                    Codec.list(PoolAliasBinding.CODEC)
                                                            .optionalFieldOf(
                                                                    "pool_aliases", List.of())
                                                            .forGetter(s -> s.poolAliases),
                                                    DimensionPadding.CODEC
                                                            .optionalFieldOf(
                                                                    "dimension_padding",
                                                                    DimensionPadding.ZERO)
                                                            .forGetter(s -> s.dimensionPadding),
                                                    LiquidSettings.CODEC
                                                            .optionalFieldOf(
                                                                    "liquid_settings",
                                                                    LiquidSettings
                                                                            .APPLY_WATERLOGGING)
                                                            .forGetter(s -> s.liquidSettings))
                                            .apply(instance, MiaJigsawStructure::new))
                    .validate(MiaJigsawStructure::verifyRange);

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final Optional<CaveFloorSearch> caveFloorSearch;
    private final Optional<VerticalAnchor> maxStartYExclusive;
    private final JigsawStructure.MaxDistance maxDistance;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public MiaJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<Identifier> startJigsawName,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            Optional<CaveFloorSearch> caveFloorSearch,
            Optional<VerticalAnchor> maxStartYExclusive,
            JigsawStructure.MaxDistance maxDistance,
            List<PoolAliasBinding> poolAliases,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.caveFloorSearch = caveFloorSearch;
        this.maxStartYExclusive = maxStartYExclusive;
        this.maxDistance = maxDistance;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    public MiaJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<Identifier> startJigsawName,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            Optional<CaveFloorSearch> caveFloorSearch,
            JigsawStructure.MaxDistance maxDistance,
            List<PoolAliasBinding> poolAliases,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings) {
        this(
                settings,
                startPool,
                startJigsawName,
                maxDepth,
                startHeight,
                useExpansionHack,
                projectStartToHeightmap,
                caveFloorSearch,
                Optional.empty(),
                maxDistance,
                poolAliases,
                dimensionPadding,
                liquidSettings);
    }

    public MiaJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<Identifier> startJigsawName,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            JigsawStructure.MaxDistance maxDistance,
            List<PoolAliasBinding> poolAliases,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings) {
        this(
                settings,
                startPool,
                startJigsawName,
                maxDepth,
                startHeight,
                useExpansionHack,
                projectStartToHeightmap,
                Optional.empty(),
                Optional.empty(),
                maxDistance,
                poolAliases,
                dimensionPadding,
                liquidSettings);
    }

    private static DataResult<MiaJigsawStructure> verifyRange(MiaJigsawStructure structure) {
        int terrainMargin =
                structure.terrainAdaptation()
                                == net.minecraft.world.level.levelgen.structure.TerrainAdjustment
                                        .NONE
                        ? 0
                        : 12;
        return structure.maxDistance.horizontal() + terrainMargin > MAX_RANGE
                ? DataResult.error(
                        () ->
                                "Horizontal structure size including terrain adaptation must not"
                                        + " exceed "
                                        + MAX_RANGE)
                : DataResult.success(structure);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        WorldGenerationContext heightContext =
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        int y = startHeight.sample(context.random(), heightContext);
        Optional<BlockPos> start =
                caveFloorSearch.isPresent()
                        ? caveFloorSearch.get().findStart(context, y)
                        : Optional.of(new BlockPos(chunk.getMinBlockX(), y, chunk.getMinBlockZ()));
        if (start.isEmpty()) {
            return Optional.empty();
        }
        BlockPos blockPos = start.get();
        Optional<GenerationStub> generation =
                JigsawPlacement.addPieces(
                        context,
                        startPool,
                        startJigsawName,
                        maxDepth,
                        blockPos,
                        useExpansionHack,
                        projectStartToHeightmap,
                        maxDistance,
                        PoolAliasLookup.create(poolAliases, blockPos, context.seed()),
                        dimensionPadding,
                        liquidSettings);
        if (maxStartYExclusive.isEmpty()) {
            return generation;
        }
        int maxYExclusive = maxStartYExclusive.get().resolveY(heightContext);
        return generation.filter(stub -> stub.position().getY() < maxYExclusive);
    }

    @Override
    public StructureType<?> type() {
        return MiaStructureTypes.JIGSAW.get();
    }
}
