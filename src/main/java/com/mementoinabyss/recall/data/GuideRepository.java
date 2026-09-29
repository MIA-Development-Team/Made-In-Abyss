package com.mementoinabyss.recall.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Atomically replaced resource snapshot, including the item-to-guide navigation index. */
public final class GuideRepository {
    private record Snapshot(
            Map<Identifier, GuideDefinition> guides, Map<Identifier, List<Target>> items) {}

    private static volatile Snapshot current = new Snapshot(Map.of(), Map.of());

    public static void replace(Map<Identifier, GuideDefinition> guides) {
        var definitions = Map.copyOf(guides);
        Map<Identifier, List<Target>> index = new HashMap<>();
        definitions.values().stream()
                .sorted(Comparator.comparing(GuideDefinition::id))
                .forEach(
                        guide -> {
                            for (var node : guide.nodes())
                                index.computeIfAbsent(node.item(), ignored -> new ArrayList<>())
                                        .add(new Target(guide.id(), node.id()));
                        });
        index.replaceAll((_, targets) -> List.copyOf(targets));
        current = new Snapshot(definitions, Map.copyOf(index));
    }

    public static Map<Identifier, GuideDefinition> snapshot() {
        return current.guides;
    }

    public static Optional<GuideDefinition> get(Identifier id) {
        return Optional.ofNullable(current.guides.get(id));
    }

    /** Prefer the host's default guide; ties follow stable guide IDs and authored node order. */
    public static Optional<Target> findItem(Identifier item, Identifier preferredGuide) {
        List<Target> targets = current.items.get(item);
        if (targets == null || targets.isEmpty()) return Optional.empty();
        return targets.stream()
                .filter(target -> target.guide.equals(preferredGuide))
                .findFirst()
                .or(() -> Optional.of(targets.getFirst()));
    }

    public record Target(Identifier guide, Identifier node) {}

    private GuideRepository() {}
}
