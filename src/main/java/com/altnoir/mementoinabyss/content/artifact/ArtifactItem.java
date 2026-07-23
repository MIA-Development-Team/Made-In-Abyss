package com.altnoir.mementoinabyss.content.artifact;

import com.altnoir.mementoinabyss.content.artifact.component.ArtifactEnhancementComponent;
import com.altnoir.mementoinabyss.content.artifact.component.ArtifactIdentityComponent;
import com.altnoir.mementoinabyss.content.artifact.component.ArtifactItemComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

public final class ArtifactItem extends Item implements ICurioItem {
    private final List<ArtifactItemComponent> components;

    private ArtifactItem(Properties properties, List<ArtifactItemComponent> components) {
        super(applyDefaults(properties, components));
        this.components = List.copyOf(components);
    }

    public static Builder builder(Properties properties, ArtifactProfile profile) {
        return new Builder(properties, profile);
    }

    public List<ArtifactItemComponent> artifactComponents() {
        return components;
    }

    @Override
    public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack) {
        CurioAttributeModifiers.Builder modifiers = CurioAttributeModifiers.builder();
        for (ArtifactItemComponent component : components) {
            component.addAttributeModifiers(stack, modifiers);
        }
        return modifiers.build();
    }

    @Override
    public List<Component> getAttributesTooltip(
            List<Component> tooltips,
            TooltipContext context,
            ItemStack stack
    ) {
        return List.of();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        for (ArtifactItemComponent component : components) {
            component.curioTick(slotContext, stack);
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack previousStack, ItemStack stack) {
        for (ArtifactItemComponent component : components) {
            component.onEquip(slotContext, previousStack, stack);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        for (ArtifactItemComponent component : components) {
            component.onUnequip(slotContext, newStack, stack);
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return components.stream().allMatch(component -> component.canEquip(slotContext, stack));
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return components.stream().allMatch(component -> component.canUnequip(slotContext, stack));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    private static Properties applyDefaults(Properties properties, List<ArtifactItemComponent> components) {
        for (ArtifactItemComponent component : components) {
            component.applyDefaults(properties);
        }
        return properties.stacksTo(1);
    }

    public static final class Builder {
        private final Properties properties;
        private final List<ArtifactItemComponent> components = new ArrayList<>();
        private boolean enhanceable;

        private Builder(Properties properties, ArtifactProfile profile) {
            this.properties = properties;
            components.add(new ArtifactIdentityComponent(profile));
        }

        public Builder enhanceable() {
            if (!enhanceable) {
                components.add(ArtifactEnhancementComponent.INSTANCE);
                enhanceable = true;
            }
            return this;
        }

        public Builder component(ArtifactItemComponent component) {
            components.add(component);
            return this;
        }

        public ArtifactItem build() {
            return new ArtifactItem(properties, components);
        }
    }
}
