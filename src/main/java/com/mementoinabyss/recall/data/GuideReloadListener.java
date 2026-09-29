package com.mementoinabyss.recall.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the small, stable guide manifest and article text during client resource reloads. */
public final class GuideReloadListener
        extends SimplePreparableReloadListener<Map<Identifier, GuideDefinition>> {
    private final String mapRoot;

    public GuideReloadListener(String resourceRoot) {
        mapRoot = resourceRoot + "/maps";
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GuideReloadListener.class);
    private static final Predicate<Identifier> JSON = id -> id.getPath().endsWith(".json");

    @Override
    protected Map<Identifier, GuideDefinition> prepare(
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, GuideDefinition> result = new HashMap<>();
        for (var entry : resourceManager.listResources(mapRoot, JSON).entrySet()) {
            Identifier resourceId = entry.getKey();
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                GuideDefinition guide = parseGuide(resourceManager, resourceId, root);
                if (result.put(guide.id(), guide) != null) {
                    LOGGER.warn("Duplicate guide id {} from resource {}", guide.id(), resourceId);
                }
            } catch (Exception exception) {
                LOGGER.error("Could not load guide manifest {}", resourceId, exception);
            }
        }
        return result;
    }

    @Override
    protected void apply(
            Map<Identifier, GuideDefinition> guides,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        GuideRepository.replace(guides);
        LOGGER.info("Loaded {} Recall guide map(s)", guides.size());
    }

    private static GuideDefinition parseGuide(
            ResourceManager resourceManager, Identifier resourceId, JsonObject root)
            throws IOException {
        Identifier guideId = identifier(root, "id", resourceId.getNamespace(), mapId(resourceId));
        String title = string(root, "title", guideId.toString());
        String subtitle = string(root, "subtitle", "");
        Identifier startNode = optionalIdentifier(root, "start_node", guideId.getNamespace());
        Identifier icon = optionalIdentifier(root, "icon", guideId.getNamespace());
        Identifier background = optionalIdentifier(root, "background", guideId.getNamespace());
        int layer = root.has("layer") ? root.get("layer").getAsInt() : 0;
        String contentRoot = contentRoot(resourceId);

        List<GuideDefinition.GuideArticle> articles = new ArrayList<>();
        for (JsonElement element : array(root, "articles")) {
            JsonObject articleObject = element.getAsJsonObject();
            Identifier articleId = identifier(articleObject, "id", guideId.getNamespace(), null);
            String contentPath = string(articleObject, "content", "");
            LoadedArticle loaded =
                    loadArticle(
                            resourceManager, resourceId.getNamespace(), contentRoot, contentPath);
            articles.add(
                    new GuideDefinition.GuideArticle(
                            articleId,
                            string(articleObject, "title", articleId.toString()),
                            string(articleObject, "summary", ""),
                            loaded.path(),
                            contentRoot,
                            loaded.lines()));
        }

        List<GuideDefinition.GuideNode> nodes = new ArrayList<>();
        for (JsonElement element : array(root, "nodes")) {
            JsonObject nodeObject = element.getAsJsonObject();
            Identifier nodeId = identifier(nodeObject, "id", guideId.getNamespace(), null);
            Identifier item = identifier(nodeObject, "item", guideId.getNamespace(), null);
            List<Identifier> articleIds = new ArrayList<>();
            for (JsonElement article : array(nodeObject, "articles")) {
                articleIds.add(parseIdentifier(article.getAsString(), guideId.getNamespace()));
            }
            nodes.add(
                    new GuideDefinition.GuideNode(
                            nodeId,
                            item,
                            number(nodeObject, "x", 0),
                            number(nodeObject, "y", 0),
                            string(nodeObject, "title", nodeId.toString()),
                            string(nodeObject, "summary", ""),
                            articleIds));
        }

        List<GuideDefinition.GuideConnection> connections = new ArrayList<>();
        for (JsonElement element : array(root, "connections")) {
            JsonObject connection = element.getAsJsonObject();
            connections.add(
                    new GuideDefinition.GuideConnection(
                            identifier(connection, "from", guideId.getNamespace(), null),
                            identifier(connection, "to", guideId.getNamespace(), null),
                            string(connection, "kind", "related")));
        }

        validateReferences(guideId, articles, nodes, connections);
        return new GuideDefinition(
                guideId,
                title,
                subtitle,
                icon,
                background,
                layer,
                startNode,
                articles,
                nodes,
                connections);
    }

    private static LoadedArticle loadArticle(
            ResourceManager resourceManager,
            String namespace,
            String contentRoot,
            String contentPath)
            throws IOException {
        if (contentPath.isBlank()) return new LoadedArticle(List.of(), "");
        List<Identifier> candidates =
                new ArrayList<>(articleCandidates(namespace, contentRoot, contentPath));
        for (Identifier candidate : candidates) {
            @Nullable Resource resource = resourceManager.getResource(candidate).orElse(null);
            if (resource == null) continue;
            try (BufferedReader reader = resource.openAsReader()) {
                return new LoadedArticle(
                        reader.lines().toList(),
                        candidate.getPath().substring(contentRoot.length() + 1));
            }
        }
        Identifier base =
                Identifier.fromNamespaceAndPath(namespace, contentRoot + "/" + contentPath);
        LOGGER.warn("Guide article resource {} was not found; tried {}", base, candidates);
        return new LoadedArticle(List.of("Missing guide article resource: " + base), contentPath);
    }

    private record LoadedArticle(List<String> lines, String path) {}

    private static Set<Identifier> articleCandidates(
            String namespace, String contentRoot, String contentPath) {
        if (!contentPath.matches("[a-z0-9_-]+(?:/[a-z0-9_-]+)*\\.md"))
            throw new IllegalArgumentException(
                    "Guide article path must be a relative .md path: " + contentPath);
        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        Set<String> languages = new LinkedHashSet<>();
        languages.add(language);
        languages.add("en_us");
        Set<Identifier> candidates = new LinkedHashSet<>();
        int extension = contentPath.length() - 3;
        String stem = contentPath.substring(0, extension);
        for (String locale : languages) {
            // Both forms are accepted so packs can group translations or keep files beside the base
            // article.
            candidates.add(
                    Identifier.fromNamespaceAndPath(
                            namespace, contentRoot + "/" + stem + "." + locale + ".md"));
            int slash = contentPath.lastIndexOf('/');
            String directory = slash < 0 ? "" : contentPath.substring(0, slash + 1);
            String file = contentPath.substring(slash + 1);
            candidates.add(
                    Identifier.fromNamespaceAndPath(
                            namespace, contentRoot + "/" + directory + locale + "/" + file));
        }
        candidates.add(Identifier.fromNamespaceAndPath(namespace, contentRoot + "/" + contentPath));
        return candidates;
    }

    private static void validateReferences(
            Identifier guideId,
            List<GuideDefinition.GuideArticle> articles,
            List<GuideDefinition.GuideNode> nodes,
            List<GuideDefinition.GuideConnection> connections) {
        var articleIds =
                articles.stream()
                        .map(GuideDefinition.GuideArticle::id)
                        .collect(java.util.stream.Collectors.toSet());
        var nodeIds =
                nodes.stream()
                        .map(GuideDefinition.GuideNode::id)
                        .collect(java.util.stream.Collectors.toSet());
        for (var node : nodes) {
            for (var articleId : node.articleIds()) {
                if (!articleIds.contains(articleId)) {
                    LOGGER.warn(
                            "Guide {} node {} references missing article {}",
                            guideId,
                            node.id(),
                            articleId);
                }
            }
        }
        for (var connection : connections) {
            if (!nodeIds.contains(connection.from()) || !nodeIds.contains(connection.to())) {
                LOGGER.warn(
                        "Guide {} has connection to missing node: {} -> {}",
                        guideId,
                        connection.from(),
                        connection.to());
            }
        }
    }

    private static String contentRoot(Identifier resourceId) {
        String path = resourceId.getPath();
        int mapsIndex = path.lastIndexOf("/maps/");
        if (mapsIndex < 0)
            throw new IllegalArgumentException("Manifest must be in a maps directory");
        return path.substring(0, mapsIndex);
    }

    private static String mapId(Identifier resourceId) {
        String path = resourceId.getPath();
        int start = path.lastIndexOf('/') + 1;
        return path.substring(start, path.length() - ".json".length());
    }

    private static JsonArray array(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonArray()
                ? object.getAsJsonArray(key)
                : new JsonArray();
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive()
                ? object.get(key).getAsString()
                : fallback;
    }

    private static float number(JsonObject object, String key, float fallback) {
        return object.has(key) && object.get(key).isJsonPrimitive()
                ? object.get(key).getAsFloat()
                : fallback;
    }

    private static Identifier identifier(
            JsonObject object, String key, String namespace, @Nullable String fallback) {
        String value = object.has(key) ? object.get(key).getAsString() : fallback;
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Missing guide identifier '" + key + "'");
        return parseIdentifier(value, namespace);
    }

    private static @Nullable Identifier optionalIdentifier(
            JsonObject object, String key, String namespace) {
        return object.has(key) ? parseIdentifier(object.get(key).getAsString(), namespace) : null;
    }

    private static Identifier parseIdentifier(String value, String defaultNamespace) {
        int separator = value.indexOf(':');
        if (separator < 0) return Identifier.fromNamespaceAndPath(defaultNamespace, value);
        return Identifier.fromNamespaceAndPath(
                value.substring(0, separator), value.substring(separator + 1));
    }
}
