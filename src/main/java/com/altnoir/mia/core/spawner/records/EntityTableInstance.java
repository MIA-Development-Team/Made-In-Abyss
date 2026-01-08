package com.altnoir.mia.core.spawner.records;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTableInstance extends WeightedEntry.IntrusiveBase {
    private final EntityType<?> entityType;
    private final Map<EquipmentSlot, ItemStack> equipment;
    private final List<MobEffectInstance> effects;
    private final Map<Holder<Attribute>, AttributeModifier> attributeModifiers;

    public EntityTableInstance(EntityType<?> entityType, int weight) {
        this(entityType, weight, new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    public EntityTableInstance(
            EntityType<?> entityType,
            int weight,
            Map<EquipmentSlot, ItemStack> equipment,
            List<MobEffectInstance> effects,
            Map<Holder<Attribute>, AttributeModifier> attributeModifiers
    ) {
        super(weight);
        this.entityType = entityType;
        this.equipment = equipment;
        this.effects = effects;
        this.attributeModifiers = attributeModifiers;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public Map<EquipmentSlot, ItemStack> getEquipment() {
        return equipment;
    }

    public List<MobEffectInstance> getEffects() {
        return effects;
    }

    public Map<Holder<Attribute>, AttributeModifier> getAttributeModifiers() {
        return attributeModifiers;
    }

    public boolean hasEquipment() {
        return !equipment.isEmpty();
    }

    public boolean hasEffects() {
        return !effects.isEmpty();
    }

    public boolean hasAttributeModifiers() {
        return !attributeModifiers.isEmpty();
    }
}
