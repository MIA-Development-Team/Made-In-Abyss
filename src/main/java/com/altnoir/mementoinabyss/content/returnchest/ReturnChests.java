package com.altnoir.mementoinabyss.content.returnchest;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class ReturnChests {
    private static final int BREAK_PARTICLES = 16;
    private static final double BREAK_SPREAD = 0.22;
    private static final double BREAK_SPEED = 0.15;

    public static ReturnChestPart part(BlockState state) {
        return state.getValue(ReturnChestBlock.PART);
    }

    public static boolean isCore(BlockState state) {
        return part(state).isCore();
    }

    public static int tint(BlockState state) {
        return state.getValue(ReturnChestBlock.TINT);
    }

    public static boolean isBound(BlockState state) {
        return tint(state) != ReturnChestBlock.UNBOUND;
    }

    public static boolean hasTintOverlay(int tint) {
        return tint != 0 && tint != ReturnChestBlock.UNBOUND;
    }

    public static int tintColor(BlockState state) {
        int tint = tint(state);
        return hasTintOverlay(tint) ? ReturnChestColors.tint(DyeColor.byId(tint - 1)) : -1;
    }

    public static int connection(DyeColor color) {
        return color.getId() + 1;
    }

    public static DyeColor color(int connection) {
        return DyeColor.byId(connection - 1);
    }

    public static @Nullable ReturnChestBlockEntity core(Level level, BlockPos center) {
        if (!level.isLoaded(center)) return null;
        return level.getBlockEntity(center) instanceof ReturnChestBlockEntity chest ? chest : null;
    }

    public static @Nullable ReturnChestBlockEntity core(
            Level level, BlockPos center, UUID structure) {
        ReturnChestBlockEntity chest = core(level, center);
        return chest != null && chest.belongsTo(center, structure) ? chest : null;
    }

    public static void bind(ServerLevel level, BlockPos center, DyeColor color) {
        ReturnChestBlockEntity chest = core(level, center);
        if (chest != null) bind(level, chest, color);
    }

    public static void bind(ServerLevel level, ReturnChestBlockEntity chest, DyeColor color) {
        chest.bind(level, connection(color));
    }

    public static void activate(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BEACON_ACTIVATE);
    }

    public static void deactivate(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.BEACON_DEACTIVATE);
    }

    private static void play(Level level, BlockPos pos, SoundEvent sound) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void breakParticles(ServerLevel level, BlockPos pos, BlockState state) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state, pos),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                BREAK_PARTICLES,
                BREAK_SPREAD,
                BREAK_SPREAD,
                BREAK_SPREAD,
                BREAK_SPEED);
    }

    public static void message(Player player, String key, Object... arguments) {
        player.sendOverlayMessage(
                Component.translatable("message.mementoinabyss.return_chest." + key, arguments));
    }

    private ReturnChests() {}
}
