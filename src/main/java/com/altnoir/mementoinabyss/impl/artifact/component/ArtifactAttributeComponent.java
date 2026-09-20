package com.altnoir.mementoinabyss.impl.artifact.component;

import com.altnoir.mementoinabyss.impl.artifact.ArtifactApi;
import com.altnoir.mementoinabyss.impl.artifact.ArtifactAttribute;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CurioAttributeModifiers;

public record ArtifactAttributeComponent(List<ArtifactAttribute> attributes)
        implements ArtifactItemComponent {
    public ArtifactAttributeComponent {
        attributes = List.copyOf(attributes);
    }

    public ArtifactAttributeComponent(ArtifactAttribute... attributes) {
        this(List.of(attributes));
    }

    @Override
    public void addAttributeModifiers(ItemStack stack, CurioAttributeModifiers.Builder modifiers) {
        for (ArtifactAttribute attribute : attributes) {
            modifiers.addModifier(attribute.attribute(), attribute.modifier(), ArtifactApi.SLOT_ID);
        }
    }
}
