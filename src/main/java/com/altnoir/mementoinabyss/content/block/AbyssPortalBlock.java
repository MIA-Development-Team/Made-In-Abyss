package com.altnoir.mementoinabyss.content.block;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaSoundEvents;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class AbyssPortalBlock extends Block implements Portal {
    public static final MapCodec<AbyssPortalBlock> CODEC = simpleCodec(AbyssPortalBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0, 1.0, 0.0, 16.0, 15.0, 16.0);

    public AbyssPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AbyssPortalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (entity.canUsePortal(false)
                && Shapes.joinIsNotEmpty(
                Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())),
                state.getShape(level, pos), BooleanOp.AND)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos portalEntryPos) {
        boolean enteringAbyss = level.dimension() != MiaDimensions.THE_ABYSS_LEVEL;
        ServerLevel destination = level.getServer().getLevel(
                enteringAbyss ? MiaDimensions.THE_ABYSS_LEVEL : Level.OVERWORLD);
        if (destination == null) {
            return null;
        }

        if (!enteringAbyss && entity instanceof ServerPlayer player) {
            return player.findRespawnPositionAndUseSpawnBlock(false, AbyssPortalBlock::afterTeleport);
        }

        BlockPos target = enteringAbyss
                ? findSuitablePosition(destination, nearestAbyssPosition(entity.getX(), entity.getZ()))
                : destination.getRespawnData().pos();
        Vec3 position = entity.adjustSpawnLocation(destination, target).getBottomCenter();
        return new TeleportTransition(destination, position, entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot(),
                AbyssPortalBlock::afterTeleport);
    }

    private static void afterTeleport(Entity entity) {
        entity.placePortalTicket(BlockPos.containing(entity.position()));
        if (entity instanceof ServerPlayer player && player.level().dimension() == Level.OVERWORLD) {
            player.level().playSound(null, player.blockPosition(), MiaSoundEvents.ABYSS_PORTAL_TRAVEL.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static BlockPos nearestAbyssPosition(double x, double z) {
        double angle = Math.atan2(z, x);
        int radius = MementoInAbyss.CONFIGS.worldGenSection.abyssRadius.get() * 2;
        return new BlockPos(
                (int) (Math.cos(angle) * radius),
                MiaHeight.THE_ABYSS.maxY() - 1,
                (int) (Math.sin(angle) * radius));
    }

    private static BlockPos findSuitablePosition(ServerLevel level, BlockPos center) {
        int startY = Math.min(MiaHeight.THE_ABYSS.maxY() - 28, level.getMaxY() - 2);
        int endY = Math.max(startY - 128, level.getMinY() + 1);
        for (int radius = 0; radius <= 8; radius++) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int dx = (int) (Math.cos(radians) * radius * 2);
                int dz = (int) (Math.sin(radians) * radius * 2);
                for (int y = startY; y >= endY; y--) {
                    BlockPos feet = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (level.getBlockState(feet).isAir()
                            && level.getBlockState(feet.above()).isAir()
                            && !level.getBlockState(feet.below()).isAir()) {
                        return feet;
                    }
                }
            }
        }
        return new BlockPos(center.getX(), startY, center.getZ());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            float[] pitches = {0.4F, 0.6F, 1.0F, 1.2F, 1.5F};
            level.playLocalSound(pos, MiaSoundEvents.ABYSS_PORTAL_AMBIENT.get(), SoundSource.BLOCKS,
                    0.5F, pitches[random.nextInt(pitches.length)], false);
        }
        for (int i = 0; i < 4; i++) {
            level.addParticle(ParticleTypes.GLOW,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    (random.nextFloat() - 0.5) * 0.5,
                    (random.nextFloat() - 0.5) * 0.5,
                    (random.nextFloat() - 0.5) * 0.5);
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }
}
