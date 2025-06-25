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
    private static final ResourceLocation CURSE_FILL = ResourceLocation.fromNamespaceAndPath(MIA.MOD_ID, "textures/gui/icon.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (!MiaConfig.curse) return;
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var dim = player.level().dimension().location();
        var dimensionIds = MIA.CURSE_MANAGER.getDimensionIds();

        if (player == null || mc.options.hideGui) return;

        for (var dims : dimensionIds) {
            if (dim.equals(dims)) {
                if (!MiaConfig.curseGod && MiaPort.isCreativeOrSpectator(player)) return;

                var curse = player.getCapability(MiaCapabilities.CURSE, null);
                if (curse == null) return;

                int value = curse.getCurse();
                int max = curse.getMaxCurse();

                if (max <= 0) return;

                drawCurseOrb(event.getGuiGraphics(), player, value, max);
            }
        }
    }

    private static void drawCurseOrb(GuiGraphics graphics, Player player, int value, int max) {
        var mc = Minecraft.getInstance();
        var screenWidth = graphics.guiWidth();
        var screenHeight = graphics.guiHeight();
        var font = mc.font;

        int ch = MiaPort.isCreativeOrSpectator(player) ? screenHeight - 30 : screenHeight - 43;
        int cx = MiaConfig.curseIcon ? screenWidth / 2 + 100 : screenWidth / 2;
        int cy = MiaConfig.curseIcon ? screenHeight - 11 : ch;

        graphics.blit(CURSE_FILL, cx - 9, cy - 9, 0, 0, 18, 18, 18, 18);

        var text = String.valueOf(value);
        int tx = cx - font.width(text) / 2;
        int ty = cy - font.lineHeight / 2;
        graphics.drawString(font, text, tx, ty, 0xFFFFFF, true);
    }
}
