package com.altnoir.mementoinabyss.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registers all common network payload codecs in one place. */
public final class MiaNetwork {
    private static final String PROTOCOL_VERSION = "6";

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(CompassTargetPayload.TYPE, CompassTargetPayload.STREAM_CODEC);
        registrar.playToClient(CrossDimensionLodPayload.TYPE, CrossDimensionLodPayload.STREAM_CODEC);
        registrar.playToClient(CrossDimensionLodDebugPayload.TYPE, CrossDimensionLodDebugPayload.STREAM_CODEC);
        registrar.playToClient(RopeSnapshotPayload.TYPE, RopeSnapshotPayload.STREAM_CODEC);
        registrar.playToServer(CrossDimensionLodControlPayload.TYPE,
                CrossDimensionLodControlPayload.STREAM_CODEC, CrossDimensionLodControlPayload::handle);
        registrar.playToServer(AdjustRopeLengthPayload.TYPE,
                AdjustRopeLengthPayload.STREAM_CODEC, AdjustRopeLengthPayload::handle);
        registrar.playToServer(RopeNodeGrabPayload.TYPE,
                RopeNodeGrabPayload.STREAM_CODEC, RopeNodeGrabPayload::handle);
        registrar.playToServer(
                ActivateWhistleSkillPayload.TYPE,
                ActivateWhistleSkillPayload.STREAM_CODEC,
                ActivateWhistleSkillPayload::handle
        );
    }

    private MiaNetwork() {}
}
