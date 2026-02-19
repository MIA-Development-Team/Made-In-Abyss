package com.altnoir.mia.init;

import com.altnoir.mia.client.network.CompassTargetPayload;
import com.altnoir.mia.client.network.CurseCapabilityPayload;
import com.altnoir.mia.common.network.RetractHookPayload;
import com.altnoir.mia.common.network.SkillCooldownPayload;
import com.altnoir.mia.common.network.SkillPlayPayload;
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
                RetractHookPayload.TYPE,
                RetractHookPayload.CODEC,
                RetractHookPayload::handle
        );
    }
}
