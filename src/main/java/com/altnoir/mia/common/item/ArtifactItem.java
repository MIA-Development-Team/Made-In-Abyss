package com.altnoir.mia.common.item;

import com.altnoir.mia.common.component.ArtifactEnhancementComponent;
import com.altnoir.mia.common.item.abs.IEArtifact;
import com.altnoir.mia.init.MiaComponents;
import com.altnoir.mia.init.MiaItems;
import com.altnoir.mia.util.MiaUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

public class ArtifactItem extends Item implements IEArtifact {
    private final Grade grade;
    private final int weight;

    public ArtifactItem(Properties properties, Grade grade, int weight) {
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

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        // 获取父类的属性修饰符映射
        Multimap<Holder<Attribute>, AttributeModifier> parentMultimap = IEArtifact.super.getAttributeModifiers(slotContext, id, stack);

        if (stack.is(MiaItems.HEALTH_JUNKIE.get())) {
            // 创建新的可变 Multimap
            Multimap<Holder<Attribute>, AttributeModifier> multimap = ArrayListMultimap.create();
            // 复制父类的所有属性
            multimap.putAll(parentMultimap);

            multimap.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    MiaUtil.miaId("knockback_resistance"),
                    0.2,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
            multimap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                    MiaUtil.miaId("armor_toughness"),
                    2.0,
                    AttributeModifier.Operation.ADD_VALUE
            ));
            return multimap;
        }
        return parentMultimap;
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
