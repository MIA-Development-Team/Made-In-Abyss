package com.altnoir.mia.client;

import com.altnoir.mia.MiaConfig;
import com.altnoir.mia.client.gui.screens.ArtifactSmithingTableScreen;
import com.altnoir.mia.client.gui.screens.inventory.tooltip.ClientArtifactBundleTooltip;
import com.altnoir.mia.client.handler.HookHandler;
import com.altnoir.mia.client.renderer.TheAbyssDimEffects;
import com.altnoir.mia.client.renderer.TheAbyssFogRenderer;
import com.altnoir.mia.common.component.ArtifactBundleInventoryComponent;
import com.altnoir.mia.compat.Mods;
import com.altnoir.mia.compat.ponder.MiaPonderRegistry;
import com.altnoir.mia.core.event.client.*;
import com.altnoir.mia.core.event.common.AbyssMobEvent;
import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaKeyBinding;
import com.altnoir.mia.init.MiaMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class MiaClientEvents {
    // 模组事件-注册事件
    public static void modLoad(final ModConfigEvent event) {
        MiaClientConfig.onLoad(event.getConfig());
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        Mods.PONDER.executeIfInstalled(() -> MiaPonderRegistry::register);
        registerBlockRenderLayers();
    }

    /**
     * 太初菌系的方块渲染层。
     * <p>
     * {@code glow_primo_fungus} 的模型是"菌柄不透明 + 菌伞半透明"的**两个模型叠在一起**的 multipart：
     * 部件各自的 {@code render_type} 由模型自己声明，NeoForge 会把它们**并集**成
     * {@code [solid, translucent]}，模型这条路径本来就是对的。
     * <p>
     * 但 {@code ItemBlockRenderTypes.getRenderLayers(...)} 这条<b>旧路径</b>不看模型，
     * 只查 {@code BLOCK_RENDER_TYPES}，没注册过就一律当成 {@code solid} —— 而部分区块渲染器
     * （例如 Sodium）走的正是这条。那样整个方块只会进 solid 通道，菌伞的 6 个面拿到 0 个 quad，
     * 表现就是"菌伞的半透明没了 / 菌伞不出现"。
     * <p>
     * 所以这里<b>显式把渲染层注册成模型真实的并集</b>：两条路径结果一致，且不改变任何 JSON。
     * 注意必须在 {@code FMLClientSetupEvent} 里**直接调用**（不能放进 {@code event.enqueueWork}）——
     * {@code setRenderLayer} 内部会检查 {@code ClientModLoader.isLoading()}。
     */
    @SuppressWarnings("deprecation")
    private static void registerBlockRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(
                MiaBlocks.GLOW_PRIMO_FUNGUS.get(),
                ChunkRenderTypeSet.of(RenderType.solid(), RenderType.translucent()));
        ItemBlockRenderTypes.setRenderLayer(
                MiaBlocks.GLOW_PRIMO_CAP.get(), ChunkRenderTypeSet.of(RenderType.translucent()));
    }

    public static void registerParticles(RegisterParticleProvidersEvent event) {
        RegisterParticlesEvent.register(event);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        RegisterEntityRendererEvent.register(event);
    }

    public static void registerTooltipComponentFactories(
            RegisterClientTooltipComponentFactoriesEvent event) {
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
        if (MC.level == null || MC.player == null) {
            TheAbyssFogRenderer.clearCache();
        }
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

    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!MiaConfig.abyssMobLevelSwitch) return;
        var result = AbyssMobEvent.onRenderMobLevel(event.getEntity());
        if (result != null) {
            event.setCanRender(result);
        }
    }

    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        event.setYaw(-event.getYaw());
        event.setPitch(-event.getPitch());
        event.setRoll(event.getRoll() + 180.0F);
    }
}
