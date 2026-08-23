package com.altnoir.mia.worldgen.structure.wall;

import com.altnoir.mia.init.worldgen.MiaStructurePlacementTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public final class AbyssWallStructurePlacement extends StructurePlacement {
    public static final MapCodec<AbyssWallStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance)
                    .and(AbyssWallPlanConfig.CODEC.fieldOf("plan").forGetter(AbyssWallStructurePlacement::plan))
                    .apply(instance, AbyssWallStructurePlacement::new)
    );

    private final AbyssWallPlanConfig plan;

    private AbyssWallStructurePlacement(
            Vec3i locateOffset,
            FrequencyReductionMethod frequencyReductionMethod,
            float frequency,
            int salt,
            Optional<ExclusionZone> exclusionZone,
            AbyssWallPlanConfig plan
    ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.plan = plan;
    }

    public AbyssWallStructurePlacement(AbyssWallPlanConfig plan) {
        this(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, plan.salt(), Optional.empty(), plan);
    }

    public AbyssWallPlanConfig plan() {
        return this.plan;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return AbyssWallPlanner.candidateForChunk(
                structureState.getLevelSeed(),
                structureState.randomState(),
                this.plan,
                new ChunkPos(x, z)
        ) != null;
    }

    @Override
    public StructurePlacementType<?> type() {
        return MiaStructurePlacementTypes.ABYSS_WALL.get();
    }
}
