package com.altnoir.mementoinabyss.content.artifact.component;

import com.altnoir.mementoinabyss.content.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.content.artifact.ArtifactAttribute;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CurioAttributeModifiers;

import java.util.List;

public record ArtifactAttributeComponent(List<ArtifactAttribute> attributes) implements ArtifactItemComponent {
    public ArtifactAttributeComponent {
        attributes = List.copyOf(attributes);
    }

    public ArtifactAttributeComponent(ArtifactAttribute... attributes) {
        this(List.of(attributes));
    }

    @Override
    public void addAttributeModifiers(
            ItemStack stack,
            CurioAttributeModifiers.Builder modifiers
    ) {
        for (ArtifactAttribute attribute : attributes) {
            modifiers.addModifier(
                    attribute.attribute(),
                    attribute.modifier(),
                    ArtifactApi.SLOT_ID
            );
        }
    }
}
