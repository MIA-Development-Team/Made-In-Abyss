package com.altnoir.mementoinabyss.impl.event;

import com.altnoir.mementoinabyss.impl.curse.CurseEvent;
import com.altnoir.mementoinabyss.impl.curse.CurseManager;
import com.altnoir.mementoinabyss.impl.strippable.StripEvent;
import com.altnoir.mementoinabyss.impl.tillable.TillEvent;
import com.altnoir.mementoinabyss.impl.rope.minecraft.RopeClimbing;
import com.altnoir.mementoinabyss.content.block.entity.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.init.MiaSoundEvents;
import com.altnoir.mementoinabyss.init.MiaAttributes;
import com.altnoir.mementoinabyss.init.MiaRecipes;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaWorldClocks;
import com.altnoir.mementoinabyss.worldgen.dimension.VerticalDimensionTeleporter;
import com.altnoir.mementoinabyss.worldgen.lod.MiaLodServer;
import com.altnoir.mementoinabyss.worldgen.structure.DelayedCavePillarGenerator;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber
public final class CommonEvents {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(MiaRecipes.ARTIFACT_ENHANCEMENT_TYPE.get());
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.getEntity().level().isClientSide() || event.isCriticalHit()) return;
        double chance = event.getEntity().getAttributeValue(MiaAttributes.CRITICAL_HIT);
        if (chance <= 0.0 || event.getEntity().getRandom().nextDouble() >= Math.min(chance, 1.0)) return;

        double multiplier = event.getEntity().getAttributeValue(MiaAttributes.CRITICAL_HIT_DAMAGE)
                + Math.max(0.0, chance - 1.0);
        event.setCriticalHit(true);
        event.setDamageMultiplier((float) multiplier);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var input = player.getLastClientInput();
            RopeClimbing.tick(player, RopeConnectorBlockEntity.serverInstances(),
                    input.jump(), input.shift());
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        CurseEvent.onEntityTick(event);
    }

    @SubscribeEvent
    public static void onNewRegistry(DataPackRegistryEvent.NewRegistry event) {
        CurseEvent.onNewRegistry(event);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        CurseManager.init(event);
        MiaWorldClocks.initialize(event.getServer());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CurseEvent.onClone(event);
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        TillEvent.onRightClickBlock(event);
        StripEvent.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        DelayedCavePillarGenerator.onChunkLoad(event);
        if (event.getLevel() instanceof ServerLevel level) {
            LevelChunk chunk = event.getChunk();
            MiaLodServer.captureIfNeeded(level, chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelChunk chunk = event.getChunk();
            MiaLodServer.captureIfNeeded(level, chunk);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        VerticalDimensionTeleporter.tick(event.getServer());
        DelayedCavePillarGenerator.onServerTick(event);
        MiaLodServer.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DelayedCavePillarGenerator.clearPending();
        MiaLodServer.stop();
        VerticalDimensionTeleporter.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) MiaLodServer.onPlayerJoined(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getTo().equals(MiaDimensions.THE_ABYSS_LEVEL)) {
                player.connection.send(new ClientboundSoundPacket(
                        MiaSoundEvents.ABYSS_PORTAL_TRAVEL,
                        SoundSource.AMBIENT,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        0.25F,
                        player.getRandom().nextFloat() * 0.4F + 0.8F,
                        player.getRandom().nextLong()
                ));
            }
            MiaLodServer.onPlayerChangedDimension(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MiaLodServer.onPlayerLeft(player);
            VerticalDimensionTeleporter.remove(player);
        }
    }

    private CommonEvents() {}
}
