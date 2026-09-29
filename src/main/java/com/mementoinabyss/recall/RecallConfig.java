package com.mementoinabyss.recall;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/** Host-owned names and content location; Recall contains no mod-specific identifiers. */
public record RecallConfig(
        String resourceRoot,
        Identifier defaultGuide,
        Identifier keyCategory,
        String keyTranslation,
        int defaultKey) {
    public RecallConfig {
        Objects.requireNonNull(defaultGuide);
        Objects.requireNonNull(keyCategory);
        Objects.requireNonNull(keyTranslation);
        if (!resourceRoot.matches("[a-z0-9_-]+(?:/[a-z0-9_-]+)*"))
            throw new IllegalArgumentException("Expected a relative resource directory");
    }
}
