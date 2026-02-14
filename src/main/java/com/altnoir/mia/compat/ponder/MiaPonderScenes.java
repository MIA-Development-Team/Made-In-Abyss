package com.altnoir.mia.compat.ponder;

import com.altnoir.mia.compat.ponder.animation.LamptubeAnimation;
import com.altnoir.mia.init.MiaBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class MiaPonderScenes {
    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(MiaBlocks.AMETHYST_LAMPTUBE.getId(), MiaBlocks.PEDESTAL.getId())
                .addStoryBoard("amethyst_lamptube", LamptubeAnimation::amethystPressing);
        helper.forComponents(MiaBlocks.PRASIOLITE_LAMPTUBE.getId())
                .addStoryBoard("prasiolite_lamptube", LamptubeAnimation::prasiolitePressing);
    }
}
