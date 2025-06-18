package com.altnoir.mia.init.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class Curse {
    private static final HashMap<UUID, Double> playerMinY = new HashMap<>();
    private static final int CURSE_TRIGGER_HEIGHT = 10;

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();

        double playerY = player.getY();
        playerMinY.put(uuid, playerY);

    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!MiaConfig.curse) return;
        var player = event.getEntity();
        var dim = player.level().dimension().location();
        var configs = MIA.CURSE_MANAGER.getEffects(dim);
        if (!player.level().isClientSide() && !configs.isEmpty()) {
            var uuid = player.getUUID();
            var playerY = player.getY();
            var minY = playerMinY.computeIfAbsent(uuid, k -> playerY);
            double deltaY = playerY - minY;

            if (playerY < minY) {
                playerMinY.put(uuid, playerY);
                return;
            }

            if (deltaY >= CURSE_TRIGGER_HEIGHT) {
                for (var config : configs) {
                    var effectId = config.effect().location();
                    var effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
                    effectHolder.ifPresent(effects -> player.addEffect(new MobEffectInstance(
                            effects,
                            config.duration(),
                            config.amplifier(),
                            false,
                            true
                    )));
                }
                playerMinY.put(uuid, playerY);
            }
        }
    }
}