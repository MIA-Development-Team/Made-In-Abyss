package com.altnoir.mementoinabyss.content.block.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class GreenParticleLeavesBlock extends LeavesBlock {
    private static final int LEAF_PARTICLE_COLOR = 0xFF5FAF4A;

    public static final MapCodec<GreenParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.floatRange(0.0F, 1.0F)
                            .fieldOf("leaf_particle_chance")
                            .forGetter(block -> block.leafParticleChance),
                    propertiesCodec()
            ).apply(instance, GreenParticleLeavesBlock::new)
    );

    public GreenParticleLeavesBlock(float leafParticleChance, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, LEAF_PARTICLE_COLOR);
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }

    @Override
    public MapCodec<? extends GreenParticleLeavesBlock> codec() {
        return CODEC;
    }
}
