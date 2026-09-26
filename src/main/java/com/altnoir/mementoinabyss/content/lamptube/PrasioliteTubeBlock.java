package com.altnoir.mementoinabyss.content.lamptube;

import com.mojang.serialization.MapCodec;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class PrasioliteTubeBlock extends CrystalTubeBlock {
    public static final MapCodec<PrasioliteTubeBlock> CODEC = simpleCodec(PrasioliteTubeBlock::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 4);

    public PrasioliteTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PrasioliteTubeBlock> codec() {
        return CODEC;
    }

    @Override
    protected IntegerProperty levelProperty() {
        return LEVEL;
    }

    @Override
    protected int maxLevel() {
        return 4;
    }

    @Override
    protected int beamColor() {
        return 0x80FF80;
    }

    @Override
    protected boolean affect(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockPos targetPos,
            BlockState targetState,
            int distance) {
        if (targetState.getBlock() instanceof CrystalTubeBlock) {
            return relay(level, pos, state, targetPos, targetState, distance);
        }
        if (!targetState.isAir()) {
            return growOn(level, pos, state, targetPos);
        }
        return false;
    }

    private boolean growOn(Level level, BlockPos pos, BlockState state, BlockPos targetPos) {
        int size = state.getValue(LEVEL) - 1;
        AtomicBoolean grown = new AtomicBoolean(false);
        BlockPos.betweenClosedStream(
                        targetPos.offset(-size, -1, -size), targetPos.offset(size, 0, size))
                .forEach(
                        cropPos -> {
                            boolean applied =
                                    BoneMealItem.applyBonemeal(
                                                    ItemStack.EMPTY, level, cropPos, null)
                                            || BoneMealItem.growWaterPlant(
                                                    ItemStack.EMPTY, level, cropPos, null);
                            if (applied) {
                                level.levelEvent(1505, cropPos, 7);
                                grown.set(true);
                            }
                        });
        if (!grown.get()) {
            return false;
        }
        beam(level, pos, targetPos, state);
        return true;
    }
}
