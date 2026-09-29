package com.altnoir.mementoinabyss;

import com.altnoir.mementoinabyss.client.tooltip.MiaTooltipModifiers;
import com.mementoinabyss.recall.RecallConfig;
import com.mementoinabyss.recall.client.RecallClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = MementoInAbyss.ID, dist = Dist.CLIENT)
public class MementoInAbyssClient {
    public MementoInAbyssClient(ModContainer container, IEventBus modEventBus) {
        MiaTooltipModifiers.register();
        RecallClient.initialize(
                modEventBus,
                new RecallConfig(
                        "abyss_guide",
                        MementoInAbyss.asResource("abyss"),
                        MementoInAbyss.asResource("guide"),
                        "key.mementoinabyss.open_guide",
                        InputConstants.KEY_G));
    }
}
