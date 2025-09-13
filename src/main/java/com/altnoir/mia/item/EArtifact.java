package com.altnoir.mia.item;

import com.altnoir.mia.component.ArtifactEnhancementComponent;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.item.abs.IEArtifact;
import net.minecraft.world.item.Item;

public class EArtifact extends Item implements IEArtifact {
    private final Grade grade;
    private final int weight;

    public EArtifact(Properties properties, Grade grade, int weight) {
        super(properties.component(MiaComponents.ARTIFACT_ENHANCEMENT, ArtifactEnhancementComponent.EMPTY));
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


    // public static class Builder {
    // private final Properties props;
    // private int weight;
    // private int maxLevel;
    // private final List<SimpleArtifactStat> mods = new ArrayList<>();

    // public Builder(Properties props) {
    // this.props = props;
    // }

    // public Builder weight(int w) {
    // this.weight = w;
    // return this;
    // }

    // public Builder maxLevel(int l) {
    // this.maxLevel = l;
    // return this;
    // }

    // public Builder addModifier(Holder<Attribute> attribute, double amount,
    // AttributeModifier.Operation operation) {
    // mods.add(new SimpleArtifactStat(attribute, amount, operation));
    // return this;
    // }

    // public EnhanceableArtifact build() {
    // return new EnhanceableArtifact(this);
    // }

    // }

    // public record SimpleArtifactStat(
    // Holder<Attribute> attribute,
    // double amount,
    // AttributeModifier.Operation operation) {
    // }

}
