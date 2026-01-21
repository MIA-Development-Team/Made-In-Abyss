package com.altnoir.mia.compat.kubejs.item;


import com.altnoir.mia.compat.kubejs.combo.ComboContext;
import com.altnoir.mia.common.item.abs.AbsSkill;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Info(value = "Builder for custom skill items in Made-In-Abyss mod.")
public class SkillBuilder extends ItemBuilder {
    private List<Integer> combo = Arrays.asList(1, 2, 3); // 默认下左右
    private AbsSkill.Grade grade = AbsSkill.Grade.D; // 默认D级
    private int cooldown = 300; // 默认15秒冷却 (300 ticks)

    public SkillBuilder(ResourceLocation id) {
        super(id);
    }

    @Info(value = "Set the combo sequence for this skill. Each integer represents a button:0=Up, 1=Left, 2=Right, 3=Middle. Default is [1, 2, 3].")
    public SkillBuilder comboInt(Integer... combo) {
        this.combo = Arrays.asList(combo);
        return this;
    }

    @Info(value = "Set the combo sequence for this skill using a builder pattern.")
    public SkillBuilder combo(Consumer<ComboContext> consumer) {
        ComboContext context = new ComboContext();
        consumer.accept(context);
        this.combo = context.build();
        return this;
    }

    @Info(value = "Set the grade of this skill, Just display. Default is D.")
    public SkillBuilder grade(AbsSkill.Grade grade) {
        this.grade = grade;
        return this;
    }

    @Info(value = "Set the cooldown in ticks (20 ticks = 1 second). Default is 300 (15 seconds).")
    public SkillBuilder cooldown(int ticks) {
        this.cooldown = ticks;
        return this;
    }

    @Override
    public Item createObject() {
        return new KubeSkill(createItemProperties(), combo, grade, cooldown);
    }
}