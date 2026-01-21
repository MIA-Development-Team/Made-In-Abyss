package com.altnoir.mia.common.item;

import com.altnoir.mia.common.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.common.item.abs.IEArtifact;
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
