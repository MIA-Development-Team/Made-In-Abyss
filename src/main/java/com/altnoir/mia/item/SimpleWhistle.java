package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbstractWhistle;

public class SimpleWhistle extends AbstractWhistle {
    private final int artifactSlotCount;
    private final int maxLevel;

    public SimpleWhistle(Properties properties, int artifactSlotCount, int maxLevel) {
        super(properties.stacksTo(1));
        this.artifactSlotCount = artifactSlotCount;
        this.maxLevel = maxLevel;
    }

    @Override
    public int getArtifactSlotCount() {
        return artifactSlotCount;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }
}
