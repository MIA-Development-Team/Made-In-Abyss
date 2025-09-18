package com.altnoir.mia.client.event;

import com.altnoir.mia.MIA;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyBindingEvent {
    private static final String CATEGORY = "key.categories." + MIA.MOD_ID;

    public static final KeyMapping ACTIVATE_KEY = new KeyMapping(
            "key." + MIA.MOD_ID + ".activate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
    );

    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE_KEY);
    }
}
