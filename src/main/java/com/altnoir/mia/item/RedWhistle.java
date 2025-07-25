package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbstractWhistle;

public class RedWhistle extends AbstractWhistle {
    private int artifactSlotCount;

    public RedWhistle(Properties properties, int artifactSlotCount) {
        super(properties.stacksTo(1));
        this.artifactSlotCount = artifactSlotCount;
    }

    @Override
    public int GetArtifactSlotCount() {
        return artifactSlotCount;
    }
}
