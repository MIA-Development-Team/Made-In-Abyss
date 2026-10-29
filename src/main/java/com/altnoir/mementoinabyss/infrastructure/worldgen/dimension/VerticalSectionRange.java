package com.altnoir.mementoinabyss.infrastructure.worldgen.dimension;

import net.minecraft.core.SectionPos;

public record VerticalSectionRange(int minSection, int maxSection) {
    public static final int RADIUS = 8;

    public static VerticalSectionRange around(double y) {
        int section = SectionPos.blockToSectionCoord(y);
        return new VerticalSectionRange(section - RADIUS, section + RADIUS);
    }

    public boolean contains(int sectionY) {
        return sectionY >= minSection && sectionY <= maxSection;
    }

    public int minY() {
        return SectionPos.sectionToBlockCoord(minSection);
    }

    public int maxYExclusive() {
        return SectionPos.sectionToBlockCoord(maxSection + 1);
    }
}
