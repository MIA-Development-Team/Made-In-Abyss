package com.altnoir.mementoinabyss;

import com.altnoir.mementoinabyss.client.tooltip.MiaTooltipModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;


@Mod(value = MementoInAbyss.ID, dist = Dist.CLIENT)
public class MementoInAbyssClient {
    public MementoInAbyssClient(ModContainer container) {
        MiaTooltipModifiers.register();
    }
}
