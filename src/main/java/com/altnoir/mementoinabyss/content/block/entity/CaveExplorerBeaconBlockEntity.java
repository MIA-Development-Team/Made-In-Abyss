package com.altnoir.mementoinabyss.content.block.entity;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaEffects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public final class CaveExplorerBeaconBlockEntity extends BlockEntity implements BeaconBeamOwner {
    private List<BeaconBeamOwner.Section> beamSections = Lists.newArrayList();
    private List<BeaconBeamOwner.Section> checkingBeamSections = Lists.newArrayList();
    private int levels;
    private int lastCheckY = Integer.MIN_VALUE;

    public CaveExplorerBeaconBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(
            Level level, BlockPos pos, BlockState state, CaveExplorerBeaconBlockEntity beacon) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos checkPos;
        if (beacon.lastCheckY < y) {
            checkPos = pos;
            beacon.checkingBeamSections = Lists.newArrayList();
            beacon.lastCheckY = y - 1;
        } else {
            checkPos = new BlockPos(x, beacon.lastCheckY + 1, z);
        }

        BeaconBeamOwner.Section lastSection =
                beacon.checkingBeamSections.isEmpty()
                        ? null
                        : beacon.checkingBeamSections.getLast();
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        for (int i = 0; i < 10 && checkPos.getY() <= surface; i++) {
            BlockState checked = level.getBlockState(checkPos);
            Integer color = checked.getBeaconColorMultiplier(level, checkPos, pos);
            if (color != null) {
                if (beacon.checkingBeamSections.size() <= 1) {
                    lastSection = new BeaconBeamOwner.Section(color);
                    beacon.checkingBeamSections.add(lastSection);
                } else if (lastSection != null) {
                    if (color == lastSection.getColor()) {
                        lastSection.increaseHeight();
                    } else {
                        lastSection =
                                new BeaconBeamOwner.Section(
                                        ARGB.average(lastSection.getColor(), color));
                        beacon.checkingBeamSections.add(lastSection);
                    }
                }
            } else if (lastSection == null
                    || checked.getLightDampening() >= 15 && !checked.is(Blocks.BEDROCK)) {
                beacon.checkingBeamSections.clear();
                beacon.lastCheckY = surface;
                break;
            } else {
                lastSection.increaseHeight();
            }

            checkPos = checkPos.above();
            beacon.lastCheckY++;
        }

        int previousLevels = beacon.levels;
        if (level.getGameTime() % 80L == 0L && !beacon.beamSections.isEmpty()) {
            beacon.levels = updateBase(level, x, y, z);
            if (beacon.levels > 0) {
                applyEffects(level, pos, beacon.levels);
                playSound(level, pos, SoundEvents.BEACON_AMBIENT);
            }
        }

        if (beacon.lastCheckY >= surface) {
            beacon.lastCheckY = level.getMinY() - 1;
            boolean wasActive = previousLevels > 0;
            beacon.beamSections = beacon.checkingBeamSections;
            if (!level.isClientSide()) {
                boolean active = beacon.levels > 0 && !beacon.beamSections.isEmpty();
                if (!wasActive && active) {
                    playSound(level, pos, SoundEvents.BEACON_ACTIVATE);
                } else if (wasActive && !active) {
                    playSound(level, pos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level level, int x, int y, int z) {
        int levels = 0;
        for (int tier = 1; tier <= 4; levels = tier++) {
            int layerY = y - tier;
            if (layerY < level.getMinY()) {
                break;
            }

            boolean complete = true;
            for (int offsetX = x - tier; offsetX <= x + tier && complete; offsetX++) {
                for (int offsetZ = z - tier; offsetZ <= z + tier; offsetZ++) {
                    if (!level.getBlockState(new BlockPos(offsetX, layerY, offsetZ))
                            .is(BlockTags.BEACON_BASE_BLOCKS)) {
                        complete = false;
                        break;
                    }
                }
            }
            if (!complete) {
                break;
            }
        }
        return levels;
    }

    private static void applyEffects(Level level, BlockPos pos, int beaconLevel) {
        if (level.isClientSide()) {
            return;
        }

        var config = MementoInAbyss.CONFIGS.gamePlaySection;
        double horizontal = beaconLevel * config.caveExplorerBeaconHorizontal.get() + 10;
        double vertical =
                config.caveExplorerBeaconMaxVertical.get()
                        ? level.getMaxY()
                        : beaconLevel * config.caveExplorerBeaconVertical.get() + 5;
        AABB range = new AABB(pos).inflate(horizontal, vertical, horizontal);
        int duration = (9 + beaconLevel * 2) * 20;
        for (Player player : level.getEntitiesOfClass(Player.class, range)) {
            player.addEffect(
                    new MobEffectInstance(MiaEffects.ABYSS_BLESSING, duration, 0, true, true));
        }
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        if (!level.isClientSide()) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public List<BeaconBeamOwner.Section> getBeamSections() {
        return levels == 0 ? ImmutableList.of() : beamSections;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        levels = input.getIntOr("Levels", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Levels", levels);
    }
}
