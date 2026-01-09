package com.altnoir.mia.client.event;

import com.altnoir.mia.client.renderer.TheAbyssDimEffects;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@OnlyIn(Dist.CLIENT)
public class ClientDimEffects {
    public static final ResourceLocation THE_ABYSS_EFFECTS = MiaUtil.miaId("the_abyss");

    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(THE_ABYSS_EFFECTS, new TheAbyssDimEffects());
    }
}
