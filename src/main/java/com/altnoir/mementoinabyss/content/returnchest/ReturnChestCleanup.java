package com.altnoir.mementoinabyss.content.returnchest;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public final class ReturnChestCleanup extends SavedData {
    private static final Codec<ReturnChestCleanup> CODEC =
            Pending.CODEC
                    .listOf()
                    .fieldOf("pending")
                    .xmap(ReturnChestCleanup::new, ReturnChestCleanup::snapshot)
                    .codec();
    private static final SavedDataType<ReturnChestCleanup> TYPE =
            new SavedDataType<>(
                    MementoInAbyss.asResource("return_chest_cleanup"),
                    ReturnChestCleanup::new,
                    CODEC);
    private final Map<BlockPos, UUID> pending = new HashMap<>();

    private ReturnChestCleanup() {}

    private ReturnChestCleanup(List<Pending> entries) {
        for (Pending entry : entries) pending.put(entry.pos(), entry.structure());
    }

    static ReturnChestCleanup get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    boolean isPending(BlockPos pos, UUID structure) {
        return structure.equals(pending.get(pos));
    }

    void queue(BlockPos pos, UUID structure) {
        pending.put(pos, structure);
        setDirty();
    }

    static boolean removePart(ServerLevel level, BlockPos pos) {
        return level.setBlock(
                pos,
                Blocks.AIR.defaultBlockState(),
                removalUpdates(level, pos)
                        | Block.UPDATE_SUPPRESS_DROPS
                        | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
    }

    static int removalUpdates(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.isLoaded(pos.relative(direction))) {
                return Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            }
        }
        return Block.UPDATE_ALL;
    }

    @SubscribeEvent
    public static void cleanLoadedParts(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var data = level.getDataStorage().get(TYPE);
        if (data == null || data.pending.isEmpty()) return;
        var entries = data.pending.entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            BlockPos pos = entry.getKey();
            if (!level.isLoaded(pos)) continue;
            if (level.getBlockEntity(pos) instanceof ReturnChestBlockEntity part
                    && part.matchesStructure(entry.getValue())
                    && !removePart(level, pos)) {
                continue;
            }
            entries.remove();
            data.setDirty();
        }
    }

    private List<Pending> snapshot() {
        return pending.entrySet().stream()
                .map(entry -> new Pending(entry.getKey(), entry.getValue()))
                .toList();
    }

    private record Pending(BlockPos pos, UUID structure) {
        private static final Codec<Pending> CODEC =
                RecordCodecBuilder.create(
                        instance ->
                                instance.group(
                                                BlockPos.CODEC
                                                        .fieldOf("pos")
                                                        .forGetter(Pending::pos),
                                                UUIDUtil.CODEC
                                                        .fieldOf("structure")
                                                        .forGetter(Pending::structure))
                                        .apply(instance, Pending::new));
    }
}
