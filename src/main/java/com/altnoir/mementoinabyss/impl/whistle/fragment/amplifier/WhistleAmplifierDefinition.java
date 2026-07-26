package com.altnoir.mementoinabyss.impl.whistle.fragment.amplifier;

import com.altnoir.mementoinabyss.impl.whistle.fragment.WhistleFragmentDefinition;
import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;

public record WhistleAmplifierDefinition(
        SkillShape shape,
        boolean unique,
        double powerMultiplier,
        double cooldownMultiplier
) implements WhistleFragmentDefinition {
    public WhistleAmplifierDefinition {
        if (!Double.isFinite(powerMultiplier) || powerMultiplier <= 0.0) {
            throw new IllegalArgumentException("Power multiplier must be finite and positive");
        }
        if (!Double.isFinite(cooldownMultiplier) || cooldownMultiplier <= 0.0) {
            throw new IllegalArgumentException("Cooldown multiplier must be finite and positive");
        }
    }

    @Override
    public int color() {
        return 0xFFD39A55;
    }

    @Override
    public String typeTranslationKey() {
        return "tooltip.mementoinabyss.whistle.fragment.type.amplifier";
    }
}
