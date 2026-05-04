package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;

public class MiaItems {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    static {
        REGISTRATE.defaultCreativeTab(MiaItemGroups.ARTIFACT.getKey());
    }

    public static void register() {}
}
