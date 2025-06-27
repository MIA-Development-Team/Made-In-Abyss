package com.altnoir.mia;

import com.altnoir.mia.client.event.ClientEventHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = MIA.MOD_ID, dist = Dist.CLIENT)
public class MIAClient {
    public MIAClient(IEventBus modEventBus, ModContainer modContainer) {
        var gameEventBus = NeoForge.EVENT_BUS;
        ClientEventHandler.addListener(modEventBus, gameEventBus);
    }
}