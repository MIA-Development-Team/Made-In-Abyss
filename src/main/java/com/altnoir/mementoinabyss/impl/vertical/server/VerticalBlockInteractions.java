package com.altnoir.mementoinabyss.impl.vertical.server;

import com.altnoir.mementoinabyss.impl.lod.server.CrossDimensionLodLinks;
import com.altnoir.mementoinabyss.impl.vertical.VerticalRaytrace;
import com.altnoir.mementoinabyss.impl.vertical.network.VerticalBlockActionPayload;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class VerticalBlockInteractions {
    private static final Map<UUID, Mining> MINING = new HashMap<>();
    private static final Map<UUID, Long> LAST_USE = new HashMap<>();

    public static void handle(ServerPlayer player, VerticalBlockActionPayload p) {
        if (p.action() == VerticalBlockActionPayload.Action.ABORT_BREAK) {
            MINING.remove(player.getUUID());
            return;
        }
        var hit = validate(player, p.source(), p.pos());
        if (hit == null) return;
        ServerLevel target = player.level().getServer().getLevel(p.source());
        if (target == null) return;
        if (p.action() == VerticalBlockActionPayload.Action.USE_BLOCK) {
            long tick = player.level().getServer().getTickCount();
            if (tick - LAST_USE.getOrDefault(player.getUUID(), -100L) < 4) return;
            LAST_USE.put(player.getUUID(), tick);
            var item = player.getMainHandItem();
            if (!(item.getItem() instanceof BlockItem blockItem)
                    || blockItem.getBlock() instanceof EntityBlock
                    || target.getBlockEntity(p.pos()) != null) return;
            if (player.getCooldowns().isOnCooldown(item)) return;
            var link =
                    CrossDimensionLodLinks.forTargets(player.level()).stream()
                            .filter(value -> value.source().equals(p.source()))
                            .findFirst()
                            .orElseThrow();
            var context = new UseOnContext(target, player, InteractionHand.MAIN_HAND, item, hit);
            BlockPos placement = new BlockPlaceContext(context).getClickedPos();
            if (!target.isInsideBuildHeight(placement)) {
                BlockPos mapped = placement.offset(0, link.displayYOffset(), 0);
                var current = player.level();
                if (!current.isInsideBuildHeight(mapped)
                        || !current.getWorldBorder().isWithinBounds(mapped)
                        || !current.mayInteract(player, mapped)
                        || current.getChunkSource()
                                        .getChunkNow(
                                                Math.floorDiv(mapped.getX(), 16),
                                                Math.floorDiv(mapped.getZ(), 16))
                                == null) return;
                // Treat the destination's replaceable cell as the click target. This preserves
                // normal item hooks and placement checks without fabricating an out-of-height
                // block.
                var mappedHit =
                        new BlockHitResult(
                                hit.getLocation().add(0, link.displayYOffset(), 0),
                                hit.getDirection(),
                                mapped,
                                hit.isInside());
                context =
                        new UseOnContext(
                                current, player, InteractionHand.MAIN_HAND, item, mappedHit);
            }
            VerticalPlacementSounds.use(
                    player,
                    context,
                    context.getLevel() == player.level() ? 0 : link.displayYOffset());
        } else {
            var previous = MINING.get(player.getUUID());
            if (previous == null
                    || !previous.source.equals(p.source())
                    || !previous.pos.equals(p.pos()))
                MINING.put(
                        player.getUUID(),
                        new Mining(
                                p.source(),
                                p.pos().immutable(),
                                player.level().getServer().getTickCount(),
                                Block.getId(target.getBlockState(p.pos()))));
            else previous.lastRequest = player.level().getServer().getTickCount();
        }
    }

    private static @Nullable BlockHitResult validate(
            ServerPlayer player, ResourceKey<Level> source, BlockPos pos) {
        if (!player.isAlive()
                || player.isSpectator()
                || !MiaDimensions.isMiaDimension(player.level().dimension())) return null;
        var target = player.level().getServer().getLevel(source);
        if (target == null
                || !MiaDimensions.isMiaDimension(source)
                || !target.isInsideBuildHeight(pos)
                || !target.getWorldBorder().isWithinBounds(pos)
                || !target.mayInteract(player, pos)) return null;
        var link =
                CrossDimensionLodLinks.forTargets(player.level()).stream()
                        .filter(value -> value.source().equals(source))
                        .findFirst();
        if (link.isEmpty()) return null;
        if (Math.abs((long) Math.floorDiv(pos.getX(), 16) - player.chunkPosition().x()) > 1
                || Math.abs((long) Math.floorDiv(pos.getZ(), 16) - player.chunkPosition().z()) > 1)
            return null;
        // The short ray may touch adjacent columns. Reject unloaded neighbors before tracing.
        for (int x = player.chunkPosition().x() - 1; x <= player.chunkPosition().x() + 1; x++)
            for (int z = player.chunkPosition().z() - 1; z <= player.chunkPosition().z() + 1; z++)
                if (target.getChunkSource().getChunkNow(x, z) == null) return null;
        double reach = Math.min(16, player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE));
        var hit = VerticalRaytrace.trace(player.level(), target, link.get(), player, reach);
        return hit != null && hit.getBlockPos().equals(pos) ? hit : null;
    }

    public static void tick(MinecraftServer server) {
        var iterator = MINING.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var mining = entry.getValue();
            var player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null
                    || server.getTickCount() - mining.lastRequest > 12
                    || validate(player, mining.source, mining.pos) == null) {
                iterator.remove();
                continue;
            }
            var target = server.getLevel(mining.source);
            if (target == null) {
                iterator.remove();
                continue;
            }
            var block = target.getBlockState(mining.pos);
            if (Block.getId(block) != mining.state) {
                iterator.remove();
                continue;
            }
            if (target.getBlockEntity(mining.pos) != null) {
                iterator.remove();
                continue;
            }
            mining.progress +=
                    player.isCreative() ? 1 : block.getDestroyProgress(player, target, mining.pos);
            if (mining.progress < 1) continue;
            // Only the game mode's synchronous world context changes. The player stays in its
            // tracked world; vanilla destruction handles drops, tool wear and NeoForge break hooks.
            ServerLevel original = player.level();
            try {
                player.gameMode.setLevel(target);
                player.gameMode.destroyBlock(mining.pos);
            } finally {
                player.gameMode.setLevel(original);
            }
            iterator.remove();
        }
        LAST_USE.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
    }

    public static void clear() {
        MINING.clear();
        LAST_USE.clear();
    }

    private static final class Mining {
        final ResourceKey<Level> source;
        final BlockPos pos;
        final int state;
        int lastRequest;
        float progress;

        Mining(ResourceKey<Level> source, BlockPos pos, int tick, int state) {
            this.source = source;
            this.pos = pos;
            this.lastRequest = tick;
            this.state = state;
        }
    }

    private VerticalBlockInteractions() {}
}
