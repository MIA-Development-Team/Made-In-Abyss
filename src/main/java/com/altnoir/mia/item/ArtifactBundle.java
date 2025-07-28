package com.altnoir.mia.item;

import com.altnoir.mia.item.abs.AbstractArtifactBundle;

public class ArtifactBundle extends AbstractArtifactBundle {

    private final int capacity;
    private final Grade grade;

    public ArtifactBundle(Properties properties, Grade grade, int capacity) {
        super(properties.stacksTo(1));
        this.grade = grade;
        this.capacity = capacity;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }
}
