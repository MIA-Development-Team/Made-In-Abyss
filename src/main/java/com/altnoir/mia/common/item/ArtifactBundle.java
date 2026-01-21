package com.altnoir.mia.common.item;

import com.altnoir.mia.common.item.abs.AbsArtifactBundle;

public class ArtifactBundle extends AbsArtifactBundle {

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
