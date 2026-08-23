package com.altnoir.mia.worldgen.structure.wall;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.worldgen.MiaStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public final class AbyssWindmillStructure extends Structure {
    public static final MapCodec<AbyssWindmillStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("straight_pool").forGetter(AbyssWindmillStructure::straightPool),
            StructureTemplatePool.CODEC.fieldOf("tilt_pool").forGetter(AbyssWindmillStructure::tiltPool),
            ResourceLocation.CODEC.fieldOf("anchor_name").forGetter(AbyssWindmillStructure::anchorName),
            AbyssWallPlanConfig.CODEC.fieldOf("plan").forGetter(AbyssWindmillStructure::plan)
    ).apply(instance, AbyssWindmillStructure::new));

    private final Holder<StructureTemplatePool> straightPool;
    private final Holder<StructureTemplatePool> tiltPool;
    private final ResourceLocation anchorName;
    private final AbyssWallPlanConfig plan;

    public AbyssWindmillStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> straightPool,
            Holder<StructureTemplatePool> tiltPool,
            ResourceLocation anchorName,
            AbyssWallPlanConfig plan
    ) {
        super(settings);
        this.straightPool = straightPool;
        this.tiltPool = tiltPool;
        this.anchorName = anchorName;
        this.plan = plan;
    }

    private Holder<StructureTemplatePool> straightPool() {
        return this.straightPool;
    }

    private Holder<StructureTemplatePool> tiltPool() {
        return this.tiltPool;
    }

    private ResourceLocation anchorName() {
        return this.anchorName;
    }

    private AbyssWallPlanConfig plan() {
        return this.plan;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!(context.chunkGenerator() instanceof NoiseBasedChunkGenerator noiseGenerator)) {
            return Optional.empty();
        }

        AbyssWallCandidate candidate = AbyssWallPlanner.candidateForChunk(
                context.seed(), context.randomState(), this.plan, context.chunkPos()
        );
        if (candidate == null) {
            MIA.LOGGER.warn("Abyss windmill placement/structure plans disagree at start chunk {}", context.chunkPos());
            return Optional.empty();
        }

        OptionalInt refinedRadius = refineRadius(context, noiseGenerator, candidate);
        if (refinedRadius.isEmpty()) {
            return Optional.empty();
        }

        double anchorRadius = refinedRadius.getAsInt()
                + AbyssWallPlanner.wallAnchorOffset(context.seed(), candidate, this.plan);
        BlockPos wallAnchor = AbyssWallPlanner.blockPos(candidate.angle(), candidate.y(), anchorRadius);
        Holder<StructureTemplatePool> selectedPool = candidate.orientation().templateKind() == AbyssWallCandidate.TemplateKind.STRAIGHT
                ? this.straightPool
                : this.tiltPool;
        StructurePoolElement element = selectedPool.value().getRandomTemplate(context.random());
        if (element == EmptyPoolElement.INSTANCE) {
            MIA.LOGGER.warn("Abyss windmill pool {} is empty", selectedPool.unwrapKey().orElse(null));
            return Optional.empty();
        }

        List<StructureTemplate.StructureBlockInfo> anchors = element.getShuffledJigsawBlocks(
                        context.structureTemplateManager(), BlockPos.ZERO, candidate.orientation().rotation(), context.random())
                .stream()
                .filter(info -> info.nbt() != null && this.anchorName.toString().equals(info.nbt().getString("name")))
                .toList();
        if (anchors.size() != 1) {
            MIA.LOGGER.warn("Abyss windmill template must contain exactly one anchor named {}, found {}", this.anchorName, anchors.size());
            return Optional.empty();
        }

        BlockPos templateOrigin = wallAnchor.subtract(anchors.getFirst().pos());
        BoundingBox boundingBox = element.getBoundingBox(
                context.structureTemplateManager(), templateOrigin, candidate.orientation().rotation()
        );
        PoolElementStructurePiece piece = new PoolElementStructurePiece(
                context.structureTemplateManager(),
                element,
                templateOrigin,
                element.getGroundLevelDelta(),
                candidate.orientation().rotation(),
                boundingBox,
                LiquidSettings.IGNORE_WATERLOGGING
        );
        return Optional.of(new GenerationStub(wallAnchor, builder -> builder.addPiece(piece)));
    }

    private OptionalInt refineRadius(
            GenerationContext context,
            NoiseBasedChunkGenerator generator,
            AbyssWallCandidate candidate
    ) {
        Map<Integer, Boolean> solidity = new HashMap<>();
        return AbyssWallPlanner.findRefinedBoundary(candidate.predictedRadius(), this.plan.maxCorrection(), radius ->
                solidity.computeIfAbsent(radius, ignored -> {
                    BlockPos pos = AbyssWallPlanner.blockPos(candidate.angle(), candidate.y(), radius);
                    NoiseColumn column = generator.getBaseColumn(
                            pos.getX(), pos.getZ(), context.heightAccessor(), context.randomState()
                    );
                    BlockState state = column.getBlock(candidate.y());
                    return !state.isAir() && state.getFluidState().isEmpty();
                })
        );
    }

    @Override
    public StructureType<?> type() {
        return MiaStructureTypes.ABYSS_WALL_TEMPLATE.get();
    }
}
