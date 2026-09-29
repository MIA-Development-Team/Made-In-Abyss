package com.mementoinabyss.recall.client;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.jetbrains.annotations.Nullable;

/** CommonMark parsing is independent of layout; a screen caches each layout at its reading width. */
final class GuideMarkdown {
    private static final Parser PARSER =
            Parser.builder().extensions(List.of(StrikethroughExtension.create())).build();

    static Node parse(List<String> lines) {
        return PARSER.parse(String.join("\n", lines));
    }

    static boolean allowedLink(String destination) {
        try {
            URI uri = URI.create(destination);
            return ("https".equalsIgnoreCase(uri.getScheme())
                            || "http".equalsIgnoreCase(uri.getScheme()))
                    ? uri.getHost() != null
                    : "guide".equals(uri.getScheme()) && !uri.getSchemeSpecificPart().isBlank();
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    static MutableComponent inline(Node parent, Style style) {
        MutableComponent result = Component.empty();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext())
            result.append(inlineNode(node, style));
        return result;
    }

    private static MutableComponent inline(List<Node> nodes, Style style) {
        MutableComponent result = Component.empty();
        for (Node node : nodes) result.append(inlineNode(node, style));
        return result;
    }

    private static MutableComponent inlineNode(Node node, Style style) {
        return switch (node) {
            case Text text -> Component.literal(text.getLiteral()).setStyle(style);
            case Code code ->
                    Component.literal(code.getLiteral()).setStyle(style.withColor(0xE3CFA0));
            case SoftLineBreak ignored -> Component.literal(" ").setStyle(style);
            case HardLineBreak ignored -> Component.literal("\n").setStyle(style);
            case StrongEmphasis ignored -> inline(node, style.withBold(true));
            case Emphasis ignored -> inline(node, style.withItalic(true));
            case Strikethrough ignored -> inline(node, style.withStrikethrough(true));
            case Link link ->
                    inline(
                            node,
                            allowedLink(link.getDestination())
                                    ? style.withColor(0xC0C0FF)
                                            .withUnderlined(true)
                                            .withInsertion(link.getDestination())
                                    : style);
            case Image ignored -> inline(node, style);
            case HtmlInline html -> Component.literal(html.getLiteral()).setStyle(style);
            default -> inline(node, style);
        };
    }

    static Layout layout(Node document, Font font, int width) {
        return layout(document, font, width, "", "", "");
    }

    static Layout layout(
            Node document,
            Font font,
            int width,
            String namespace,
            String resourceRoot,
            String articlePath) {
        Builder builder = new Builder(font, namespace, resourceRoot, articlePath);
        builder.blocks(document, 0, width);
        return new Layout(List.copyOf(builder.elements), builder.y);
    }

    record Layout(List<Element> elements, int height) {}

    record Element(
            Kind kind,
            int x,
            int y,
            int width,
            int height,
            float scale,
            FormattedCharSequence text,
            String scene,
            @Nullable ImageTexture image) {
        Element(
                Kind kind,
                int x,
                int y,
                int width,
                int height,
                float scale,
                FormattedCharSequence text,
                String scene) {
            this(kind, x, y, width, height, scale, text, scene, null);
        }
    }

    record ImageTexture(Identifier id, int width, int height) {}

    enum Kind {
        TEXT,
        CODE,
        QUOTE,
        RULE,
        SCENE,
        IMAGE
    }

    private static final class Builder {
        final Font font;
        final String namespace;
        final String resourceRoot;
        final String articlePath;
        final List<Element> elements = new ArrayList<>();
        int y;

        Builder(Font font, String namespace, String resourceRoot, String articlePath) {
            this.font = font;
            this.namespace = namespace;
            this.resourceRoot = resourceRoot;
            this.articlePath = articlePath;
        }

        void blocks(Node parent, int x, int width) {
            for (Node node = parent.getFirstChild(); node != null; node = node.getNext())
                block(node, x, width);
        }

        void block(Node node, int x, int width) {
            width = Math.max(16, width);
            switch (node) {
                case Heading heading -> {
                    y += 4;
                    text(
                            inline(node, Style.EMPTY.withBold(true)),
                            x,
                            width,
                            heading.getLevel() == 1 ? 1.35F : heading.getLevel() == 2 ? 1.15F : 1);
                    y += 5;
                }
                case Paragraph paragraph -> paragraph(paragraph, x, width);
                case BlockQuote ignored -> {
                    int start = y;
                    int index = elements.size();
                    blocks(node, x + 10, width - 10);
                    elements.add(
                            index, new Element(Kind.QUOTE, x, start, 2, y - start, 1, null, null));
                }
                case BulletList ignored -> list(node, x, width, 0);
                case OrderedList ordered -> list(node, x, width, ordered.getMarkerStartNumber());
                case FencedCodeBlock code -> {
                    String[] info = code.getInfo().trim().split("\\s+", 2);
                    if (info.length == 2 && info[0].equals("recall-scene")) {
                        elements.add(
                                new Element(Kind.SCENE, x, y, width, 216, 1, null, info[1].trim()));
                        y += 224;
                    } else code(code.getLiteral(), x, width);
                }
                case IndentedCodeBlock code -> code(code.getLiteral(), x, width);
                case ThematicBreak ignored -> {
                    elements.add(new Element(Kind.RULE, x, y + 5, width, 1, 1, null, null));
                    y += 12;
                }
                case HtmlBlock html -> {
                    text(Component.literal(html.getLiteral()), x, width, 1);
                    y += 6;
                }
                default -> blocks(node, x, width);
            }
        }

        void list(Node list, int x, int width, int number) {
            boolean ordered = list instanceof OrderedList;
            for (Node item = list.getFirstChild(); item != null; item = item.getNext()) {
                String marker = ordered ? number++ + "." : "\u2022";
                int indent = Math.max(14, font.width(marker) + 5);
                elements.add(
                        new Element(
                                Kind.TEXT,
                                x,
                                y,
                                indent,
                                font.lineHeight,
                                1,
                                Component.literal(marker).getVisualOrderText(),
                                null));
                blocks(item, x + indent, width - indent);
            }
        }

        void paragraph(Paragraph paragraph, int x, int width) {
            List<Node> segment = new ArrayList<>();
            for (Node child = paragraph.getFirstChild(); child != null; child = child.getNext()) {
                if (child instanceof Image image) {
                    if (!segment.isEmpty()) {
                        text(inline(segment, Style.EMPTY), x, width, 1);
                        segment.clear();
                    }
                    image(image, x, width);
                } else {
                    segment.add(child);
                }
            }
            if (!segment.isEmpty()) text(inline(segment, Style.EMPTY), x, width, 1);
            y += 6;
        }

        void image(Image image, int x, int width) {
            ImageTexture texture = imageTexture(image.getDestination());
            if (texture == null) {
                text(inline(image, Style.EMPTY), x, width, 1);
                return;
            }
            int maxHeight = Math.max(48, Math.min(256, width * 3 / 4));
            float scale =
                    Math.min(
                            1,
                            Math.min(
                                    width / (float) texture.width(),
                                    maxHeight / (float) texture.height()));
            int displayWidth = Math.max(1, Math.round(texture.width() * scale));
            int displayHeight = Math.max(1, Math.round(texture.height() * scale));
            elements.add(
                    new Element(
                            Kind.IMAGE, x, y, displayWidth, displayHeight, 1, null, null, texture));
            y += displayHeight + 6;
        }

        @Nullable ImageTexture imageTexture(String destination) {
            try {
                URI uri = URI.create(destination);
                Identifier texture;
                if ("texture".equalsIgnoreCase(uri.getScheme())) {
                    if (uri.getRawQuery() != null || uri.getRawFragment() != null) return null;
                    String value = uri.getSchemeSpecificPart();
                    if (value.endsWith(".png")) value = value.substring(0, value.length() - 4);
                    texture = Identifier.tryParse(value);
                    if (texture == null) return null;
                } else {
                    if (uri.getScheme() != null
                            || uri.getRawAuthority() != null
                            || uri.getRawQuery() != null
                            || uri.getRawFragment() != null
                            || uri.getPath().startsWith("/")
                            || !uri.getPath().toLowerCase(java.util.Locale.ROOT).endsWith(".png")
                            || namespace.isBlank()) return null;
                    String path = uri.getPath();
                    if (path.startsWith("textures/")) {
                        texture =
                                Identifier.tryBuild(
                                        namespace, path.substring(9, path.length() - 4));
                        if (texture == null) return null;
                    } else if (resourceRoot.isBlank() || articlePath.isBlank()) {
                        return null;
                    } else {
                        Path root = Path.of(resourceRoot).normalize();
                        Path article = root.resolve(articlePath).normalize();
                        Path parent = article.getParent();
                        if (!article.startsWith(root) || parent == null) return null;
                        Path resolved = parent.resolve(uri.getPath()).normalize();
                        if (!resolved.startsWith(root)) return null;
                        String texturePath = resolved.toString().replace('\\', '/');
                        texturePath = texturePath.substring(0, texturePath.length() - 4);
                        texture = Identifier.tryBuild(namespace, texturePath);
                        if (texture == null) return null;
                    }
                }
                Identifier file = texture.withPath("textures/" + texture.getPath() + ".png");
                Resource resource =
                        Minecraft.getInstance().getResourceManager().getResource(file).orElse(null);
                if (resource == null) return null;
                try (var stream = resource.open();
                        ImageInputStream input = ImageIO.createImageInputStream(stream)) {
                    if (input == null) return null;
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                    if (!readers.hasNext()) return null;
                    ImageReader reader = readers.next();
                    try {
                        reader.setInput(input, true, true);
                        int imageWidth = reader.getWidth(0);
                        int imageHeight = reader.getHeight(0);
                        if (imageWidth < 1
                                || imageHeight < 1
                                || imageWidth > 16384
                                || imageHeight > 16384) return null;
                        return new ImageTexture(file, imageWidth, imageHeight);
                    } finally {
                        reader.dispose();
                    }
                }
            } catch (IOException | IllegalArgumentException exception) {
                return null;
            }
        }

        void code(String literal, int x, int width) {
            int start = y;
            int index = elements.size();
            y += 5;
            // Preserve indentation and empty lines; wrap long code inside the article viewport.
            String body =
                    literal.endsWith("\n") ? literal.substring(0, literal.length() - 1) : literal;
            for (String line : body.split("\n", -1))
                text(
                        Component.literal(line.replace("\t", "    ")).withColor(0xE3CFA0),
                        x + 5,
                        width - 10,
                        1);
            y += 5;
            elements.add(index, new Element(Kind.CODE, x, start, width, y - start, 1, null, null));
            y += 6;
        }

        void text(Component component, int x, int width, float scale) {
            List<FormattedCharSequence> lines =
                    font.split(component, Math.max(1, (int) (width / scale)));
            int lineHeight = (int) Math.ceil((font.lineHeight + 2) * scale);
            for (FormattedCharSequence line : lines) {
                elements.add(new Element(Kind.TEXT, x, y, width, lineHeight, scale, line, null));
                y += lineHeight;
            }
            if (lines.isEmpty()) y += lineHeight;
        }
    }

    private GuideMarkdown() {}
}
