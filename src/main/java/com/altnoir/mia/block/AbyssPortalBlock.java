package com.altnoir.mia.block;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaSounds;
import com.altnoir.mia.worldgen.MiaHeight;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AbyssPortalBlock extends Block implements Portal {
    public static final MapCodec<AbyssPortalBlock> CODEC = simpleCodec(AbyssPortalBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 1.0, 0.0, 16.0, 15.0, 16.0);

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
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false) &&
                Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())), state.getShape(level, pos), BooleanOp.AND)
        ) {
//            if (!level.isClientSide && level.dimension() == Level.OVERWORLD && entity instanceof ServerPlayer serverplayer && !serverplayer.seenCredits) {
//                serverplayer.showEndCredits(); // 片尾字幕
//                return;
//            }

            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> resourcekey = level.dimension() == MiaDimensions.THE_ABYSS_LEVEL ? Level.OVERWORLD : MiaDimensions.THE_ABYSS_LEVEL;
        ServerLevel serverlevel = level.getServer().getLevel(resourcekey);
        if (serverlevel == null) {
            return null;
        } else {
            boolean flag = resourcekey == MiaDimensions.THE_ABYSS_LEVEL;
            BlockPos blockpos;
            Vec3 vec3;
            float f = entity.getYRot();
            if (flag) {
                //AbyssPortalFeature.createAbyssBrinkPlatform(serverlevel, BlockPos.containing(vec3).below(), true);
                blockpos = nearestAbyssPosition(entity.getX(), entity.getZ());
                BlockPos suitablePos = findSuitablePosition(serverlevel, blockpos);

                vec3 = suitablePos.getBottomCenter();
                f = Direction.WEST.toYRot();
                if (entity instanceof ServerPlayer) {
                    vec3 = vec3.subtract(0.0, 1.0, 0.0);
                }
            } else {
                if (entity instanceof ServerPlayer serverplayer) {
                    return serverplayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
                }
                blockpos = serverlevel.getSharedSpawnPos();
                vec3 = entity.adjustSpawnLocation(serverlevel, blockpos).getBottomCenter();
            }

            DimensionTransition.PostDimensionTransition portalSound = playerEntity -> {
                if (playerEntity instanceof ServerPlayer serverplayer) {
                    serverplayer.playNotifySound(MiaSounds.ABYSS_PORTAL_TRAVEL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            };

            return new DimensionTransition(
                    serverlevel,
                    vec3,
                    entity.getDeltaMovement(),
                    f,
                    entity.getXRot(),
                    portalSound.then(DimensionTransition.PLACE_PORTAL_TICKET)
            );
        }
    }

    /**
     * 计算距离原点指定半径的圆上最接近给定坐标的位置
     *
     * @param x 目标X坐标
     * @param z 目标Z坐标
     * @return 圆周上的最近位置
     */
    private BlockPos nearestAbyssPosition(double x, double z) {
        double angle = Math.atan2(z, x);

        // 根据进入的坐标计算圆周上的坐标
        int radius = MiaConfig.abyssRadius * 2 + 32;
        int targetX = (int) (Math.cos(angle) * radius);
        int targetZ = (int) (Math.sin(angle) * radius);

        return new BlockPos(targetX, MiaHeight.THE_ABYSS.maxY(), targetZ);
    }

    /**
     * 从指定高度开始向下搜索合适的位置
     *
     * @param level     目标世界
     * @param centerPos 中心位置
     * @return 适合站立的位置
     */
    private BlockPos findSuitablePosition(ServerLevel level, BlockPos centerPos) {
        int endY = Math.max(MiaHeight.THE_ABYSS.maxY() - 128, level.getMinBuildHeight());
        int startY = MiaHeight.THE_ABYSS.maxY() - 28;

        int maxRadius = 8;
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                int dx = (int) (Math.cos(rad) * radius * 2);
                int dz = (int) (Math.sin(rad) * radius * 2);

                BlockPos searchCenter = centerPos.offset(dx, 0, dz);

                // 从上到下搜索，找到第一个合适位置就返回
                for (int y = startY; y >= endY; y--) {
                    BlockPos feetPos = new BlockPos(searchCenter.getX(), y, searchCenter.getZ());
                    if (isSafePosition(level, feetPos)) {
                        return feetPos;
                    }
                }
            }
        }
        return centerPos;
    }

    private boolean isSafePosition(ServerLevel level, BlockPos feetPos) {
        BlockPos belowPos = feetPos.below();
        BlockPos headPos = feetPos.above(1);

        boolean hasSpace = level.getBlockState(feetPos).isAir() && level.getBlockState(headPos).isAir();
        boolean hasSupport = !level.getBlockState(belowPos).isAir();

        return hasSpace && hasSupport;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            float[] pitchValues = {0.4F, 0.6F, 1.0F, 1.2F, 1.5F};
            float pitch = pitchValues[random.nextInt(pitchValues.length)];

            level.playLocalSound(
                    (double) pos.getX() + 0.5,
                    (double) pos.getY() + 0.5,
                    (double) pos.getZ() + 0.5,
                    MiaSounds.ABYSS_PORTAL_AMBIENT.get(),
                    SoundSource.BLOCKS,
                    0.5F,
                    pitch,
                    false
            );
        }

        for (int i = 0; i < 4; i++) {
            double d0 = (double) pos.getX() + random.nextDouble();
            double d1 = (double) pos.getY() + random.nextDouble();
            double d2 = (double) pos.getZ() + random.nextDouble();
            double d3 = ((double) random.nextFloat() - 0.5) * 0.5;
            double d4 = ((double) random.nextFloat() - 0.5) * 0.5;
            double d5 = ((double) random.nextFloat() - 0.5) * 0.5;
            int j = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                d0 = (double) pos.getX() + 0.5 + 0.25 * (double) j;
                d3 = (double) (random.nextFloat() * 2.0F * (float) j);
            } else {
                d2 = (double) pos.getZ() + 0.5 + 0.25 * (double) j;
                d5 = (double) (random.nextFloat() * 2.0F * (float) j);
            }

            level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }
}
