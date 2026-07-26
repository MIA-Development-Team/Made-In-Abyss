package com.altnoir.mementoinabyss.impl.whistle.skill;

public enum WhistleSkillCategory {
    BREATH("breath", 0xFF83B56A),
    ECHO("echo", 0xFF72A7A1),
    MELODY("melody", 0xFFAE8AC4);

    private final String serializedName;
    private final int color;

    WhistleSkillCategory(String serializedName, int color) {
        this.serializedName = serializedName;
        this.color = color;
    }

    public String translationKey() {
        return "tooltip.mementoinabyss.whistle.skill.category." + serializedName;
    }

    public int color() {
        return color;
    }
}
