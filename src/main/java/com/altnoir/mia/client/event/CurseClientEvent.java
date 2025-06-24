package com.altnoir.mia.client.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class CurseClientEvent {
    private static final ResourceLocation CURSE_FILL = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "textures/gui/icon.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || mc.options.hideGui) return;

        if (player.isSpectator() || player.isCreative()) return;

        var curse = player.getCapability(MiaCapabilities.CURSE, null);
        if (curse == null) return;

        int value = curse.getCurse();
        int max = curse.getMaxCurse();

        if (max <= 0) return;

        drawCurseOrb(event.getGuiGraphics(), value, max);
    }

    private static void drawCurseOrb(GuiGraphics graphics, int value, int max) {
        var mc = Minecraft.getInstance();
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        var font = mc.font;

        int cx = screenWidth / 2;
        int cy = screenHeight - 49;

        graphics.blit(CURSE_FILL, cx - 12, cy - 12, 0, 0, 24, 24, 24, 24);

        var text = String.valueOf(value);
        int tx = cx - font.width(text) / 2;
        int ty = cy - font.lineHeight / 2;
        graphics.drawString(font, text, tx, ty, 0xFFFFFF, true);
    }
}
