package com.altnoir.mementoinabyss.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The ordered client mailbox carries both cache offers and bounded data parts. */
public sealed interface CrossDimensionLodStreamPayload extends CustomPacketPayload
        permits CrossDimensionLodBatchPayload, CrossDimensionLodCacheOfferPayload {
    CrossDimensionLodTransfer transfer();
    int encodedSize();
    default int retainedBytes() { return encodedSize(); }
}
