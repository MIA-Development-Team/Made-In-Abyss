package com.altnoir.mia.init.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaCapabilities;
import com.altnoir.mia.network.CurseCapabilityPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CurseEvent {
    private static final HashMap<UUID, Double> playerMinY = new HashMap<>();

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        var uuid = event.getEntity().getUUID();
        playerMinY.put(uuid, event.getEntity().getY());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!MiaConfig.curse) return;

        var player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.isSpectator() || player.isCreative()) return;

        var cap = player.getCapability(MiaCapabilities.CURSE, null);
        if (cap == null) return;

        var uuid = player.getUUID();
        var currentY = player.getY();
        double lastY = playerMinY.computeIfAbsent(uuid, k -> currentY);

        var delta = currentY - lastY;

        if (currentY < lastY) {
            playerMinY.put(uuid, currentY);
        }

        cap.setCurse((int) Math.floor(delta));

        if (delta > cap.getMaxCurse()) {
            var dim = player.level().dimension().location();
            var curseEffects = MIA.CURSE_MANAGER.getEffects(dim);

            for (var curseEffect : curseEffects) {
                var effectId = curseEffect.effect().location();
                var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
                effectHolder.ifPresent(effects -> player.addEffect(new MobEffectInstance(
                        effects,
                        curseEffect.duration(),
                        curseEffect.amplifier(),
                        false,
                        true
                )));
            }
            playerMinY.put(uuid, currentY);
        }

        PacketDistributor.sendToPlayer((ServerPlayer) player, new CurseCapabilityPayload(cap.getCurse(), cap.getMaxCurse()));
    }
}