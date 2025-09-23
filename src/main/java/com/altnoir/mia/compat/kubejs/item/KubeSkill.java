package com.altnoir.mia.compat.kubejs.item;

import com.altnoir.mia.item.abs.AbsSkill;

import java.util.List;

public class KubeSkill extends AbsSkill {
    private final List<Integer> combo;
    private final Grade grade;
    private final int cooldown;

    public KubeSkill(Properties properties, List<Integer> combo, Grade grade, int cooldown) {
        super(properties);
        this.combo = combo;
        this.grade = grade;
        this.cooldown = cooldown;
    }

    @Override
    public List<Integer> getComboSequence() {
        return combo;
    }

    @Override
    public Grade getGrade() {
        return grade;
    }

    @Override
    public int cooldownTicks() {
        return cooldown;
    }


}
