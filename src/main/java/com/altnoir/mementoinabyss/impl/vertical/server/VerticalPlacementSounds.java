package com.altnoir.mementoinabyss.impl.vertical.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

@EventBusSubscriber
public final class VerticalPlacementSounds {
    private static final ThreadLocal<Capture> ACTIVE = new ThreadLocal<>();

    public static void use(ServerPlayer player, UseOnContext context, int offset) {
        ServerLevel viewer = player.level();
        Capture previous = ACTIVE.get();
        Capture capture = new Capture(context.getLevel(), new ArrayList<>());
        InteractionResult result;
        ACTIVE.set(capture);
        try {
            result = context.getItemInHand().useOn(context);
        } finally {
            if (previous == null) ACTIVE.remove();
            else ACTIVE.set(previous);
        }
        if (!result.consumesAction()) return;
        for (var sound : capture.sounds) {
            Vec3 pos = sound.pos.add(0, offset, 0);
            var packet =
                    new ClientboundSoundPacket(
                            sound.sound,
                            sound.source,
                            pos.x,
                            pos.y,
                            pos.z,
                            sound.volume,
                            sound.pitch,
                            ThreadLocalRandom.current().nextLong());
            // BlockItem excludes the actor from its server broadcast because ordinary placement
            // predicts sound locally. Only the remote-only request needs this missing copy.
            if (context.getLevel() == viewer) {
                if (player.level() == viewer) player.connection.send(packet);
            } else {
                viewer.getServer()
                        .getPlayerList()
                        .broadcast(
                                null,
                                pos.x,
                                pos.y,
                                pos.z,
                                sound.sound.value().getRange(sound.volume),
                                viewer.dimension(),
                                packet);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void capture(PlayLevelSoundEvent.AtPosition event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        Capture capture = ACTIVE.get();
        var sound = event.getSound();
        if (capture != null
                && capture.level == event.getLevel()
                && sound != null
                && capture.sounds.size() < 16)
            capture.sounds.add(
                    new Sound(
                            sound,
                            event.getSource(),
                            event.getPosition(),
                            event.getNewVolume(),
                            event.getNewPitch()));
    }

    private record Capture(Level level, List<Sound> sounds) {}

    private record Sound(
            Holder<SoundEvent> sound, SoundSource source, Vec3 pos, float volume, float pitch) {}

    private VerticalPlacementSounds() {}
}
