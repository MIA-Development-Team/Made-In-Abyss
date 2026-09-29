package com.mementoinabyss.recall.client;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Keeps outgoing and incoming article bodies alive throughout an interruptible transition. */
final class GuideAccordion {
    private final Map<Identifier, ArticleState> states = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    @Nullable private Identifier selected;

    void toggle(Identifier article) {
        if (article.equals(selected)) {
            collapse();
            return;
        }
        collapse();
        selected = article;
        ArticleState state = states.computeIfAbsent(article, key -> new ArticleState());
        if (state.spring.isSettled()) state.scroll = 0;
        state.spring.setTarget(1);
    }

    void collapse() {
        selected = null;
        states.values().forEach(state -> state.spring.setTarget(0));
    }

    void clear() {
        selected = null;
        states.clear();
    }

    void step(float seconds) {
        states.values().forEach(state -> state.spring.step(seconds));
    }

    boolean isSettled() {
        return states.values().stream().allMatch(state -> state.spring.isSettled());
    }

    float progress(Identifier article) {
        ArticleState state = states.get(article);
        return state == null ? 0 : Math.clamp(state.spring.getValue(), 0, 1);
    }

    double scroll(Identifier article) {
        ArticleState state = states.get(article);
        return state == null ? 0 : state.scroll;
    }

    void scroll(double delta, double maxScroll) {
        ArticleState state = states.get(selected);
        if (state != null) state.scroll = Math.clamp(state.scroll + delta, 0, maxScroll);
    }

    private static final class ArticleState {
        private final GuideSpring spring = new GuideSpring(0.32F);
        private double scroll;
    }
}
