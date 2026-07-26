package com.altnoir.mementoinabyss.impl.whistle.fragment;

import com.altnoir.mementoinabyss.impl.whistle.grid.SkillShape;

public interface WhistleFragmentDefinition {
    SkillShape shape();

    boolean unique();

    int color();

    String typeTranslationKey();
}
