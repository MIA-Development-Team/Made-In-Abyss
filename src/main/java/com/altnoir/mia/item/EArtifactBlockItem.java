package com.altnoir.mia.item;

import com.altnoir.mia.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.abs.IEArtifact;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class EArtifactBlockItem extends BlockItem implements IEArtifact {
    private final Grade grade;
    private final int weight;

    public EArtifactBlockItem(Block block, Properties properties, Grade grade, int weight) {
        super(block, properties.component(MiaComponents.ARTIFACT_ENHANCEMENT, ArtifactEnhancementComponent.EMPTY));
        this.grade = grade;
        this.weight = weight;
    }

    @Override
    public Grade getGrade() {
        return this.grade;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }
}
