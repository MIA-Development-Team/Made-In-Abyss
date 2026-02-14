package com.altnoir.mia.compat.ponder;

import com.altnoir.mia.MIA;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class MiaPonder implements PonderPlugin {
    @Override
    public String getModId() {
        return MIA.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        MiaPonderScenes.registerScenes(helper);
    }
}
