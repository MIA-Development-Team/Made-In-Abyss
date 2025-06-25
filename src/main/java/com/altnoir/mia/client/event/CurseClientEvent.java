package com.altnoir.mia.client.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaCapabilities;
import com.altnoir.mia.util.MiaPort;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class CurseClientEvent {
    private static final ResourceLocation CURSE_ORB = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "textures/gui/icon.png");
    private static final int FRAME_SIZE = 18;
    private static final int FRAME_COUNT = 22;
    private static final int FRAME_DURATION = 400;

    private static int animationTick = 0;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (!MiaConfig.curse) return;
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var dim = player.level().dimension().location();
        var dimensionIds = MIA.CURSE_MANAGER.getDimensionIds();

        if (mc.options.hideGui) return;

        if (dimensionIds.contains(dim)) {
            if (!MiaConfig.curseGod && MiaPort.isCreativeOrSpectator(player)) return;

            var curse = player.getCapability(MiaCapabilities.CURSE, null);
            if (curse == null) return;

            var value = curse.getCurse();
            var max = curse.getMaxCurse();

            if (max <= 0) return;

            drawCurseOrb(event.getGuiGraphics(), player, value, max);
        }
    }

    private static void drawCurseOrb(GuiGraphics graphics, Player player, int value, int max) {
        var mc = Minecraft.getInstance();
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        var font = mc.font;

        var ch = MiaPort.isCreativeOrSpectator(player) ? screenHeight - 30 : screenHeight - 43;
        var cx = MiaConfig.curseIcon ? screenWidth / 2 + 100 : screenWidth / 2;
        var cy = MiaConfig.curseIcon ? screenHeight - 11 : ch;

        int frame = (animationTick / FRAME_DURATION) % FRAME_COUNT;
        int u = 0;
        int v = frame * FRAME_SIZE;

        graphics.blit(
                CURSE_ORB, cx - 9, cy - 9,
                u, v,
                FRAME_SIZE, FRAME_SIZE,
                FRAME_SIZE, FRAME_SIZE * FRAME_COUNT
        );

        animationTick = (animationTick + 1) % (FRAME_DURATION * FRAME_COUNT);

        var text = String.valueOf(value);
        var tx = cx - font.width(text) / 2;
        var ty = cy - font.lineHeight / 2;
        graphics.drawString(font, text, tx, ty, 0xFFFFFF, true);
    }
}
