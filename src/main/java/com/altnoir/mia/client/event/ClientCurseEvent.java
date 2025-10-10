package com.altnoir.mia.client.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaClientConfig;
import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.init.MiaCapabilities;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
public class ClientCurseEvent {
    public static final String GUI_DISCONNECT = "gui.mia.disconnect";
    private static final ResourceLocation CURSE_ORB = MiaUtil.id(MIA.MOD_ID, "textures/gui/icon.png");
    private static final int FRAME_SIZE = 18;
    private static final int FRAME_COUNT = 22;
    private static final int FRAME_DURATION = 1000;

    private static boolean firstFrame = false;
    private static int animationTick = 0;

    public static void ScreenEventInitPost(ScreenEvent.Init.Post event) {
        if (MiaConfig.disconnectButtonState == MiaConfig.DisconnectButtonState.DEFAULT) return;

        if (event.getScreen() instanceof PauseScreen pauseScreen) {
            Button disconnectButton = pauseScreen.disconnectButton;

            if (disconnectButton != null) {
                if (MiaConfig.disconnectButtonState == MiaConfig.DisconnectButtonState.HIDDEN) {
                    disconnectButton.visible = false;
                } else {
                    disconnectButton.active = false;
                    disconnectButton.setMessage(Component.translatable("gui.mia.disconnect"));
                }
            }
        }

    }

    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (!MiaConfig.curse) return;
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var dim = player.level().dimension().location();
        var dimensionIds = MIA.CURSE_MANAGER.getDimensionIds();

        if (mc.options.hideGui || MiaClientConfig.curseIconPosition == MiaClientConfig.CurseIconPosition.HIDDEN) return;

        if (dimensionIds.contains(dim)) {
            if (!MiaConfig.curseGod && MiaUtil.isCreativeOrSpectator(player)) return;

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

        var ch = MiaUtil.isCreativeOrSpectator(player) ? screenHeight - 30 : screenHeight - 43;
        int cx, cy;
        switch (MiaClientConfig.curseIconPosition) {
            case MIDDLE:
                cx = screenWidth / 2;
                cy = ch;
                break;
            case RIGHT:
                cx = screenWidth / 2 + 100;
                cy = screenHeight - 11;
                break;
            default:
                return;
        }

        int frame;

        if (value == 0) {
            frame = 0;
            animationTick = 0;
        } else {
            int dynamicFrameDelay = Math.max(1, FRAME_DURATION - (value * 100));
            frame = (animationTick / dynamicFrameDelay) % FRAME_COUNT;
        }

        int u = 0;
        int v = frame * FRAME_SIZE;

        graphics.blit(
                CURSE_ORB, cx - FRAME_SIZE / 2, cy - FRAME_SIZE / 2,
                u, v,
                FRAME_SIZE, FRAME_SIZE,
                FRAME_SIZE, FRAME_SIZE * FRAME_COUNT
        );

        if (value > 0) {
            animationTick = (animationTick + 1) % (FRAME_DURATION * FRAME_COUNT);
        }


        var text = String.valueOf(value);
        var tx = cx - font.width(text) / 2;
        var ty = cy - font.lineHeight / 2;
        graphics.drawString(font, text, tx, ty, 0xEEEEEE, true);
    }
}
