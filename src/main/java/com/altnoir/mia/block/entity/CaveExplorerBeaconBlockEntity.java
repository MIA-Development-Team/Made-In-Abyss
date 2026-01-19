package com.altnoir.mia.block.entity;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaBlockEntities;
import com.altnoir.mia.init.MiaEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CaveExplorerBeaconBlockEntity extends BlockEntity {
    int levels;
    boolean hasActive = false;

    public CaveExplorerBeaconBlockEntity(BlockPos pos, BlockState blockState) {
        super(MiaBlockEntities.CAVE_EXPLORER_BEACON_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CaveExplorerBeaconBlockEntity blockEntity) {
        if (level.getGameTime() % 80L == 0L) {
            blockEntity.levels = updateBase(level, pos.getX(), pos.getY(), pos.getZ());

            boolean hasLevel = blockEntity.levels > 0;
            if (hasLevel) {
                applyEffects(level, pos, blockEntity.levels);
                playSound(level, pos, SoundEvents.BEACON_AMBIENT);
            }

            boolean hasActive = blockEntity.hasActive;
            if (!hasActive && hasLevel) {
                playSound(level, pos, SoundEvents.BEACON_ACTIVATE);
            } else if (hasActive && !hasLevel) {
                playSound(level, pos, SoundEvents.BEACON_DEACTIVATE);
            }

            blockEntity.hasActive = hasLevel;
        }
    }


    private static int updateBase(Level level, int x, int y, int z) {
        int i = 0;

        for (int j = 1; j <= 4; i = j++) {
            int k = y - j;
            if (k < level.getMinBuildHeight()) {
                break;
            }

            boolean flag = true;

            for (int l = x - j; l <= x + j && flag; l++) {
                for (int i1 = z - j; i1 <= z + j; i1++) {
                    if (!level.getBlockState(new BlockPos(l, k, i1)).is(net.minecraft.tags.BlockTags.BEACON_BASE_BLOCKS)) {
                        flag = false;
                        break;
                    }
                }
            }

            if (!flag) {
                break;
            }
        }

        return i;
    }

    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level level, BlockPos pos, int beaconLevel) {
        if (!level.isClientSide) {
            double horizontalRange = beaconLevel * MiaConfig.caveExplorerBeaconHorizontal + 10;
            double verticalRange = beaconLevel * MiaConfig.caveExplorerBeaconVertical + 5;
            boolean maxVertical = MiaConfig.caveExplorerBeaconMaxVertical;

            AABB aabb = new AABB(pos).inflate(
                    horizontalRange,
                    maxVertical ? level.getMaxBuildHeight() : verticalRange,
                    horizontalRange
            );
            List<Player> players = level.getEntitiesOfClass(Player.class, aabb);

            int duration = (9 + beaconLevel * 2) * 20;

            for (Player player : players) {
                player.addEffect(new MobEffectInstance(MiaEffects.ABYSS_BLESSING, duration, 0, true, true));
            }
        }
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.levels = tag.getInt("Levels");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Levels", this.levels);
    }
}