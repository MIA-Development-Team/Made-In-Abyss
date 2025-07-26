package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbstractArtifactBundle;

public class ArtifactBundle extends AbstractArtifactBundle {

    private final int capacity;

    public ArtifactBundle(Properties properties, int capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }
}
