package com.altnoir.mementoinabyss.impl.whistle.skill;

import com.altnoir.mementoinabyss.impl.whistle.fragment.WhistleFragmentDefinition;
import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;

import java.util.List;

public record WhistleSkillDefinition(
        SkillShape shape,
        WhistleSkillCategory category,
        boolean unique,
        List<WhistleNote> sequence,
        int cooldownTicks
) implements WhistleFragmentDefinition {
    public WhistleSkillDefinition {
        sequence = List.copyOf(sequence);
        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("An active whistle skill needs a note sequence");
        }
        if (cooldownTicks < 1) {
            throw new IllegalArgumentException("Whistle skill cooldown must be positive");
        }
    }

    @Override
    public int color() {
        return category.color();
    }

    @Override
    public String typeTranslationKey() {
        return "tooltip.mementoinabyss.whistle.fragment.type.skill";
    }
}
