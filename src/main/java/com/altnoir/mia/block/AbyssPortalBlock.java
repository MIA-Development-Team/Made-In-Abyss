package com.altnoir.mia.block;

import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
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
    public MapCodec<AbyssPortalBlock> codec() {return CODEC;}


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)
                && Shapes.joinIsNotEmpty(
                Shapes.create(entity.getBoundingBox().move((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()))),
                state.getShape(level, pos),
                BooleanOp.AND
        )) {
            if (!level.isClientSide && level.dimension() == Level.OVERWORLD && entity instanceof ServerPlayer serverplayer && !serverplayer.seenCredits) {
                serverplayer.showEndCredits(); // 片尾字幕
                return;
            }

            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> resourcekey = level.dimension() == MiaDimensions.ABYSS_BRINK_LEVEL ? Level.OVERWORLD : MiaDimensions.ABYSS_BRINK_LEVEL;
        ServerLevel serverlevel = level.getServer().getLevel(resourcekey);
        if (serverlevel == null) {
            return null;
        } else {
            boolean flag = resourcekey == MiaDimensions.ABYSS_BRINK_LEVEL;
            BlockPos blockpos = flag ? new BlockPos(0, 300, 0) : serverlevel.getSharedSpawnPos();
            Vec3 vec3 = blockpos.getBottomCenter();
            float f = entity.getYRot();
            if (flag) {
                EndPlatformFeature.createEndPlatform(serverlevel, BlockPos.containing(vec3).below(), true);
                f = Direction.WEST.toYRot();
                if (entity instanceof ServerPlayer) {
                    vec3 = vec3.subtract(0.0, 1.0, 0.0);
                }
            } else {
                if (entity instanceof ServerPlayer serverplayer) {
                    return serverplayer.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
                }

                vec3 = entity.adjustSpawnLocation(serverlevel, blockpos).getBottomCenter();
            }

            return new DimensionTransition(
                    serverlevel,
                    vec3,
                    entity.getDeltaMovement(),
                    f,
                    entity.getXRot(),
                    DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET)
            );
        }
    }
}
