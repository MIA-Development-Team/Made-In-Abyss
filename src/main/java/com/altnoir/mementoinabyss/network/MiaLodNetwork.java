package com.altnoir.mementoinabyss.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registers the complete cross-dimension LOD protocol in one place. */
public final class MiaLodNetwork {
    private static final String PROTOCOL_VERSION = "4";

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(CrossDimensionLodPayload.TYPE, CrossDimensionLodPayload.STREAM_CODEC);
        registrar.playToClient(CrossDimensionLodDebugPayload.TYPE, CrossDimensionLodDebugPayload.STREAM_CODEC);
        registrar.playToServer(CrossDimensionLodControlPayload.TYPE,
                CrossDimensionLodControlPayload.STREAM_CODEC, CrossDimensionLodControlPayload::handle);
    }

    private MiaLodNetwork() {}
}
