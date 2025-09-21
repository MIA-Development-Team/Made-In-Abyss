package com.altnoir.mia.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class MiaKeyBinding {
    public static final String KEY_CATEGORIES_MIA = "key.categories.mia";
    public static final String KEY_SKILL_DIAL = "key.mia.skill_dial";

    private static final String CATEGORY = KEY_CATEGORIES_MIA;

    public static final KeyMapping SKILL_DIAL = new KeyMapping(
            KEY_SKILL_DIAL,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
    );

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(SKILL_DIAL);
    }
}
