package com.altnoir.mementoinabyss.impl.vertical.client;

import com.altnoir.mementoinabyss.impl.vertical.network.VerticalBlockActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public final class VerticalClientInteraction {
    private static VerticalNearTerrain.RemoteHit mining;
    private static int ticks;

    @SubscribeEvent
    public static void interact(InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        var hit = VerticalNearTerrain.pick();
        if (hit == null) return;
        if (event.isAttack()) {
            event.setCanceled(true);
            event.setSwingHand(true);
            mining = hit;
            send(hit, VerticalBlockActionPayload.Action.START_BREAK);
        } else if (event.isUseItem()
                && event.getHand() == InteractionHand.MAIN_HAND
                && mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            event.setCanceled(true);
            event.setSwingHand(true);
            send(hit, VerticalBlockActionPayload.Action.USE_BLOCK);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        VerticalNearTerrain.holdAtPendingBoundary();
        ticks++;
        if (mining == null) return;
        var mc = Minecraft.getInstance();
        var hit = VerticalNearTerrain.pick();
        if (mc.player == null
                || !mc.options.keyAttack.isDown()
                || hit == null
                || !hit.source().equals(mining.source())
                || !hit.hit().getBlockPos().equals(mining.hit().getBlockPos())) {
            if (mc.getConnection() != null)
                send(mining, VerticalBlockActionPayload.Action.ABORT_BREAK);
            mining = null;
        } else if (ticks % 5 == 0) {
            send(mining, VerticalBlockActionPayload.Action.START_BREAK);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void send(
            VerticalNearTerrain.RemoteHit hit, VerticalBlockActionPayload.Action action) {
        ClientPacketDistributor.sendToServer(
                new VerticalBlockActionPayload(hit.source(), hit.hit().getBlockPos(), action));
    }

    private VerticalClientInteraction() {}
}
