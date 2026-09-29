package com.mementoinabyss.recall.client;

import com.mementoinabyss.recall.RecallConfig;
import com.mementoinabyss.recall.client.scene.SceneReloadListener;
import com.mementoinabyss.recall.client.scene.SceneRenderState;
import com.mementoinabyss.recall.client.scene.SceneRenderer;
import com.mementoinabyss.recall.data.GuideReloadListener;
import com.mementoinabyss.recall.data.GuideRepository;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

/** The client-only integration boundary for resource loading, input and scene rendering. */
public final class RecallClient {
    private static boolean initialized;
    private static Identifier defaultGuide;

    public static void initialize(IEventBus modBus, RecallConfig config) {
        if (initialized) throw new IllegalStateException("Recall is already initialized");
        initialized = true;
        defaultGuide = config.defaultGuide();
        var category = new KeyMapping.Category(config.keyCategory());
        var key =
                new KeyMapping(
                        config.keyTranslation(),
                        KeyConflictContext.IN_GAME,
                        InputConstants.Type.KEYSYM,
                        config.defaultKey(),
                        category);
        var itemKey =
                new KeyMapping(
                        "key.recall.open_item_guide",
                        KeyConflictContext.GUI,
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_R,
                        category);
        new RecallItemTooltip(itemKey, config.defaultGuide());
        modBus.addListener(
                (RegisterKeyMappingsEvent event) -> {
                    event.registerCategory(category);
                    event.register(key);
                    event.register(itemKey);
                });
        modBus.addListener(
                (AddClientReloadListenersEvent event) -> {
                    event.addListener(
                            Identifier.fromNamespaceAndPath("recall", "guides"),
                            new GuideReloadListener(config.resourceRoot()));
                    event.addListener(
                            Identifier.fromNamespaceAndPath("recall", "scenes"),
                            new SceneReloadListener(config.resourceRoot()));
                });
        modBus.addListener(
                (RegisterPictureInPictureRenderersEvent event) ->
                        event.register(SceneRenderState.class, SceneRenderer::new));
        NeoForge.EVENT_BUS.addListener(
                (ClientTickEvent.Pre event) -> {
                    while (key.consumeClick()) open(config.defaultGuide());
                });
    }

    /** Opens one loaded in-game guide. Missing IDs do not select another host's content. */
    public static boolean open(Identifier id) {
        Minecraft client = Minecraft.getInstance();
        // Item registry components are not bound until a level has been joined in 26.1.
        if (client.level == null || client.player == null || client.screen instanceof GuideScreen)
            return false;
        var guide = GuideRepository.get(id);
        if (guide.isEmpty()) return false;
        client.setScreen(new GuideScreen(guide.get(), client.screen));
        return true;
    }

    /** Resolves the hovered item's resource-defined node, not merely the map's start node. */
    public static boolean openItem(Identifier item) {
        Minecraft client = Minecraft.getInstance();
        if (!initialized || client.level == null || client.player == null) return false;
        var target = GuideRepository.findItem(item, defaultGuide).orElse(null);
        if (target == null) return false;
        if (client.screen instanceof GuideScreen screen && screen.guideId().equals(target.guide()))
            return screen.recallNode(target.node());
        var guide = GuideRepository.get(target.guide()).orElse(null);
        if (guide == null) return false;
        var screen = new GuideScreen(guide, client.screen);
        client.setScreen(screen);
        return screen.recallNode(target.node());
    }

    private RecallClient() {}
}
