package com.mementoinabyss.recall.client;

import com.mementoinabyss.recall.client.gui.GuiDraw;
import com.mementoinabyss.recall.client.scene.SceneScreen;
import com.mementoinabyss.recall.client.scene.SceneView;
import com.mementoinabyss.recall.data.GuideDefinition;
import com.mementoinabyss.recall.data.GuideRepository;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A frame driven, hand drawn star map and in-place guide node expansion. */
public final class GuideScreen extends Screen {
    private static final int MAP_WIDTH = 720;
    private static final int MAP_HEIGHT = 500;
    private static final int NODE_WIDTH = 64;
    private static final int NODE_HEIGHT = 60;
    private static final int CARD_WIDTH = 400;
    private static final int CARD_HEADER_HEIGHT = 94;
    private static final int ARTICLE_ROW_HEIGHT = 44;
    private static final int ARTICLE_ROW_GAP = 6;
    private static final int ARTICLE_VIEWPORT_HEIGHT = 286;
    private static final int CARD_BOTTOM_PADDING = 16;
    private static final float NODE_REPULSION_RADIUS = 172.0F;
    private static final float NODE_REPULSION_FORCE = 90.0F;
    private static final float NODE_HOME_FORCE = 3.2F;
    private static final float EDGE_IDEAL_DISTANCE = 250.0F;
    private static final float EDGE_FORCE = 1.1F;
    private static final float CARD_OBSTACLE_PADDING = 46.0F;
    private static final float CARD_OBSTACLE_INFLUENCE = 200.0F;
    private static final float CARD_OBSTACLE_FORCE = 210.0F;
    private static final float LAYOUT_DAMPING = 0.84F;
    private static final float LAYOUT_MAX_SPEED = 260.0F;
    private static final int NODE_BOX_BACKGROUND = 0xFF000000;
    // PonderButton.COLOR_IDLE and COLOR_HOVER, including their original alpha values.
    private static final int PONDER_BORDER_TOP = 0x60C0C0FF;
    private static final int PONDER_BORDER_BOTTOM = 0x30C0C0FF;
    private static final int PONDER_HOVER_TOP = 0xF0C0C0FF;
    private static final int PONDER_HOVER_BOTTOM = 0xA0C0C0FF;
    private static final int NODE_BODY_WIDTH = 28;
    private static final int NODE_BODY_HEIGHT = 28;
    private static final float NODE_ICON_SCALE = 1.5F;
    private static final int CONNECTION_ACTIVE = 0xE0FFFFFF;
    private static final int BACKGROUND = 0xB0101012;
    private static final int IMAGE_BACKGROUND_OVERLAY = 0x70101012;
    private static final Logger LOGGER = LoggerFactory.getLogger(GuideScreen.class);
    private static final int CONNECTION = 0x90C0C0FF;
    private static final int CARD_INNER = 0x28181830;
    private static final int TEXT = 0xFFF1F1F1;
    private static final int MUTED_TEXT = 0xFFB6B6C0;
    private static final int ACCENT = 0xFFC0C0FF;
    private static final float MIN_MAP_SCALE = 0.60F;
    private static final float MAX_MAP_SCALE = 1.45F;
    private static final int MAP_HEADER_X = 16;
    private static final int MAP_HEADER_Y = 16;
    private static final int MAP_HEADER_WIDTH = 300;
    private static final int MAP_HEADER_HEIGHT = 44;
    private static final int MAP_ICON_SIZE = 44;
    private static final int MAP_LIST_X = 16;
    private static final int MAP_LIST_ROW_HEIGHT = 16;
    private static final int MAP_LIST_WIDTH = 168;
    private static final int MAP_LIST_ICON_SIZE = 14;
    private static final int SEARCH_WIDTH = 128;
    private static final int SEARCH_HEIGHT = 20;
    private static final int SEARCH_BOTTOM = 6;
    private static final int SEARCH_PADDING_X = 7;
    private static final int SEARCH_PADDING_Y = 6;
    private static final int SEARCH_WIDGET_HEIGHT = SEARCH_HEIGHT - SEARCH_PADDING_Y * 2;
    private static final int SEARCH_RESULT_HEIGHT = MAP_LIST_ROW_HEIGHT;
    private static final int MAX_SEARCH_RESULTS = 8;

    private GuideDefinition guide;
    @Nullable private final Screen returnScreen;
    private Map<Identifier, GuideDefinition.GuideArticle> articles;
    private final Map<Identifier, LayoutNode> layoutNodes = new LinkedHashMap<>();
    private final List<LayoutNode> layoutNodeList = new ArrayList<>();
    private final Map<Identifier, ImageSize> backgroundSizes = new HashMap<>();
    private final GuideSpring cardSpring = new GuideSpring(0.34F);
    private final GuideSpring outgoingCardSpring = new GuideSpring(0.34F);
    private final GuideAccordion accordion = new GuideAccordion();
    private final Map<Identifier, org.commonmark.node.Node> documents = new LinkedHashMap<>();
    private final Map<LayoutKey, GuideMarkdown.Layout> articleLayouts = new LinkedHashMap<>();
    private final Map<String, SceneView> sceneViews = new LinkedHashMap<>();
    @Nullable private SceneView draggingScene;
    @Nullable private GuideDefinition.GuideNode selectedNode;
    @Nullable private GuideDefinition.GuideNode pendingNode;
    @Nullable private Rect sourceRect;
    @Nullable private Rect openingViewport;
    @Nullable private OutgoingCard outgoingCard;
    private float mapScale = 1.0F;
    private float mapOffsetX;
    private float mapOffsetY;
    private boolean panning;
    private boolean draggingMapListScrollbar;
    private float mapListScroll;
    private float mapListScrollbarDragOffset;
    private String searchText = "";
    private final GuideSpring mapTransition = new GuideSpring(0.65F);
    private boolean reverseMapTransition;
    @Nullable private GuideDefinition outgoingGuide;
    private Map<Identifier, Rect> outgoingNodeRects = Map.of();
    @Nullable private Identifier pendingMapNode;
    @Nullable private EditBox searchBox;
    private long lastFrameMillis;

    public GuideScreen(GuideDefinition guide, @Nullable Screen returnScreen) {
        super(Component.translatable(guide.title()));
        this.guide = guide;
        this.returnScreen = returnScreen;
        installGuideContent(guide);
    }

    @Override
    protected void init() {
        super.init();
        if (lastFrameMillis == 0) fitMap();
        articleLayouts.clear();
        searchBox =
                addRenderableWidget(
                        new EditBox(
                                font,
                                (width - SEARCH_WIDTH) / 2 + SEARCH_PADDING_X,
                                height - SEARCH_BOTTOM - SEARCH_HEIGHT + SEARCH_PADDING_Y,
                                SEARCH_WIDTH - SEARCH_PADDING_X * 2,
                                SEARCH_WIDGET_HEIGHT,
                                Component.translatable("recall.search")));
        searchBox.setMaxLength(80);
        searchBox.setBordered(false);
        searchBox.setTextColor(TEXT);
        searchBox.setHint(Component.translatable("recall.search.hint"));
        searchBox.setResponder(value -> searchText = value);
        lastFrameMillis = Util.getMillis();
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        advanceAnimation();
        if (outgoingGuide != null) {
            float progress = smoothstep(mapTransition.getValue());
            drawGuideBackground(graphics, outgoingGuide, 1.0F - progress);
            drawGuideBackground(graphics, guide, progress);
        } else {
            drawGuideBackground(graphics, guide, 1.0F);
        }
        boolean hasImageBackground =
                guide.background() != null
                        || (outgoingGuide != null && outgoingGuide.background() != null);
        graphics.fill(
                0, 0, width, height, hasImageBackground ? IMAGE_BACKGROUND_OVERLAY : BACKGROUND);
        float worldMouseX = worldX(mouseX);
        float worldMouseY = worldY(mouseY);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(mapOffsetX, mapOffsetY);
        pose.scale(mapScale);
        drawMap(graphics, worldMouseX, worldMouseY);
        if (selectedNode != null && sourceRect != null) {
            graphics.nextStratum();
            drawExpandedNode(graphics, worldMouseX, worldMouseY);
        }
        pose.popMatrix();
        drawNavigation(graphics, mouseX, mouseY);
        extractGuideTooltip(graphics, mouseX, mouseY);
    }

    private void drawGuideBackground(
            GuiGraphicsExtractor graphics, GuideDefinition definition, float opacity) {
        Identifier texture = definition.background();
        if (texture == null || opacity <= 0.001F) return;
        ImageSize size = backgroundSizes.computeIfAbsent(texture, this::loadBackgroundSize);
        Identifier textureResource = texture.withPath("textures/" + texture.getPath() + ".png");
        float scale =
                Math.max(width / (float) size.width(), height / (float) size.height()) * 1.08F;
        int imageWidth = Math.max(width, Math.round(size.width() * scale));
        int imageHeight = Math.max(height, Math.round(size.height() * scale));
        float maxOffsetX = Math.min((imageWidth - width) / 2.0F, width * 0.03F);
        float maxOffsetY = Math.min((imageHeight - height) / 2.0F, height * 0.03F);
        float offsetX = Mth.clamp(mapOffsetX * 0.035F, -maxOffsetX, maxOffsetX);
        float offsetY = Mth.clamp(mapOffsetY * 0.035F, -maxOffsetY, maxOffsetY);
        int x = Math.round((width - imageWidth) / 2.0F + offsetX);
        int y = Math.round((height - imageHeight) / 2.0F + offsetY);
        int color = Mth.clamp(Math.round(opacity * 255.0F), 0, 255) << 24 | 0x00FFFFFF;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                textureResource,
                x,
                y,
                0,
                0,
                imageWidth,
                imageHeight,
                size.width(),
                size.height(),
                size.width(),
                size.height(),
                color);
    }

    private ImageSize loadBackgroundSize(Identifier texture) {
        Identifier resourceId =
                Identifier.fromNamespaceAndPath(
                        texture.getNamespace(), "textures/" + texture.getPath() + ".png");
        try {
            var resource = minecraft.getResourceManager().getResource(resourceId).orElse(null);
            if (resource == null) return new ImageSize(1, 1);
            try (var input = resource.open();
                    NativeImage image = NativeImage.read(input)) {
                return new ImageSize(image.getWidth(), image.getHeight());
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not read guide background dimensions for {}", resourceId, exception);
            return new ImageSize(1, 1);
        }
    }

    private void installGuideContent(GuideDefinition definition) {
        articles = definition.articlesById();
        documents.clear();
        layoutNodes.clear();
        layoutNodeList.clear();
        articleLayouts.clear();
        sceneViews.clear();
        for (var article : definition.articles())
            documents.put(article.id(), GuideMarkdown.parse(article.lines()));
        for (GuideDefinition.GuideNode node : definition.nodes()) {
            LayoutNode layoutNode = new LayoutNode(node);
            layoutNodes.put(node.id(), layoutNode);
            layoutNodeList.add(layoutNode);
        }
    }

    private void positionSearchBox() {
        if (searchBox == null) return;
        searchBox.setX((width - SEARCH_WIDTH) / 2 + SEARCH_PADDING_X);
        searchBox.setY(height - SEARCH_BOTTOM - SEARCH_HEIGHT + SEARCH_PADDING_Y);
    }

    @Override
    protected void repositionElements() {
        positionSearchBox();
    }

    private void advanceAnimation() {
        long now = Util.getMillis();
        if (lastFrameMillis == 0L) {
            lastFrameMillis = now;
        }
        float deltaSeconds = Mth.clamp((now - lastFrameMillis) / 1000.0F, 0.0F, 0.05F);
        lastFrameMillis = now;
        cardSpring.step(deltaSeconds);
        outgoingCardSpring.step(deltaSeconds);
        if (outgoingCard != null && outgoingCardSpring.isSettled()) outgoingCard = null;
        mapTransition.step(deltaSeconds);
        if (outgoingGuide != null && mapTransition.isSettled()) {
            outgoingGuide = null;
            outgoingNodeRects = Map.of();
            reverseMapTransition = false;
            if (pendingMapNode != null) {
                guide.node(pendingMapNode).ifPresent(node -> focusNode(node.id()));
                pendingMapNode = null;
            }
        }
        accordion.step(deltaSeconds);
        advanceLayout(deltaSeconds);

        if (selectedNode != null
                && cardSpring.getTarget() == 0.0F
                && cardSpring.isSettled()
                && accordion.isSettled()) {
            selectedNode = null;
            sourceRect = null;
            openingViewport = null;
            accordion.clear();
            if (pendingNode != null) openNode(pendingNode);
        }
    }

    private void advanceLayout(float deltaSeconds) {
        if (layoutNodeList.isEmpty()) {
            return;
        }

        Rect obstacle =
                selectedNode != null && sourceRect != null && cardSpring.getValue() > 0.001F
                        ? cardObstacleRect()
                        : null;
        for (LayoutNode layoutNode : layoutNodeList) {
            layoutNode.forceX = (layoutNode.homeX - layoutNode.x) * NODE_HOME_FORCE;
            layoutNode.forceY = (layoutNode.homeY - layoutNode.y) * NODE_HOME_FORCE;
        }

        for (int firstIndex = 0; firstIndex < layoutNodeList.size(); firstIndex++) {
            LayoutNode first = layoutNodeList.get(firstIndex);
            if (first.node == selectedNode
                    || (outgoingCard != null && first.node == outgoingCard.node())) {
                continue;
            }
            for (int secondIndex = firstIndex + 1;
                    secondIndex < layoutNodeList.size();
                    secondIndex++) {
                LayoutNode second = layoutNodeList.get(secondIndex);
                if (second.node == selectedNode
                        || (outgoingCard != null && second.node == outgoingCard.node())) {
                    continue;
                }
                float firstCenterX = first.x + NODE_WIDTH / 2.0F;
                float firstCenterY = first.y + NODE_HEIGHT / 2.0F;
                float secondCenterX = second.x + NODE_WIDTH / 2.0F;
                float secondCenterY = second.y + NODE_HEIGHT / 2.0F;
                float dx = secondCenterX - firstCenterX;
                float dy = secondCenterY - firstCenterY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance < 0.001F) {
                    dx = 1.0F;
                    dy = 0.0F;
                    distance = 1.0F;
                }
                float directionX = dx / distance;
                float directionY = dy / distance;

                if (isConnected(first.node, second.node)) {
                    float edgeForce = (distance - EDGE_IDEAL_DISTANCE) * EDGE_FORCE;
                    first.forceX += directionX * edgeForce;
                    first.forceY += directionY * edgeForce;
                    second.forceX -= directionX * edgeForce;
                    second.forceY -= directionY * edgeForce;
                }
                if (distance < NODE_REPULSION_RADIUS) {
                    float repulsion =
                            (1.0F - distance / NODE_REPULSION_RADIUS) * NODE_REPULSION_FORCE;
                    first.forceX -= directionX * repulsion;
                    first.forceY -= directionY * repulsion;
                    second.forceX += directionX * repulsion;
                    second.forceY += directionY * repulsion;
                }
            }
        }

        for (LayoutNode layoutNode : layoutNodeList) {
            if (layoutNode.node == selectedNode
                    || (outgoingCard != null && layoutNode.node == outgoingCard.node())) {
                layoutNode.velocityX = 0.0F;
                layoutNode.velocityY = 0.0F;
                continue;
            }
            if (obstacle != null) {
                applyObstacleForce(layoutNode, obstacle);
            }

            float damping = (float) Math.pow(LAYOUT_DAMPING, deltaSeconds * 20.0F);
            layoutNode.velocityX =
                    Mth.clamp(
                            (layoutNode.velocityX + layoutNode.forceX * deltaSeconds) * damping,
                            -LAYOUT_MAX_SPEED,
                            LAYOUT_MAX_SPEED);
            layoutNode.velocityY =
                    Mth.clamp(
                            (layoutNode.velocityY + layoutNode.forceY * deltaSeconds) * damping,
                            -LAYOUT_MAX_SPEED,
                            LAYOUT_MAX_SPEED);
            layoutNode.x += layoutNode.velocityX * deltaSeconds;
            layoutNode.y += layoutNode.velocityY * deltaSeconds;
            float boundedX = Mth.clamp(layoutNode.x, 16.0F, MAP_WIDTH - NODE_WIDTH - 16.0F);
            float boundedY = Mth.clamp(layoutNode.y, 16.0F, MAP_HEIGHT - NODE_HEIGHT - 16.0F);
            if (boundedX != layoutNode.x) {
                layoutNode.velocityX = 0.0F;
                layoutNode.x = boundedX;
            }
            if (boundedY != layoutNode.y) {
                layoutNode.velocityY = 0.0F;
                layoutNode.y = boundedY;
            }
        }
    }

    private void applyObstacleForce(LayoutNode layoutNode, Rect obstacle) {
        float centerX = layoutNode.x + NODE_WIDTH / 2.0F;
        float centerY = layoutNode.y + NODE_HEIGHT / 2.0F;
        float closestX = Mth.clamp(centerX, obstacle.left(), obstacle.right());
        float closestY = Mth.clamp(centerY, obstacle.top(), obstacle.bottom());
        float pushX = centerX - closestX;
        float pushY = centerY - closestY;
        float distance = (float) Math.sqrt(pushX * pushX + pushY * pushY);
        if (distance < 0.001F) {
            float leftDistance = centerX - obstacle.left();
            float rightDistance = obstacle.right() - centerX;
            float topDistance = centerY - obstacle.top();
            float bottomDistance = obstacle.bottom() - centerY;
            float nearest =
                    Math.min(
                            Math.min(leftDistance, rightDistance),
                            Math.min(topDistance, bottomDistance));
            if (nearest == leftDistance) {
                pushX = -1.0F;
                pushY = 0.0F;
            } else if (nearest == rightDistance) {
                pushX = 1.0F;
                pushY = 0.0F;
            } else if (nearest == topDistance) {
                pushX = 0.0F;
                pushY = -1.0F;
            } else {
                pushX = 0.0F;
                pushY = 1.0F;
            }
            distance = 1.0F;
        }
        if (distance >= CARD_OBSTACLE_INFLUENCE) {
            return;
        }
        float push = (1.0F - distance / CARD_OBSTACLE_INFLUENCE) * CARD_OBSTACLE_FORCE;
        layoutNode.forceX += pushX / distance * push;
        layoutNode.forceY += pushY / distance * push;
    }

    private Rect cardObstacleRect() {
        Rect card = currentCardRect();
        float padding = CARD_OBSTACLE_PADDING * Mth.clamp(cardSpring.getValue(), 0.0F, 1.0F);
        return new Rect(
                card.left() - padding,
                card.top() - padding,
                card.width() + padding * 2.0F,
                card.height() + padding * 2.0F);
    }

    private boolean isConnected(GuideDefinition.GuideNode first, GuideDefinition.GuideNode second) {
        return guide.connections().stream()
                .anyMatch(
                        connection ->
                                (connection.from().equals(first.id())
                                                && connection.to().equals(second.id()))
                                        || (connection.from().equals(second.id())
                                                && connection.to().equals(first.id())));
    }

    private void drawMap(GuiGraphicsExtractor graphics, float mouseX, float mouseY) {
        if (outgoingGuide != null) {
            float progress = smoothstep(mapTransition.getValue());
            drawMapLayer(
                    graphics,
                    outgoingGuide,
                    outgoingNodeRects,
                    progress,
                    false,
                    reverseMapTransition,
                    1.0F - progress,
                    1.0F,
                    1.0F);
            drawMapLayer(
                    graphics,
                    guide,
                    snapshotNodeRects(),
                    progress,
                    true,
                    reverseMapTransition,
                    progress,
                    0.0F,
                    0.0F);
            return;
        }
        drawMapLayer(graphics, guide, snapshotNodeRects(), 1.0F, true, false, 1.0F, mouseX, mouseY);
    }

    private Map<Identifier, Rect> snapshotNodeRects() {
        Map<Identifier, Rect> result = new LinkedHashMap<>();
        for (var node : guide.nodes()) result.put(node.id(), nodeVisualRect(node));
        return result;
    }

    private void drawMapLayer(
            GuiGraphicsExtractor graphics,
            GuideDefinition definition,
            Map<Identifier, Rect> nodeRects,
            float progress,
            boolean entering,
            boolean reverse,
            float opacity,
            float mouseX,
            float mouseY) {
        for (GuideDefinition.GuideConnection connection : definition.connections()) {
            Rect from = nodeRects.get(connection.from());
            Rect to = nodeRects.get(connection.to());
            if (from == null || to == null) continue;
            Rect fromVisual =
                    entering && selectedNode != null && connection.from().equals(selectedNode.id())
                            ? currentCardRect()
                            : flightRect(from, progress, entering, reverse);
            Rect toVisual =
                    entering && selectedNode != null && connection.to().equals(selectedNode.id())
                            ? currentCardRect()
                            : flightRect(to, progress, entering, reverse);
            int connectionColor = withAlpha(CONNECTION, opacity);
            if (entering
                    && selectedNode != null
                    && (connection.from().equals(selectedNode.id())
                            || connection.to().equals(selectedNode.id())))
                connectionColor = withAlpha(CONNECTION_ACTIVE, opacity);
            drawLine(
                    graphics,
                    fromVisual.centerX(),
                    fromVisual.centerY(),
                    toVisual.centerX(),
                    toVisual.centerY(),
                    connectionColor,
                    1.0F);
        }

        for (GuideDefinition.GuideNode node : definition.nodes()) {
            if (entering && node == selectedNode) continue;
            Rect target = nodeRects.get(node.id());
            if (target == null) continue;
            Rect visual = flightRect(target, progress, entering, reverse);
            boolean hovered = entering && progress > 0.9F && contains(visual, mouseX, mouseY);
            drawNode(graphics, node, visual, hovered, opacity);
        }
    }

    private static Rect flightRect(Rect target, float progress, boolean entering, boolean reverse) {
        float centerX = MAP_WIDTH / 2.0F;
        float centerY = MAP_HEIGHT / 2.0F;
        float distance;
        float scale;
        if (reverse) {
            distance = entering ? 1.7F - 0.7F * progress : 1.0F - 0.78F * progress;
            scale = entering ? 0.55F + 0.45F * progress : 1.0F - 0.45F * progress;
        } else {
            distance = entering ? 0.22F + 0.78F * progress : 1.0F + 0.7F * progress;
            scale = entering ? 0.55F + 0.45F * progress : 1.0F - 0.25F * progress;
        }
        float targetCenterX = centerX + (target.centerX() - centerX) * distance;
        float targetCenterY = centerY + (target.centerY() - centerY) * distance;
        return new Rect(
                targetCenterX - target.width() * scale / 2.0F,
                targetCenterY - target.height() * scale / 2.0F,
                target.width() * scale,
                target.height() * scale);
    }

    private void drawNode(
            GuiGraphicsExtractor graphics,
            GuideDefinition.GuideNode node,
            Rect visual,
            boolean hovered) {
        drawNode(graphics, node, visual, hovered, 1.0F);
    }

    private void drawNode(
            GuiGraphicsExtractor graphics,
            GuideDefinition.GuideNode node,
            Rect visual,
            boolean hovered,
            float opacity) {
        if (opacity <= 0.02F) return;
        drawPonderBox(graphics, visual, hovered, opacity);
        drawItemIcon(graphics, itemStack(node.item()), nodeIconRect(visual), opacity);
        drawNodeTitle(graphics, node, visual, 0, withAlpha(TEXT, opacity));
    }

    private void drawNodeTitle(
            GuiGraphicsExtractor graphics,
            GuideDefinition.GuideNode node,
            Rect current,
            float progress) {
        drawNodeTitle(graphics, node, current, progress, TEXT);
    }

    private void drawNodeTitle(
            GuiGraphicsExtractor graphics,
            GuideDefinition.GuideNode node,
            Rect current,
            float progress,
            int color) {
        Component title = Component.literal(node.title()).withStyle(ChatFormatting.BOLD);
        Rect source = progress == 0 ? current : sourceRect;
        float x =
                Mth.lerp(
                        progress, source.centerX() - font.width(title) / 2.0F, current.left() + 47);
        float y = Mth.lerp(progress, source.bottom() + 7, current.top() + 18);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        graphics.text(font, title, 0, 0, color);
        pose.popMatrix();
    }

    /** BoxElement geometry: one omitted outer pixel at each corner and a one pixel inner border. */
    private static void drawPonderBox(GuiGraphicsExtractor graphics, Rect rect, boolean hovered) {
        drawPonderBox(graphics, rect, hovered, 1.0F);
    }

    private static void drawPonderBox(
            GuiGraphicsExtractor graphics, Rect rect, boolean hovered, float opacity) {
        GuiDraw.box(
                graphics,
                rect.left(),
                rect.top(),
                Math.round(rect.width()),
                Math.round(rect.height()),
                withAlpha(NODE_BOX_BACKGROUND, opacity),
                withAlpha(hovered ? PONDER_HOVER_TOP : PONDER_BORDER_TOP, opacity),
                withAlpha(hovered ? PONDER_HOVER_BOTTOM : PONDER_BORDER_BOTTOM, opacity));
    }

    private static Rect nodeIconRect(Rect visual) {
        float size = 16.0F * NODE_ICON_SCALE;
        return new Rect(visual.centerX() - size / 2, visual.centerY() - size / 2, size, size);
    }

    private static void drawItemIcon(GuiGraphicsExtractor graphics, ItemStack item, Rect icon) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(icon.left(), icon.top());
        pose.scale(icon.width() / 16.0F);
        graphics.item(item, 0, 0);
        pose.popMatrix();
    }

    private static void drawItemIcon(
            GuiGraphicsExtractor graphics, ItemStack item, Rect icon, float opacity) {
        float iconScale = (float) Math.sqrt(Mth.clamp(opacity, 0.0F, 1.0F));
        float iconWidth = icon.width() * iconScale;
        float iconHeight = icon.height() * iconScale;
        drawItemIcon(
                graphics,
                item,
                new Rect(
                        icon.centerX() - iconWidth / 2.0F,
                        icon.centerY() - iconHeight / 2.0F,
                        iconWidth,
                        iconHeight));
    }

    private void drawOutgoingCard(GuiGraphicsExtractor graphics) {
        float progress = Mth.clamp(outgoingCardSpring.getValue(), 0.0F, 1.0F);
        float collapse = 1.0F - progress;
        Rect source = outgoingCard.source();
        Rect card = Rect.lerp(source, outgoingCard.card(), progress);
        drawPonderBox(graphics, card, false);
        Rect icon = Rect.lerp(nodeIconRect(source), outgoingCard.icon(), progress);
        drawItemIcon(graphics, itemStack(outgoingCard.node().item()), icon);

        Component title =
                Component.literal(outgoingCard.node().title()).withStyle(ChatFormatting.BOLD);
        float textX =
                Mth.lerp(
                        collapse,
                        outgoingCard.card().left() + 47,
                        source.centerX() - font.width(title) / 2.0F);
        float textY = Mth.lerp(collapse, outgoingCard.card().top() + 18, source.bottom() + 7);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(textX, textY);
        graphics.text(font, title, 0, 0, TEXT);
        pose.popMatrix();
    }

    private void drawExpandedNode(GuiGraphicsExtractor graphics, float mouseX, float mouseY) {
        if (outgoingCard != null) drawOutgoingCard(graphics);
        float progress = Mth.clamp(cardSpring.getValue(), 0.0F, 1.0F);
        Rect current = currentCardRect();
        int left = Math.round(current.left());
        int top = Math.round(current.top());
        int right = Math.round(current.right());
        int bottom = Math.round(current.bottom());
        drawPonderBox(graphics, current, contains(current, mouseX, mouseY));
        drawItemIcon(graphics, itemStack(selectedNode.item()), expandedIconRect());
        drawNodeTitle(graphics, selectedNode, current, progress);

        float contentAlpha = smoothstep((progress - 0.16F) / 0.70F);
        int contentColor = withAlpha(TEXT, contentAlpha);
        int mutedColor = withAlpha(MUTED_TEXT, contentAlpha);
        int accentColor = withAlpha(ACCENT, contentAlpha);
        if (contentAlpha > 0.01F) {
            graphics.textWithWordWrap(
                    font,
                    Component.literal(selectedNode.summary()),
                    left + 14,
                    top + 50,
                    right - left - 48,
                    mutedColor);
            graphics.text(font, Component.literal("X"), right - 25, top + 14, mutedColor);
        }

        if (contentAlpha <= 0.01F) {
            return;
        }
        float contentTop = top + CARD_HEADER_HEIGHT;
        float contentWidth = right - left - 24.0F;
        float viewportHeight = bottom - top - CARD_HEADER_HEIGHT - CARD_BOTTOM_PADDING;
        if (viewportHeight <= 0.0F) {
            return;
        }
        graphics.enableScissor(
                left + 12,
                Math.round(contentTop),
                Math.round(left + 12 + contentWidth),
                Math.round(contentTop + viewportHeight));
        drawArticles(
                graphics,
                left + 12,
                contentTop,
                contentWidth,
                contentColor,
                mutedColor,
                accentColor,
                mouseX,
                mouseY);
        graphics.disableScissor();
    }

    private void drawArticles(
            GuiGraphicsExtractor graphics,
            float x,
            float y,
            float width,
            int textColor,
            int mutedColor,
            int accentColor,
            float mouseX,
            float mouseY) {
        int articleIndex = 0;
        for (Identifier articleId : selectedNode.articleIds()) {
            GuideDefinition.GuideArticle article = articles.get(articleId);
            if (article == null) {
                continue;
            }
            float rowY = y;
            boolean hovered =
                    contains(new Rect(x, rowY, width, ARTICLE_ROW_HEIGHT), mouseX, mouseY);
            graphics.fill(
                    Math.round(x),
                    Math.round(rowY),
                    Math.round(x + width),
                    Math.round(rowY + ARTICLE_ROW_HEIGHT),
                    hovered ? 0x40C0C0FF : CARD_INNER);
            graphics.text(
                    font,
                    Component.literal(article.title()).withStyle(ChatFormatting.BOLD),
                    Math.round(x + 10),
                    Math.round(rowY + 7),
                    hovered ? accentColor : textColor);
            graphics.textWithWordWrap(
                    font,
                    Component.literal(article.summary()),
                    Math.round(x + 10),
                    Math.round(rowY + 23),
                    Math.round(width - 28),
                    mutedColor);
            graphics.text(
                    font,
                    Component.literal(articleId.equals(accordion.getSelected()) ? "-" : "+"),
                    Math.round(x + width - 18),
                    Math.round(rowY + 13),
                    accentColor);
            y += ARTICLE_ROW_HEIGHT + ARTICLE_ROW_GAP;
            float articleProgress = accordion.progress(articleId);
            if (articleProgress > 0) {
                float visibleHeight = articleVisibleHeight(article);
                if (visibleHeight > 0.0F) {
                    float bodyY = y - (float) accordion.scroll(articleId);
                    graphics.enableScissor(
                            Math.round(x),
                            Math.round(y),
                            Math.round(x + width),
                            Math.round(y + visibleHeight));
                    drawArticleBody(
                            graphics,
                            article,
                            x + 6,
                            bodyY,
                            width - 12,
                            withAlpha(TEXT, articleProgress),
                            withAlpha(ACCENT, articleProgress),
                            withAlpha(MUTED_TEXT, articleProgress),
                            mouseX,
                            mouseY);
                    graphics.disableScissor();
                }
                y += visibleHeight;
            }
            articleIndex++;
        }
        if (articleIndex == 0) {
            graphics.text(font, "No articles available", Math.round(x), Math.round(y), mutedColor);
        }
    }

    private void drawArticleBody(
            GuiGraphicsExtractor graphics,
            GuideDefinition.GuideArticle article,
            float x,
            float y,
            float width,
            int textColor,
            int accentColor,
            int mutedColor,
            float mouseX,
            float mouseY) {
        for (var element : articleLayout(article).elements()) {
            int left = Math.round(x + element.x());
            int top = Math.round(y + element.y());
            switch (element.kind()) {
                case TEXT -> {
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(left, top);
                    graphics.pose().scale(element.scale());
                    graphics.text(font, element.text(), 0, 0, textColor);
                    graphics.pose().popMatrix();
                }
                case CODE ->
                        graphics.fill(
                                left,
                                top,
                                left + element.width(),
                                top + element.height(),
                                0xA0252530);
                case QUOTE ->
                        graphics.fill(
                                left,
                                top,
                                left + element.width(),
                                top + element.height(),
                                accentColor);
                case RULE -> graphics.fill(left, top, left + element.width(), top + 1, mutedColor);
                case SCENE -> {
                    SceneView view = sceneView(article, element);
                    if (view != null)
                        view.extract(
                                graphics,
                                left,
                                top,
                                element.width(),
                                element.height(),
                                mouseX,
                                mouseY,
                                false,
                                0);
                    else
                        graphics.text(
                                font, "Invalid scene: " + element.scene(), left, top, mutedColor);
                }
                case IMAGE -> {
                    var image = element.image();
                    if (image != null)
                        graphics.blit(
                                RenderPipelines.GUI_TEXTURED,
                                image.id(),
                                left,
                                top,
                                0,
                                0,
                                element.width(),
                                element.height(),
                                image.width(),
                                image.height(),
                                image.width(),
                                image.height());
                }
            }
        }
    }

    private void drawNavigation(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        drawItemIcon(
                graphics,
                itemStack(mapIcon()),
                new Rect(MAP_HEADER_X, MAP_HEADER_Y, MAP_ICON_SIZE - 4, MAP_ICON_SIZE - 4));

        float titleLeft = MAP_HEADER_X + MAP_ICON_SIZE + 4;
        String title = Component.translatable(guide.title()).getString();
        boolean hasSubtitle = !guide.subtitle().isBlank();
        String subtitle = hasSubtitle ? Component.translatable(guide.subtitle()).getString() : "";
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(titleLeft, MAP_HEADER_Y + (hasSubtitle ? 7 : 16));
        pose.scale(1.5F);
        graphics.text(
                font,
                font.plainSubstrByWidth(title, Math.round((MAP_HEADER_WIDTH - 8) / 1.5F)),
                0,
                0,
                TEXT);
        pose.popMatrix();
        if (hasSubtitle) {
            graphics.text(
                    font,
                    font.plainSubstrByWidth(subtitle, MAP_HEADER_WIDTH - 8),
                    Math.round(titleLeft),
                    MAP_HEADER_Y + 27,
                    MUTED_TEXT);
        }

        List<GuideDefinition> maps = maps();
        Rect listViewport = mapListViewport(maps);
        float maxListScroll = mapListMaxScroll(maps, listViewport);
        mapListScroll = Mth.clamp(mapListScroll, 0.0F, maxListScroll);
        if (!maps.isEmpty() && listViewport.height() > 0.0F) {
            graphics.enableScissor(
                    MAP_LIST_X,
                    Math.round(listViewport.top()),
                    MAP_LIST_X + MAP_LIST_WIDTH,
                    Math.round(listViewport.bottom()));
            for (int i = 0; i < maps.size(); i++) {
                GuideDefinition map = maps.get(i);
                float rowTop = mapListContentTop(maps, listViewport) + i * MAP_LIST_ROW_HEIGHT;
                Rect row = new Rect(MAP_LIST_X, rowTop, MAP_LIST_WIDTH, MAP_LIST_ROW_HEIGHT);
                boolean hovered =
                        contains(row, mouseX, mouseY) && contains(listViewport, mouseX, mouseY);
                boolean selected = map.id().equals(guide.id());
                int leftColor = hovered ? 0xA0181818 : selected ? 0x18181818 : 0x0A181818;
                GuiDraw.horizontalGradient(
                        graphics,
                        row.left(),
                        row.top(),
                        row.right(),
                        row.bottom(),
                        leftColor,
                        0x00181818);
                if (hovered) {
                    drawItemIcon(
                            graphics,
                            itemStack(mapIcon(map)),
                            new Rect(
                                    row.left() + 3,
                                    row.top() + 1,
                                    MAP_LIST_ICON_SIZE,
                                    MAP_LIST_ICON_SIZE));
                }
                graphics.text(
                        font,
                        font.plainSubstrByWidth(
                                Component.translatable(map.title()).getString(),
                                Math.round(row.width() - MAP_LIST_ICON_SIZE - 9)),
                        Math.round(row.left() + MAP_LIST_ICON_SIZE + 6),
                        Math.round(row.top() + (MAP_LIST_ROW_HEIGHT - 9) / 2.0F),
                        hovered ? TEXT : selected ? 0x40F1F1F1 : 0x18F1F1F1);
            }
            graphics.disableScissor();

            float trackTop = mapListTrackTop(maps, listViewport);
            float trackHeight = mapListTrackHeight(maps, listViewport);
            float thumbHeight = mapListThumbHeight(maps, listViewport);
            float thumbTop = mapListThumbTop(maps, listViewport, thumbHeight);
            graphics.fill(
                    MAP_LIST_X,
                    Math.round(trackTop),
                    MAP_LIST_X + 1,
                    Math.round(trackTop + trackHeight),
                    0x30202020);
            graphics.fill(
                    MAP_LIST_X,
                    Math.round(thumbTop),
                    MAP_LIST_X + 1,
                    Math.round(thumbTop + thumbHeight),
                    contains(listViewport, mouseX, mouseY) ? 0xD0C0C0FF : 0x70C0C0FF);
        }

        int searchX = (width - SEARCH_WIDTH) / 2;
        int searchY = height - SEARCH_BOTTOM - SEARCH_HEIGHT;
        graphics.fill(
                searchX, searchY, searchX + SEARCH_WIDTH, searchY + SEARCH_HEIGHT, 0xE0000000);
        graphics.outline(searchX, searchY, SEARCH_WIDTH, SEARCH_HEIGHT, 0xFFB0B0B0);

        List<SearchResult> results = searchResults();
        if (!results.isEmpty()) {
            int resultTop = searchY - results.size() * SEARCH_RESULT_HEIGHT - 6;
            int popupHeight = results.size() * SEARCH_RESULT_HEIGHT + 4;
            graphics.fill(
                    searchX,
                    resultTop,
                    searchX + SEARCH_WIDTH,
                    resultTop + popupHeight,
                    0xD0101010);
            for (int i = 0; i < results.size(); i++) {
                SearchResult result = results.get(i);
                int rowTop = resultTop + 2 + i * SEARCH_RESULT_HEIGHT;
                boolean hovered =
                        mouseX >= searchX + 2
                                && mouseX < searchX + SEARCH_WIDTH - 2
                                && mouseY >= rowTop
                                && mouseY < rowTop + SEARCH_RESULT_HEIGHT - 2;
                if (hovered)
                    graphics.fill(
                            searchX + 2,
                            rowTop,
                            searchX + SEARCH_WIDTH - 2,
                            rowTop + SEARCH_RESULT_HEIGHT - 2,
                            0x70404040);
                drawItemIcon(
                        graphics,
                        itemStack(result.node().item()),
                        new Rect(searchX + 5, rowTop + 1, MAP_LIST_ICON_SIZE, MAP_LIST_ICON_SIZE));
                graphics.text(
                        font,
                        font.plainSubstrByWidth(
                                result.node().title(), SEARCH_WIDTH - MAP_LIST_ICON_SIZE - 12),
                        searchX + MAP_LIST_ICON_SIZE + 9,
                        rowTop + (SEARCH_RESULT_HEIGHT - 9) / 2,
                        TEXT);
            }
        }
    }

    private List<GuideDefinition> maps() {
        return GuideRepository.snapshot().values().stream()
                .sorted(
                        Comparator.comparingInt(
                                        (GuideDefinition definition) ->
                                                definition.layer() == 0
                                                        ? Integer.MAX_VALUE
                                                        : definition.layer())
                                .thenComparing(this::localizedMapTitle)
                                .thenComparing(GuideDefinition::id))
                .toList();
    }

    private String localizedMapTitle(GuideDefinition definition) {
        return Component.translatable(definition.title()).getString();
    }

    private String localizedMapSubtitle(GuideDefinition definition) {
        return definition.subtitle().isBlank()
                ? ""
                : Component.translatable(definition.subtitle()).getString();
    }

    private float mapListContentTop(List<GuideDefinition> maps, Rect viewport) {
        float contentHeight = maps.size() * MAP_LIST_ROW_HEIGHT;
        return viewport.top()
                + Math.max(0.0F, (viewport.height() - contentHeight) / 2.0F)
                - mapListScroll;
    }

    private Rect mapListViewport(List<GuideDefinition> maps) {
        float viewportHeight = Math.max(0.0F, height - 48.0F);
        return new Rect(
                MAP_LIST_X, (height - viewportHeight) / 2.0F, MAP_LIST_WIDTH, viewportHeight);
    }

    private float mapListMaxScroll(List<GuideDefinition> maps, Rect viewport) {
        return Math.max(0.0F, maps.size() * MAP_LIST_ROW_HEIGHT - viewport.height());
    }

    private float mapListTrackTop(List<GuideDefinition> maps, Rect viewport) {
        return mapListMaxScroll(maps, viewport) > 0.0F
                ? viewport.top()
                : mapListContentTop(maps, viewport);
    }

    private float mapListTrackHeight(List<GuideDefinition> maps, Rect viewport) {
        return Math.min(viewport.height(), maps.size() * MAP_LIST_ROW_HEIGHT);
    }

    private float mapListThumbHeight(List<GuideDefinition> maps, Rect viewport) {
        if (viewport.height() <= 0.0F) return 0.0F;
        float contentHeight = maps.size() * MAP_LIST_ROW_HEIGHT;
        float maxScroll = mapListMaxScroll(maps, viewport);
        if (maxScroll <= 0.0F) return Math.min(viewport.height(), MAP_LIST_ROW_HEIGHT);
        return Math.clamp(
                viewport.height() * viewport.height() / contentHeight, 6.0F, viewport.height());
    }

    private float mapListThumbTop(List<GuideDefinition> maps, Rect viewport, float thumbHeight) {
        float maxScroll = mapListMaxScroll(maps, viewport);
        if (maxScroll <= 0.0F) {
            int selected = 0;
            for (int i = 0; i < maps.size(); i++) {
                if (maps.get(i).id().equals(guide.id())) {
                    selected = i;
                    break;
                }
            }
            return mapListContentTop(maps, viewport) + selected * MAP_LIST_ROW_HEIGHT;
        }
        float trackTravel = viewport.height() - thumbHeight;
        if (trackTravel <= 0.0F) return viewport.top();
        return viewport.top() + trackTravel * mapListScroll / maxScroll;
    }

    private void updateMapListScroll(double mouseY) {
        List<GuideDefinition> maps = maps();
        Rect viewport = mapListViewport(maps);
        float thumbHeight = mapListThumbHeight(maps, viewport);
        float maxScroll = mapListMaxScroll(maps, viewport);
        float trackTop = mapListTrackTop(maps, viewport);
        float trackHeight = mapListTrackHeight(maps, viewport);
        float trackTravel = trackHeight - thumbHeight;
        if (maxScroll <= 0.0F || trackTravel <= 0.0F) return;
        float thumbTop =
                Mth.clamp(
                        (float) mouseY - mapListScrollbarDragOffset,
                        trackTop,
                        trackTop + trackHeight - thumbHeight);
        mapListScroll = (thumbTop - trackTop) / trackTravel * maxScroll;
    }

    private Identifier mapIcon() {
        return mapIcon(guide);
    }

    private Identifier mapIcon(GuideDefinition definition) {
        if (definition.icon() != null) return definition.icon();
        return definition.nodes().stream()
                .findFirst()
                .map(GuideDefinition.GuideNode::item)
                .orElse(Identifier.parse("minecraft:barrier"));
    }

    private List<SearchResult> searchResults() {
        String query = searchText.trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isBlank()) return List.of();
        List<SearchResult> results = new ArrayList<>();
        for (GuideDefinition definition : maps()) {
            for (GuideDefinition.GuideNode node : definition.nodes()) {
                String searchable =
                        (localizedMapTitle(definition)
                                        + " "
                                        + localizedMapSubtitle(definition)
                                        + " "
                                        + node.title()
                                        + " "
                                        + node.id()
                                        + " "
                                        + node.item())
                                .toLowerCase(java.util.Locale.ROOT);
                if (searchable.contains(query)) {
                    results.add(new SearchResult(definition, node));
                    if (results.size() == MAX_SEARCH_RESULTS) return results;
                }
            }
        }
        return results;
    }

    private boolean handleNavigationClick(double screenX, double screenY, boolean doubleClick) {
        if (searchBox != null && searchBox.isMouseOver(screenX, screenY)) return false;
        List<SearchResult> results = searchResults();
        if (!results.isEmpty()) {
            int searchY = height - SEARCH_BOTTOM - SEARCH_HEIGHT;
            int resultTop = searchY - results.size() * SEARCH_RESULT_HEIGHT - 6;
            int searchX = (width - SEARCH_WIDTH) / 2;
            for (int i = 0; i < results.size(); i++) {
                Rect row =
                        new Rect(
                                searchX + 2,
                                resultTop + 2 + i * SEARCH_RESULT_HEIGHT,
                                SEARCH_WIDTH - 4,
                                SEARCH_RESULT_HEIGHT - 2);
                if (contains(row, (float) screenX, (float) screenY)) {
                    activateSearchResult(results.get(i));
                    return true;
                }
            }
        }

        List<GuideDefinition> maps = maps();
        Rect listViewport = mapListViewport(maps);
        float maxListScroll = mapListMaxScroll(maps, listViewport);
        float trackTop = mapListTrackTop(maps, listViewport);
        float trackHeight = mapListTrackHeight(maps, listViewport);
        float trackBottom = trackTop + trackHeight;
        if (maxListScroll > 0.0F
                && screenX >= MAP_LIST_X
                && screenX < MAP_LIST_X + 1
                && screenY >= trackTop
                && screenY < trackBottom) {
            float thumbHeight = mapListThumbHeight(maps, listViewport);
            float thumbTop = mapListThumbTop(maps, listViewport, thumbHeight);
            if (screenY >= thumbTop && screenY < thumbTop + thumbHeight)
                mapListScrollbarDragOffset = (float) screenY - thumbTop;
            else {
                mapListScrollbarDragOffset = thumbHeight / 2.0F;
                updateMapListScroll(screenY);
            }
            draggingMapListScrollbar = true;
            return true;
        }

        for (int i = 0; i < maps.size(); i++) {
            Rect row =
                    new Rect(
                            MAP_LIST_X,
                            mapListContentTop(maps, listViewport) + i * MAP_LIST_ROW_HEIGHT,
                            MAP_LIST_WIDTH,
                            MAP_LIST_ROW_HEIGHT);
            if (contains(listViewport, (float) screenX, (float) screenY)
                    && contains(row, (float) screenX, (float) screenY)) {
                switchGuide(maps.get(i), null);
                return true;
            }
        }
        return false;
    }

    private void activateSearchResult(SearchResult result) {
        if (searchBox != null) {
            searchBox.setValue("");
            setInitialFocus(searchBox);
            searchBox.setCursorPosition(0);
        }
        if (result.guide().id().equals(guide.id())) {
            focusNode(result.node().id());
        } else {
            switchGuide(result.guide(), result.node().id());
        }
    }

    private void switchGuide(GuideDefinition target, @Nullable Identifier focusNode) {
        if (target.id().equals(guide.id())) {
            if (focusNode != null) focusNode(focusNode);
            return;
        }
        outgoingGuide = guide;
        outgoingNodeRects = snapshotNodeRects();
        reverseMapTransition =
                guide.layer() > 0 && target.layer() > 0 && target.layer() < guide.layer();
        guide = target;
        installGuideContent(target);
        selectedNode = null;
        sourceRect = null;
        outgoingCard = null;
        outgoingCardSpring.snap(0.0F);
        openingViewport = null;
        accordion.clear();
        cardSpring.snap(0.0F);
        pendingMapNode = focusNode;
        mapTransition.snap(0.0F);
        mapTransition.setTarget(1.0F);
        fitMap();
    }

    private void focusNode(Identifier id) {
        var node = guide.node(id).orElse(null);
        if (node == null) return;
        var source = nodeVisualRect(node);
        mapOffsetX = width / 2.0F - source.centerX() * mapScale;
        mapOffsetY = height / 2.0F - source.centerY() * mapScale;
        if (selectedNode == null) openNode(node);
        else if (selectedNode != node) switchNode(node);
    }

    private void extractGuideTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        float pointerX = worldX(mouseX);
        float pointerY = worldY(mouseY);
        if (selectedNode == null) {
            for (GuideDefinition.GuideNode node : guide.nodes()) {
                Rect rect = nodeIconRect(nodeVisualRect(node));
                if (contains(rect, pointerX, pointerY)) {
                    graphics.setTooltipForNextFrame(font, itemStack(node.item()), mouseX, mouseY);
                    return;
                }
            }
        } else if (sourceRect != null && contains(expandedIconRect(), pointerX, pointerY)) {
            graphics.setTooltipForNextFrame(font, itemStack(selectedNode.item()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int searchLeft = (width - SEARCH_WIDTH) / 2;
        int searchTop = height - SEARCH_BOTTOM - SEARCH_HEIGHT;
        boolean inSearchFrame =
                event.x() >= searchLeft
                        && event.x() < searchLeft + SEARCH_WIDTH
                        && event.y() >= searchTop
                        && event.y() < searchTop + SEARCH_HEIGHT;
        if (searchBox != null && searchBox.isFocused() && !inSearchFrame) clearFocus();
        if (inSearchFrame) {
            if (searchBox != null && searchBox.isMouseOver(event.x(), event.y()))
                return super.mouseClicked(event, doubleClick);
            if (searchBox != null) setInitialFocus(searchBox);
            return true;
        }
        if (handleNavigationClick(event.x(), event.y(), doubleClick)) return true;
        float mouseX = worldX(event.x());
        float mouseY = worldY(event.y());
        if (event.button() == 2) {
            panning = true;
            return true;
        }
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        if (selectedNode != null && sourceRect != null) {
            Rect card = currentCardRect();
            if (!contains(card, mouseX, mouseY)) {
                var other = nodeAt(mouseX, mouseY);
                if (other != null && other != selectedNode) {
                    switchNode(other);
                } else if (doubleClick && other == null) {
                    closeCard();
                } else {
                    panning = true;
                }
                return true;
            }
            if (mouseX >= card.right() - 34
                    && mouseX < card.right() - 6
                    && mouseY >= card.top() + 8
                    && mouseY < card.top() + 36) {
                closeCard();
                return true;
            }
            if (clickArticle(mouseX, mouseY, card)) {
                return true;
            }
            draggingScene = sceneAt(mouseX, mouseY);
            if (draggingScene != null) {
                if (draggingScene.mouseClicked(mouseX, mouseY, doubleClick)) {
                    var scene = draggingScene;
                    draggingScene = null;
                    minecraft.setScreen(new SceneScreen(this, scene));
                }
                return true;
            }
            String link = linkAt(mouseX, mouseY);
            if (link != null) openLink(link);
            return true;
        }

        var node = nodeAt(mouseX, mouseY);
        if (node != null) openNode(node);
        else panning = true;
        return true;
    }

    private @Nullable GuideDefinition.GuideNode nodeAt(float x, float y) {
        for (var node : guide.nodes()) if (contains(nodeVisualRect(node), x, y)) return node;
        return null;
    }

    private boolean clickArticle(float mouseX, float mouseY, Rect card) {
        float y = card.top() + CARD_HEADER_HEIGHT;
        float x = card.left() + 12;
        float width = card.width() - 24;
        for (Identifier articleId : selectedNode.articleIds()) {
            GuideDefinition.GuideArticle article = articles.get(articleId);
            if (article == null) {
                continue;
            }
            if (contains(new Rect(x, y, width, ARTICLE_ROW_HEIGHT), mouseX, mouseY)) {
                accordion.toggle(articleId);
                return true;
            }
            y += ARTICLE_ROW_HEIGHT + ARTICLE_ROW_GAP;
            y += articleVisibleHeight(article);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingMapListScrollbar) {
            updateMapListScroll(event.y());
            return true;
        }
        if (searchBox != null && searchBox.isFocused())
            return super.mouseDragged(event, dragX, dragY);
        if (draggingScene != null) {
            draggingScene.mouseDragged(dragX / mapScale, dragY / mapScale);
            return true;
        }
        if (panning) {
            var before = cardScreenBounds();
            mapOffsetX += (float) dragX;
            mapOffsetY += (float) dragY;
            collapseAtViewportEdge(before);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        panning = false;
        draggingMapListScrollbar = false;
        if (draggingScene != null) draggingScene.mouseReleased();
        draggingScene = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<GuideDefinition> maps = maps();
        Rect listViewport = mapListViewport(maps);
        if (contains(listViewport, (float) mouseX, (float) mouseY)) {
            mapListScroll =
                    Mth.clamp(
                            mapListScroll - (float) scrollY * MAP_LIST_ROW_HEIGHT * 2.0F,
                            0.0F,
                            mapListMaxScroll(maps, listViewport));
            return true;
        }
        SceneView scene = sceneAt(worldX(mouseX), worldY(mouseY));
        if (scene != null) {
            scene.zoom(scrollY);
            return true;
        }
        if (selectedNode != null && sourceRect != null) {
            Rect card = currentCardRect();
            GuideDefinition.GuideArticle article = selectedArticle();
            if (article != null && contains(card, worldX(mouseX), worldY(mouseY))) {
                int maxScroll = Math.max(0, bodyHeight(article) - ARTICLE_VIEWPORT_HEIGHT);
                accordion.scroll(-scrollY * 22.0, maxScroll);
                return true;
            }
        }
        zoomMap((float) mouseX, (float) mouseY, (float) scrollY);
        return true;
    }

    private void zoomMap(float mouseX, float mouseY, float scrollY) {
        var before = cardScreenBounds();
        float oldScale = mapScale;
        mapScale = Mth.clamp(mapScale + scrollY * 0.08F, MIN_MAP_SCALE, MAX_MAP_SCALE);
        if (mapScale == oldScale) {
            return;
        }
        float worldX = (mouseX - mapOffsetX) / oldScale;
        float worldY = (mouseY - mapOffsetY) / oldScale;
        mapOffsetX = mouseX - worldX * mapScale;
        mapOffsetY = mouseY - worldY * mapScale;
        collapseAtViewportEdge(before);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchBox != null
                && searchBox.isFocused()
                && event.key() == InputConstants.KEY_RETURN) {
            List<SearchResult> results = searchResults();
            if (!results.isEmpty()) activateSearchResult(results.getFirst());
            return true;
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            if (searchBox != null && searchBox.isFocused()) {
                clearFocus();
                return true;
            }
            if (selectedNode != null && accordion.getSelected() != null) {
                accordion.collapse();
                return true;
            }
            if (selectedNode != null) {
                closeCard();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    boolean isSubject(Identifier item) {
        return selectedNode != null && selectedNode.item().equals(item);
    }

    Identifier guideId() {
        return guide.id();
    }

    boolean recallNode(Identifier id) {
        var node =
                guide.nodes().stream()
                        .filter(candidate -> candidate.id().equals(id))
                        .findFirst()
                        .orElse(null);
        if (node == null) return false;
        if (node == selectedNode) {
            pendingNode = null;
            cardSpring.setTarget(1);
        } else if (selectedNode != null) {
            switchNode(node);
        } else {
            var source = nodeVisualRect(node);
            mapOffsetX = width / 2F - source.centerX() * mapScale;
            mapOffsetY = height / 2F - source.centerY() * mapScale;
            openNode(node);
        }
        return true;
    }

    @Override
    public void onClose() {
        if (draggingScene != null) draggingScene.mouseReleased();
        minecraft.setScreen(returnScreen);
    }

    private void openNode(GuideDefinition.GuideNode node) {
        pendingNode = null;
        panning = false;
        outgoingCard = null;
        outgoingCardSpring.snap(0.0F);
        selectedNode = node;
        sourceRect = nodeVisualRect(node);
        openingViewport = new Rect(worldX(0), worldY(0), width / mapScale, height / mapScale);
        accordion.clear();
        cardSpring.snap(0.0F);
        cardSpring.setTarget(1.0F);
    }

    private void switchNode(GuideDefinition.GuideNode node) {
        if (selectedNode == null || sourceRect == null) {
            openNode(node);
            return;
        }
        outgoingCard =
                new OutgoingCard(selectedNode, sourceRect, currentCardRect(), expandedIconRect());
        outgoingCardSpring.snap(1.0F);
        outgoingCardSpring.setTarget(0.0F);
        pendingNode = null;
        panning = false;
        if (draggingScene != null) draggingScene.mouseReleased();
        draggingScene = null;
        selectedNode = node;
        sourceRect = nodeVisualRect(node);
        accordion.clear();
        cardSpring.snap(0.0F);
        cardSpring.setTarget(1.0F);
    }

    private @Nullable GuideViewport.Bounds cardScreenBounds() {
        if (selectedNode == null || sourceRect == null || cardSpring.getTarget() == 0) return null;
        Rect card = currentCardRect();
        return new GuideViewport.Bounds(
                card.left() * mapScale + mapOffsetX,
                card.top() * mapScale + mapOffsetY,
                card.right() * mapScale + mapOffsetX,
                card.bottom() * mapScale + mapOffsetY);
    }

    private void collapseAtViewportEdge(@Nullable GuideViewport.Bounds before) {
        var after = cardScreenBounds();
        if (before != null
                && after != null
                && GuideViewport.shouldCollapse(before, after, width, height)) closeCard();
    }

    private void closeCard() {
        pendingNode = null;
        panning = false;
        if (draggingScene != null) draggingScene.mouseReleased();
        draggingScene = null;
        cardSpring.setTarget(0.0F);
        accordion.collapse();
    }

    private float worldX(double screenX) {
        return (float) ((screenX - mapOffsetX) / mapScale);
    }

    private float worldY(double screenY) {
        return (float) ((screenY - mapOffsetY) / mapScale);
    }

    private Rect nodeVisualRect(GuideDefinition.GuideNode node) {
        LayoutNode layoutNode = layoutNodes.get(node.id());
        float x = layoutNode == null ? node.x() : layoutNode.x;
        float y = layoutNode == null ? node.y() : layoutNode.y;
        return new Rect(
                x + (NODE_WIDTH - NODE_BODY_WIDTH) / 2.0F,
                y + 1.0F,
                NODE_BODY_WIDTH,
                NODE_BODY_HEIGHT);
    }

    private Rect expandedIconRect() {
        Rect current = currentCardRect();
        return Rect.lerp(
                nodeIconRect(sourceRect),
                new Rect(current.left() + 16, current.top() + 16, 16, 16),
                Mth.clamp(cardSpring.getValue(), 0.0F, 1.0F));
    }

    private Rect currentCardRect() {
        return Rect.lerp(
                sourceRect, cardTargetRect(), Mth.clamp(cardSpring.getValue(), 0.0F, 1.0F));
    }

    private Rect cardTargetRect() {
        float cardWidth = cardWidth();
        float cardHeight =
                CARD_HEADER_HEIGHT + validArticleCount() * (ARTICLE_ROW_HEIGHT + ARTICLE_ROW_GAP);
        for (Identifier articleId : selectedNode.articleIds()) {
            GuideDefinition.GuideArticle article = articles.get(articleId);
            if (article != null) cardHeight += articleVisibleHeight(article);
        }
        cardHeight += CARD_BOTTOM_PADDING;
        float centerX = sourceRect.centerX();
        float centerY = sourceRect.centerY();
        float minLeft = openingViewport.left() + 16;
        float minTop = openingViewport.top() + 16;
        float left =
                Mth.clamp(
                        centerX - cardWidth / 2,
                        minLeft,
                        Math.max(minLeft, openingViewport.right() - cardWidth - 16));
        float top =
                Mth.clamp(
                        centerY - cardHeight / 2,
                        minTop,
                        Math.max(minTop, openingViewport.bottom() - cardHeight - 16));
        return new Rect(left, top, cardWidth, cardHeight);
    }

    private float cardWidth() {
        return Math.clamp(openingViewport.width() - 32, 180, CARD_WIDTH);
    }

    private int validArticleCount() {
        int count = 0;
        if (selectedNode != null) {
            for (Identifier articleId : selectedNode.articleIds()) {
                if (articles.containsKey(articleId)) {
                    count++;
                }
            }
        }
        return count;
    }

    private @Nullable GuideDefinition.GuideArticle selectedArticle() {
        Identifier selected = accordion.getSelected();
        return selected == null ? null : articles.get(selected);
    }

    private float articleVisibleHeight(GuideDefinition.GuideArticle article) {
        float progress = accordion.progress(article.id());
        return progress == 0
                ? 0
                : Math.min(bodyHeight(article), ARTICLE_VIEWPORT_HEIGHT) * progress;
    }

    private int bodyHeight(GuideDefinition.GuideArticle article) {
        return articleLayout(article).height();
    }

    private GuideMarkdown.Layout articleLayout(GuideDefinition.GuideArticle article) {
        int width = Math.max(1, Math.round(cardWidth() - 36));
        return articleLayouts.computeIfAbsent(
                new LayoutKey(article.id(), width),
                key ->
                        GuideMarkdown.layout(
                                documents.get(article.id()),
                                font,
                                width,
                                guide.id().getNamespace(),
                                article.resourceRoot(),
                                article.content()));
    }

    private @Nullable SceneView sceneView(
            GuideDefinition.GuideArticle article, GuideMarkdown.Element element) {
        Identifier id =
                Identifier.tryParse(
                        element.scene().contains(":")
                                ? element.scene()
                                : article.id().getNamespace() + ":" + element.scene());
        if (id == null) return null;
        String key = article.id() + "/" + element.y() + "/" + id;
        return sceneViews.computeIfAbsent(key, ignored -> new SceneView(id));
    }

    private @Nullable ArticleHit articleAt(float mouseX, float mouseY) {
        if (selectedNode == null || sourceRect == null) return null;
        Rect card = currentCardRect();
        if (!contains(
                new Rect(
                        card.left() + 12,
                        card.top() + CARD_HEADER_HEIGHT,
                        card.width() - 24,
                        Math.max(0, card.height() - CARD_HEADER_HEIGHT - CARD_BOTTOM_PADDING)),
                mouseX,
                mouseY)) return null;
        float y = card.top() + CARD_HEADER_HEIGHT;
        for (Identifier id : selectedNode.articleIds()) {
            var article = articles.get(id);
            if (article == null) continue;
            y += ARTICLE_ROW_HEIGHT + ARTICLE_ROW_GAP;
            float height = articleVisibleHeight(article);
            if (mouseY >= y && mouseY < y + height)
                return new ArticleHit(article, card.left() + 18, y - (float) accordion.scroll(id));
            y += height;
        }
        return null;
    }

    private @Nullable SceneView sceneAt(float mouseX, float mouseY) {
        ArticleHit hit = articleAt(mouseX, mouseY);
        if (hit == null) return null;
        for (var element : articleLayout(hit.article()).elements()) {
            if (element.kind() == GuideMarkdown.Kind.SCENE
                    && contains(
                            new Rect(
                                    hit.x() + element.x(),
                                    hit.y() + element.y(),
                                    element.width(),
                                    element.height()),
                            mouseX,
                            mouseY)) return sceneView(hit.article(), element);
        }
        return null;
    }

    private @Nullable String linkAt(float mouseX, float mouseY) {
        ArticleHit hit = articleAt(mouseX, mouseY);
        if (hit == null) return null;
        for (var element : articleLayout(hit.article()).elements()) {
            if (element.kind() != GuideMarkdown.Kind.TEXT) continue;
            float x = mouseX - Math.round(hit.x() + element.x());
            float y = mouseY - Math.round(hit.y() + element.y());
            if (x < 0 || y < 0 || y >= font.lineHeight * element.scale()) continue;
            float[] remaining = {x / element.scale()};
            String[] destination = {null};
            element.text()
                    .accept(
                            (index, style, codepoint) -> {
                                remaining[0] -=
                                        font.getSplitter()
                                                .stringWidth(
                                                        net.minecraft.util.FormattedCharSequence
                                                                .codepoint(codepoint, style));
                                if (remaining[0] < 0) {
                                    destination[0] = style.getInsertion();
                                    return false;
                                }
                                return true;
                            });
            if (destination[0] != null) return destination[0];
        }
        return null;
    }

    private void openLink(String destination) {
        if (!GuideMarkdown.allowedLink(destination)) return;
        if (destination.startsWith("guide:")) {
            String target = destination.substring("guide:".length());
            Identifier id =
                    Identifier.tryParse(
                            target.contains(":")
                                    ? target
                                    : guide.id().getNamespace() + ":" + target);
            if (id == null) return;
            var node = guide.nodeForArticle(id).orElse(null);
            if (node != null) {
                if (node != selectedNode) openNode(node);
                if (!id.equals(accordion.getSelected())) accordion.toggle(id);
            }
        } else ConfirmLinkScreen.confirmLinkNow(this, destination);
    }

    private static ItemStack itemStack(Identifier itemId) {
        return new ItemStack(
                net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .get(itemId)
                        .map(net.minecraft.core.Holder.Reference::value)
                        .orElse(net.minecraft.world.item.Items.BARRIER));
    }

    private void fitMap() {
        mapScale =
                Mth.clamp(
                        Math.min((width - 32.0F) / MAP_WIDTH, (height - 32.0F) / MAP_HEIGHT),
                        MIN_MAP_SCALE,
                        1.15F);
        mapOffsetX = (width - MAP_WIDTH * mapScale) / 2.0F;
        mapOffsetY = (height - MAP_HEIGHT * mapScale) / 2.0F;
    }

    private void drawLine(
            GuiGraphicsExtractor graphics,
            float x1,
            float y1,
            float x2,
            float y2,
            int color,
            float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.001F || thickness <= 0.0F || (color >>> 24) == 0) {
            return;
        }
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x1, y1);
        pose.rotate((float) Math.atan2(dy, dx));

        // The transformed Y axis is perpendicular to the line. Include both view zoom and
        // Minecraft GUI scale so each alpha ramp spans one framebuffer pixel at every zoom.
        float pixelsPerUnit =
                (float) Math.hypot(pose.m10(), pose.m11()) * minecraft.getWindow().getGuiScale();
        float feather = 1.0F / Math.max(pixelsPerUnit, 0.0001F);
        float coreWidth = Math.max(0.0F, thickness - feather);
        int coveredColor = withAlpha(color, Math.min(1.0F, thickness / feather));
        int transparentColor = coveredColor & 0x00FFFFFF;
        float halfCore = coreWidth / 2.0F;

        // Non-overlapping bands preserve brightness. Transparent vertices keep the same RGB
        // to avoid dark fringes. Subpixel lines use two ramps with proportionally lower alpha.
        drawLineBand(
                graphics, length, -halfCore - feather, feather, transparentColor, coveredColor);
        if (coreWidth > 0.0F) {
            drawLineBand(graphics, length, -halfCore, coreWidth, coveredColor, coveredColor);
        }
        drawLineBand(graphics, length, halfCore, feather, coveredColor, transparentColor);
        // The unfeathered end caps lie beneath the opaque node/card boxes.
        pose.popMatrix();
    }

    private static void drawLineBand(
            GuiGraphicsExtractor graphics,
            float length,
            float top,
            float height,
            int topColor,
            int bottomColor) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(0.0F, top);
        pose.scale(length, height);
        graphics.fillGradient(0, 0, 1, 1, topColor, bottomColor);
        pose.popMatrix();
    }

    private static boolean contains(Rect rect, float x, float y) {
        return x >= rect.left() && y >= rect.top() && x < rect.right() && y < rect.bottom();
    }

    private static float smoothstep(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static int withAlpha(int color, float alpha) {
        int sourceAlpha = color >>> 24;
        int outputAlpha = Mth.clamp(Math.round(sourceAlpha * Mth.clamp(alpha, 0.0F, 1.0F)), 0, 255);
        return outputAlpha << 24 | color & 0x00FFFFFF;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return false;
    }

    private static final class LayoutNode {
        private final GuideDefinition.GuideNode node;
        private final float homeX;
        private final float homeY;
        private float x;
        private float y;
        private float velocityX;
        private float velocityY;
        private float forceX;
        private float forceY;

        private LayoutNode(GuideDefinition.GuideNode node) {
            this.node = node;
            this.homeX = node.x();
            this.homeY = node.y();
            this.x = homeX;
            this.y = homeY;
        }
    }

    private record Rect(float left, float top, float width, float height) {
        private float right() {
            return left + width;
        }

        private float bottom() {
            return top + height;
        }

        private float centerX() {
            return left + width / 2.0F;
        }

        private float centerY() {
            return top + height / 2.0F;
        }

        private static Rect lerp(Rect from, Rect to, float progress) {
            float t = Mth.clamp(progress, 0.0F, 1.0F);
            return new Rect(
                    Mth.lerp(t, from.left, to.left),
                    Mth.lerp(t, from.top, to.top),
                    Mth.lerp(t, from.width, to.width),
                    Mth.lerp(t, from.height, to.height));
        }
    }

    private record LayoutKey(Identifier article, int width) {}

    private record OutgoingCard(
            GuideDefinition.GuideNode node, Rect source, Rect card, Rect icon) {}

    private record ImageSize(int width, int height) {}

    private record SearchResult(GuideDefinition guide, GuideDefinition.GuideNode node) {}

    private record ArticleHit(GuideDefinition.GuideArticle article, float x, float y) {}
}
