package com.altnoir.mia.compat.ponder;

import com.altnoir.mia.init.MiaBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class TestPonderScenes {
    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(MiaBlocks.AMETHYST_LAMPTUBE.getId(), MiaBlocks.PRASIOLITE_BLOCK.getId())
                .addStoryBoard("test_template", TestPonderAnimation::test);

    }
}
