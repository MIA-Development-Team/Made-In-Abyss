package com.altnoir.mia.client.event;

import com.altnoir.mia.client.renderer.AbyssBrinkDimEffects;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@OnlyIn(Dist.CLIENT)
public class ClientDimEffects {
    public static final ResourceLocation ABYSS_BRINK_EFFECTS = MiaUtil.miaId("abyss_brink");

    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ABYSS_BRINK_EFFECTS, new AbyssBrinkDimEffects());
    }
}
