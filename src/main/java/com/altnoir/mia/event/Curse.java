package com.altnoir.mia.event;

import com.altnoir.mia.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
        if (!Config.curse) return;
        Player player = event.getEntity();
        ResourceLocation dim = player.level().dimension().location();
        List<CurseConfig.EffectConfig> configs = CurseConfig.getEffects(dim);
        if (!configs.isEmpty()) {
            UUID uuid = player.getUUID();
            double playerY = player.getY();
            double minY = playerMinY.get(uuid);
            double deltaY = playerY - minY;

            if (!playerMinY.containsKey(uuid)) {
                playerMinY.put(uuid, playerY);
                return;
            }

            if (playerY < minY) {
                playerMinY.put(uuid, playerY);
                return;
            }

            if (deltaY >= CURSE_TRIGGER_HEIGHT) {
                for (CurseConfig.EffectConfig config : configs) {
                    ResourceLocation effectId = ResourceLocation.tryParse(config.id);
                    if (effectId == null) continue;
                    Optional<Holder.Reference<MobEffect>> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(effectId);
                    effectHolder.ifPresent(effects -> player.addEffect(new MobEffectInstance(
                            effects,
                            config.duration,
                            config.amplifier,
                            false,
                            true
                    )));
                }
                playerMinY.put(uuid, playerY);
            }
        }
            /*if (player.tickCount % 40 == 0) {
                MIA.LOGGER.info("MinY: {} | DeltaY: {} | Dim: {}", minY, deltaY, dim);
            }*/
    }
}