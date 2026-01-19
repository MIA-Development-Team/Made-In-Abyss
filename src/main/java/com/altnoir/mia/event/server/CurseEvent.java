package com.altnoir.mia.event.server;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaAttachments;
import com.altnoir.mia.init.MiaCapabilities;
import com.altnoir.mia.network.client.CurseCapabilityPayload;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.UUID;

public class CurseEvent {
    private static final HashMap<UUID, Double> playerMinY = new HashMap<>();

    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        var uuid = event.getEntity().getUUID();
        playerMinY.put(uuid, event.getEntity().getY());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!MiaConfig.curse) return;

        var player = event.getEntity();
        if (player.level().isClientSide()) return;

        var cap = player.getCapability(MiaCapabilities.CURSE, null);
        if (cap == null) return;

        var uuid = player.getUUID();
        var currentY = player.getY();
        double lastY = playerMinY.computeIfAbsent(uuid, k -> currentY);

        var delta = Math.max(currentY - lastY, 0);
        var isCurseGod = !MiaConfig.curseGod && MiaUtil.isCreativeOrSpectator(player);

        if (currentY < lastY || isCurseGod || (player instanceof ServerPlayer serverPlayer && serverPlayer.isSleepingLongEnough())) {
            playerMinY.put(uuid, currentY);
        }
        if (isCurseGod) return;

        var dim = player.level().dimension().location();
        var dimensionIds = MIA.CURSE_MANAGER.getDimensionIds();

        if (dimensionIds.contains(dim)) {
            cap.setCurse((int) Math.floor(delta));

            if (delta > cap.getMaxCurse()) {
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

    public static void attachEntityCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                MiaCapabilities.CURSE,
                EntityType.PLAYER,
                (entity, side) -> entity.getData(MiaAttachments.CURSE)
        );
    }

    /**
     * 设置玩家的最小Y坐标
     * @param playerUUID 玩家UUID
     * @param minY 最小Y坐标
     */
    public static void setPlayerMinY(UUID playerUUID, double minY) {
        playerMinY.put(playerUUID, minY);
    }
}