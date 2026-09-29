package com.mementoinabyss.recall.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Immutable content model for a resource-driven Recall guide. */
public record GuideDefinition(
        Identifier id,
        String title,
        String subtitle,
        @Nullable Identifier icon,
        @Nullable Identifier background,
        int layer,
        @Nullable Identifier startNode,
        List<GuideArticle> articles,
        List<GuideNode> nodes,
        List<GuideConnection> connections) {
    public GuideDefinition(
            Identifier id,
            String title,
            String subtitle,
            @Nullable Identifier icon,
            @Nullable Identifier startNode,
            List<GuideArticle> articles,
            List<GuideNode> nodes,
            List<GuideConnection> connections) {
        this(id, title, subtitle, icon, null, 0, startNode, articles, nodes, connections);
    }

    public GuideDefinition(
            Identifier id,
            String title,
            @Nullable Identifier icon,
            @Nullable Identifier startNode,
            List<GuideArticle> articles,
            List<GuideNode> nodes,
            List<GuideConnection> connections) {
        this(id, title, "", icon, null, 0, startNode, articles, nodes, connections);
    }

    public GuideDefinition(
            Identifier id,
            String title,
            @Nullable Identifier startNode,
            List<GuideArticle> articles,
            List<GuideNode> nodes,
            List<GuideConnection> connections) {
        this(id, title, "", null, null, 0, startNode, articles, nodes, connections);
    }

    public GuideDefinition {
        subtitle = subtitle == null ? "" : subtitle;
        if (layer < 0) throw new IllegalArgumentException("Guide layer must not be negative");
        articles = List.copyOf(articles);
        nodes = List.copyOf(nodes);
        connections = List.copyOf(connections);
    }

    public Map<Identifier, GuideArticle> articlesById() {
        return articles.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(GuideArticle::id, a -> a));
    }

    public Optional<GuideNode> node(Identifier nodeId) {
        return nodes.stream().filter(node -> node.id().equals(nodeId)).findFirst();
    }

    public Optional<GuideNode> nodeForArticle(Identifier articleId) {
        return nodes.stream().filter(node -> node.articleIds().contains(articleId)).findFirst();
    }

    public record GuideArticle(
            Identifier id,
            String title,
            String summary,
            String content,
            String resourceRoot,
            List<String> lines) {
        public GuideArticle {
            lines = List.copyOf(lines);
        }

        public GuideArticle(
                Identifier id, String title, String summary, String content, List<String> lines) {
            this(id, title, summary, content, "", lines);
        }
    }

    public record GuideNode(
            Identifier id,
            Identifier item,
            float x,
            float y,
            String title,
            String summary,
            List<Identifier> articleIds) {
        public GuideNode {
            articleIds = List.copyOf(articleIds);
        }
    }

    public record GuideConnection(Identifier from, Identifier to, String kind) {}
}
