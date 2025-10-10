package com.altnoir.mia.portal;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class MiaPortalShape {
    private static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 21;
    private static final BlockState FRAME = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
    private final LevelAccessor level;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int length;
    private final int width;

    public static Optional<MiaPortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos bottomLeft) {
        return findPortalShape(level, bottomLeft, p -> p.isValid() && p.numPortalBlocks == 0);
    }

    public static Optional<MiaPortalShape> findPortalShape(LevelAccessor level, BlockPos bottomLeft, Predicate<MiaPortalShape> predicate) {
        return Optional.of(new MiaPortalShape(level, bottomLeft)).filter(predicate);
    }

    public MiaPortalShape(LevelAccessor level, BlockPos bottomLeft) {
        this.level = level;
        this.bottomLeft = this.calculateBottomLeft(bottomLeft);
        if (this.bottomLeft == null) {
            this.bottomLeft = bottomLeft;
            this.width = 1;
            this.length = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.length = this.calculateLength();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minY = Math.max(this.level.getMinBuildHeight(), pos.getY() - 21);

        // 向下找到实体方块
        while (pos.getY() > minY && isEmpty(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        // 检查是否是有效的水平传送门起始点
        if (!isEmpty(this.level.getBlockState(pos))) {
            return null;
        }

        // 找到X轴方向的左边界
        int leftOffset = 0;
        BlockPos testPos = pos.west();
        while (leftOffset < 21 && this.level.getBlockState(testPos).is(FRAME.getBlock())) {
            leftOffset++;
            testPos = testPos.west();
        }

        // 找到Z轴方向的前边界
        int frontOffset = 0;
        testPos = pos.north();
        while (frontOffset < 21 && this.level.getBlockState(testPos).is(FRAME.getBlock())) {
            frontOffset++;
            testPos = testPos.north();
        }

        return pos.offset(-leftOffset, 0, -frontOffset);
    }

    private int calculateWidth() {
        if (this.bottomLeft == null) return 0;

        int width = 0;
        BlockPos testPos = this.bottomLeft.east();

        // 向东计算宽度直到遇到框架
        while (width < 21) {
            if (this.level.getBlockState(testPos).is(FRAME.getBlock())) {
                break;
            }
            width++;
            testPos = testPos.east();
        }

        return width >= 1 && width <= 21 ? width : 0;
    }

    private int calculateLength() {
        if (this.bottomLeft == null) return 0;

        int length = 0;
        BlockPos testPos = this.bottomLeft.south();

        // 向南计算长度直到遇到框架
        while (length < 21) {
            if (this.level.getBlockState(testPos).is(FRAME.getBlock())) {
                break;
            }
            length++;
            testPos = testPos.south();
        }

        // 验证四周是否被框架包围
        if (length >= 1 && length <= 21) {
            // 检查前后面框架
            for (int x = 0; x < this.width; x++) {
                BlockPos frontPos = this.bottomLeft.offset(x, 0, -1);
                BlockPos backPos = this.bottomLeft.offset(x, 0, length);

                if (!this.level.getBlockState(frontPos).is(FRAME.getBlock()) ||
                        !this.level.getBlockState(backPos).is(FRAME.getBlock())) {
                    return 0;
                }
            }

            // 检查左右面框架
            for (int z = 0; z < length; z++) {
                BlockPos leftPos = this.bottomLeft.offset(-1, 0, z);
                BlockPos rightPos = this.bottomLeft.offset(this.width, 0, z);

                if (!this.level.getBlockState(leftPos).is(FRAME.getBlock()) ||
                        !this.level.getBlockState(rightPos).is(FRAME.getBlock())) {
                    return 0;
                }
            }

            // 计算传送门方块数量
            for (int x = 0; x < this.width; x++) {
                for (int z = 0; z < length; z++) {
                    BlockPos pos = this.bottomLeft.offset(x, 0, z);
                    BlockState state = this.level.getBlockState(pos);
                    if (state.is(MiaBlocks.ABYSS_PORTAL.get())) {
                        this.numPortalBlocks++;
                    }
                }
            }

            return length;
        }

        return 0;
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.is(MiaBlocks.ABYSS_PORTAL.get());
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= 1 && this.width <= 21 && this.length >= 1 && this.length <= 21;
    }

    public void createPortalBlocks() {
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.offset(this.width - 1, 0, this.length - 1))
                .forEach(pos -> this.level.setBlock(pos, MiaBlocks.ABYSS_PORTAL.get().defaultBlockState(), 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.length;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Vec3 pos, EntityDimensions entityDimensions) {
        double widthDiff = (double) foundRectangle.axis1Size - (double) entityDimensions.width();
        double lengthDiff = (double) foundRectangle.axis2Size - (double) entityDimensions.height();
        BlockPos corner = foundRectangle.minCorner;

        double relativeX;
        if (widthDiff > 0.0) {
            double centerX = (double) corner.getX() + (double) entityDimensions.width() / 2.0;
            relativeX = Mth.clamp(Mth.inverseLerp(pos.x() - centerX, 0.0, widthDiff), 0.0, 1.0);
        } else {
            relativeX = 0.5;
        }

        double relativeZ;
        if (lengthDiff > 0.0) {
            double centerZ = (double) corner.getZ() + (double) entityDimensions.height() / 2.0;
            relativeZ = Mth.clamp(Mth.inverseLerp(pos.z() - centerZ, 0.0, lengthDiff), 0.0, 1.0);
        } else {
            relativeZ = 0.5;
        }

        double relativeY = pos.y() - (double) corner.getY();
        return new Vec3(relativeX, relativeY, relativeZ);
    }

    public static Vec3 findCollisionFreePosition(Vec3 pos, ServerLevel level, Entity entity, EntityDimensions dimensions) {
        if (!(dimensions.width() > 4.0F) && !(dimensions.height() > 4.0F)) {
            double halfHeight = (double) dimensions.height() / 2.0;
            Vec3 centerPos = pos.add(0.0, halfHeight, 0.0);
            VoxelShape voxelShape = Shapes.create(
                    AABB.ofSize(centerPos, (double) dimensions.width(), 0.0, (double) dimensions.width()).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6)
            );
            Optional<Vec3> freePos = level.findFreePosition(
                    entity, voxelShape, centerPos, (double) dimensions.width(), (double) dimensions.height(), (double) dimensions.width()
            );
            Optional<Vec3> adjustedPos = freePos.map(p -> p.subtract(0.0, halfHeight, 0.0));
            return adjustedPos.orElse(pos);
        } else {
            return pos;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public BlockPos getBottomLeft() {
        return bottomLeft;
    }
}