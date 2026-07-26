package com.altnoir.mementoinabyss.client;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class WhistleKeyMappings {
    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(MementoInAbyss.asResource("whistle"));

    public static final KeyMapping SKILL_DIAL = new KeyMapping(
            "key.mementoinabyss.whistle_skill_dial",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    private WhistleKeyMappings() {}
}
