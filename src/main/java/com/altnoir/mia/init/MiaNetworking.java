package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.network.CurseCapabilityPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class MiaNetworking {
    private static final String VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event){
        var registrar = event.registrar(MIA.MOD_ID).versioned(VERSION);
        registrar.playToClient(
                CurseCapabilityPayload.TYPE,
                CurseCapabilityPayload.CODEC,
                CurseCapabilityPayload::handle
        );
    }
}
