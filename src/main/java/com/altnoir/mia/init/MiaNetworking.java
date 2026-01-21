package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.network.client.CompassTargetPayload;
import com.altnoir.mia.network.client.CurseCapabilityPayload;
import com.altnoir.mia.network.server.PopHookPayload;
import com.altnoir.mia.network.server.SkillCooldownPayload;
import com.altnoir.mia.network.server.SkillPlayPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MiaNetworking {
    private static final String VERSION = "1";

    public static void register(PayloadRegistrar payloadRegistrar) {
        var registrar = payloadRegistrar.versioned(VERSION);
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
                PopHookPayload.TYPE,
                PopHookPayload.CODEC,
                PopHookPayload::handle
        );
    }
}
