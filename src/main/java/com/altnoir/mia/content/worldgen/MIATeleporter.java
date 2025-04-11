/*
package com.altnoir.mia.content.worldgen.portal;

import com.altnoir.mia.BlocksRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.util.ITeleporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class MIATeleporter implements ITeleporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MIATeleporter.class);
    private final BlockPos targetPos;
    private final boolean leavingDimension;

    public MIATeleporter(BlockPos pos, boolean leavingDim) {
        this.targetPos = pos.immutable();
        this.leavingDimension = leavingDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        // 高度计算
        int targetY = destinationWorld.getHeight(Heightmap.Types.WORLD_SURFACE, targetPos.getX(), targetPos.getZ());
        if (!leavingDimension) {
            targetY = Math.max(targetPos.getY(), destinationWorld.getMinBuildHeight() + 5);
        }

        // 位置搜索
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos(
                targetPos.getX(),
                Mth.clamp(targetY, destinationWorld.getMinBuildHeight(), destinationWorld.getMaxBuildHeight()),
                targetPos.getZ()
        );

        for (int attempt = 0; attempt < 16; attempt++) {
            if (isSafeSpawnPosition(destinationWorld, searchPos)) {
                break;
            }
            searchPos.move(Direction.UP);
        }

        BlockPos destinationPos = searchPos.immutable();
        entity.setPos(destinationPos.getX() + 0.5D, destinationPos.getY(), destinationPos.getZ() + 0.5D);

        try {
            generatePortalFrame(destinationWorld, destinationPos);
        } catch (Exception e) {
            LOGGER.error("Failed to generate MIA portal at {}", destinationPos, e);
            // 备用位置生成（使用世界出生点）
            BlockPos spawnPoint = destinationWorld.getSharedSpawnPos();
            entity.setPos(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }

        return entity;
    }

    // 安全位置检测（兼容1.20.1）
    private boolean isSafeSpawnPosition(ServerLevel world, BlockPos.MutableBlockPos pos) {
        BlockState currentBlock = world.getBlockState(pos);
        BlockState belowBlock = world.getBlockState(pos.below());
        FluidState fluidState = belowBlock.getFluidState();

        return currentBlock.isAir()
                && world.getBlockState(pos.above()).isAir()
                && !belowBlock.isAir()
                && fluidState.isEmpty(); // 替换getMaterial().isLiquid()
    }

    // 兼容1.20.1的传送门生成方法
    private void generatePortalFrame(ServerLevel level, BlockPos center) {
        BlockState portalFrame = Blocks.OBSIDIAN.defaultBlockState();
        BlockState portalBlock = BlocksRegister.EXAMPLE_PORTAL_BLOCK.get().defaultBlockState();

        // 生成3x3门框（手动实现）
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 3; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if ((x == 0 && z == 0) || y == 0 || y == 3) {
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, portalFrame, 3);
                        }
                    }
                }
            }
        }

        // 填充传送门方块
        for (int y = 1; y <= 2; y++) {
            BlockPos pos = center.above(y);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, portalBlock, 3);
            }
        }
    }
}
*/
