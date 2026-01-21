package com.altnoir.mia.client;

import com.altnoir.mia.client.gui.screens.ArtifactSmithingTableScreen;
import com.altnoir.mia.client.gui.screens.inventory.tooltip.ClientArtifactBundleTooltip;
import com.altnoir.mia.client.handler.HookHandler;
import com.altnoir.mia.client.renderer.TheAbyssDimEffects;
import com.altnoir.mia.common.component.ArtifactBundleInventoryComponent;
import com.altnoir.mia.event.client.*;
import com.altnoir.mia.init.MiaKeyBinding;
import com.altnoir.mia.init.MiaMenus;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class MiaClientEvents {
    // 模组事件-注册事件
    public static void modLoad(final ModConfigEvent event) {
        MiaClientConfig.onLoad(event.getConfig());
    }

    public static void registerParticles(RegisterParticleProvidersEvent event) {
        RegisterParticlesEvent.register(event);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        RegisterEntityRendererEvent.register(event);
    }

    public static void registerTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ArtifactBundleInventoryComponent.class, ClientArtifactBundleTooltip::new);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MiaMenus.ARTIFACT_ENHANCEMENT_TABLE.get(), ArtifactSmithingTableScreen::new);
    }

    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ClientDimEffects.THE_ABYSS_EFFECTS, new TheAbyssDimEffects());
    }

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(MiaKeyBinding.SKILL_DIAL);
    }

    // 游戏事件-持续事件
    public static void onClientTick(ClientTickEvent.Post event) {
        final Minecraft MC = Minecraft.getInstance();
        if (MC.level == null || MC.player == null) return;

        KeyArrowEvent.onClientTick();
        HookHandler.handler(MC.player, MC.level, MC.options.keyJump.consumeClick());
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        KeyArrowEvent.onRenderGui(event.getGuiGraphics());
        CompassOverlayEvent.onRenderGui(event.getGuiGraphics(), event.getPartialTick());
    }

    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        ClientCurseEvent.onRenderOverlay(event.getGuiGraphics());
    }

    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        ClientCurseEvent.ScreenInitPost(event.getScreen());
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ClientTooltipEvent.onTooltip(event.getItemStack(), event.getToolTip());
    }
}


