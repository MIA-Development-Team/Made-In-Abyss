package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.network.client.CompassTargetPayload;
import com.altnoir.mia.network.client.CurseCapabilityPayload;
import com.altnoir.mia.network.client.SyncHookPayload;
import com.altnoir.mia.network.server.DiscardHookPayload;
import com.altnoir.mia.network.server.SkillCooldownPayload;
import com.altnoir.mia.network.server.SkillPlayPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class MiaNetworking {
    private static final String VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(MIA.MOD_ID).versioned(VERSION);
        registrar.playToClient(
                CurseCapabilityPayload.TYPE,
                CurseCapabilityPayload.CODEC,
                CurseCapabilityPayload::handle
        );
        registrar.playToClient(
                CompassTargetPayload.TYPE,
                CompassTargetPayload.CODEC,
                CompassTargetPayload::handle
        );
        registrar.playToClient(
                SyncHookPayload.TYPE,
                SyncHookPayload.CODEC,
                SyncHookPayload::handle
        );
        registrar.playToServer(
                SkillCooldownPayload.TYPE,
                SkillCooldownPayload.CODEC,
                SkillCooldownPayload::handle
        );
        registrar.playToServer(
                SkillPlayPayload.TYPE,
                SkillPlayPayload.CODEC,
                SkillPlayPayload::handle
        );
        registrar.playToServer(
                DiscardHookPayload.TYPE,
                DiscardHookPayload.CODEC,
                DiscardHookPayload::handle
        );
    }
}
