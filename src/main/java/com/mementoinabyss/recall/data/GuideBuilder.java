package com.mementoinabyss.recall.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mementoinabyss.recall.data.GuideDefinition.GuideArticle;
import com.mementoinabyss.recall.data.GuideDefinition.GuideConnection;
import com.mementoinabyss.recall.data.GuideDefinition.GuideNode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Fluent authoring for explicit guide manifests, validated before any JSON is written. */
public final class GuideBuilder {
    private final Identifier id;
    private final String title;
    private final Map<Identifier, GuideArticle> articles = new LinkedHashMap<>();
    private final Map<Identifier, GuideNode> nodes = new LinkedHashMap<>();
    private final Set<GuideConnection> connections = new LinkedHashSet<>();
    @Nullable private Identifier icon;
    @Nullable private Identifier background;
    @Nullable private Identifier startNode;
    private int layer;
    private String subtitle = "";

    public GuideBuilder(Identifier id, String title) {
        this.id = id;
        this.title = title;
    }

    public GuideBuilder icon(Identifier icon) {
        this.icon = icon;
        return this;
    }

    public GuideBuilder background(Identifier background) {
        this.background = background;
        return this;
    }

    public GuideBuilder layer(int layer) {
        if (layer < 0) throw new IllegalArgumentException("Guide layer must not be negative");
        this.layer = layer;
        return this;
    }

    public GuideBuilder subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public GuideBuilder start(String node) {
        startNode = identifier(node);
        return this;
    }

    public GuideBuilder article(String name, String title, String summary, String content) {
        Identifier articleId = identifier(name);
        if (content.isBlank())
            throw new IllegalArgumentException("Missing content for " + articleId);
        if (articles.putIfAbsent(
                        articleId, new GuideArticle(articleId, title, summary, content, List.of()))
                != null) {
            throw new IllegalArgumentException("Duplicate guide article " + articleId);
        }
        return this;
    }

    public GuideBuilder node(
            String name,
            Identifier item,
            float x,
            float y,
            String title,
            String summary,
            String... articleNames) {
        Identifier nodeId = identifier(name);
        if (!Float.isFinite(x) || !Float.isFinite(y))
            throw new IllegalArgumentException("Invalid node position " + nodeId);
        List<Identifier> articleIds = Arrays.stream(articleNames).map(this::identifier).toList();
        if (nodes.putIfAbsent(nodeId, new GuideNode(nodeId, item, x, y, title, summary, articleIds))
                != null) {
            throw new IllegalArgumentException("Duplicate guide node " + nodeId);
        }
        return this;
    }

    public GuideBuilder connect(String from, String to) {
        Identifier first = identifier(from);
        Identifier second = identifier(to);
        if (first.equals(second))
            throw new IllegalArgumentException("Self connection for " + first);
        boolean forward = first.toString().compareTo(second.toString()) < 0;
        connections.add(
                new GuideConnection(forward ? first : second, forward ? second : first, "related"));
        return this;
    }

    public GuideDefinition build() {
        if (startNode != null && !nodes.containsKey(startNode)) {
            throw new IllegalStateException("Unknown start node " + startNode);
        }
        for (GuideNode node : nodes.values()) {
            for (Identifier article : node.articleIds()) {
                if (!articles.containsKey(article))
                    throw new IllegalStateException(
                            "Node " + node.id() + " references unknown article " + article);
            }
        }
        for (GuideConnection connection : connections) {
            if (!nodes.containsKey(connection.from()) || !nodes.containsKey(connection.to())) {
                throw new IllegalStateException(
                        "Connection references unknown node: " + connection);
            }
        }
        return new GuideDefinition(
                id,
                title,
                subtitle,
                icon,
                background,
                layer,
                startNode,
                List.copyOf(articles.values()),
                List.copyOf(nodes.values()),
                List.copyOf(connections));
    }

    public JsonObject toJson() {
        GuideDefinition guide = build();
        JsonObject root = new JsonObject();
        root.addProperty("id", guide.id().toString());
        root.addProperty("title", guide.title());
        if (!guide.subtitle().isBlank()) root.addProperty("subtitle", guide.subtitle());
        if (guide.icon() != null) root.addProperty("icon", guide.icon().toString());
        if (guide.background() != null)
            root.addProperty("background", guide.background().toString());
        if (guide.layer() > 0) root.addProperty("layer", guide.layer());
        if (guide.startNode() != null) root.addProperty("start_node", guide.startNode().toString());
        JsonArray articleArray = new JsonArray();
        for (GuideArticle article : guide.articles()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", article.id().toString());
            json.addProperty("title", article.title());
            json.addProperty("summary", article.summary());
            json.addProperty("content", article.content());
            articleArray.add(json);
        }
        root.add("articles", articleArray);
        JsonArray nodeArray = new JsonArray();
        for (GuideNode node : guide.nodes()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", node.id().toString());
            json.addProperty("item", node.item().toString());
            json.addProperty("x", node.x());
            json.addProperty("y", node.y());
            json.addProperty("title", node.title());
            json.addProperty("summary", node.summary());
            JsonArray articleIds = new JsonArray();
            node.articleIds().forEach(article -> articleIds.add(article.toString()));
            json.add("articles", articleIds);
            nodeArray.add(json);
        }
        root.add("nodes", nodeArray);
        JsonArray connectionArray = new JsonArray();
        for (GuideConnection connection : guide.connections()) {
            JsonObject json = new JsonObject();
            json.addProperty("from", connection.from().toString());
            json.addProperty("to", connection.to().toString());
            json.addProperty("kind", connection.kind());
            connectionArray.add(json);
        }
        root.add("connections", connectionArray);
        return root;
    }

    private Identifier identifier(String value) {
        return value.contains(":")
                ? Identifier.parse(value)
                : Identifier.fromNamespaceAndPath(id.getNamespace(), value);
    }
}
