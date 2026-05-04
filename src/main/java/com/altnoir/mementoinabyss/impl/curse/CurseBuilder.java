package com.altnoir.mementoinabyss.impl.curse;

import com.altnoir.mementoinabyss.impl.curse.record.CurseDimension;
import com.altnoir.mementoinabyss.impl.curse.record.CurseEffect;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CurseBuilder {
    private final Identifier id;
    private final List<CurseEffect> effects = new ArrayList<>();
    private int level;

    public CurseBuilder(Identifier id) {
        this.id = id;
    }

    public CurseBuilder effect(CurseEffect effect) {
        this.effects.add(effect);
        return this;
    }

    public CurseBuilder level(int level) {
        this.level = level;
        return this;
    }

    public CurseDimension build() {
        return new CurseDimension(
                id,
                effects,
                level
        );
    }
}
