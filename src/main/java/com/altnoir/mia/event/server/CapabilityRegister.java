package com.altnoir.mia.event.server;

import com.altnoir.mia.init.MiaBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilityRegister {
    public static void RegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MiaBlockEntities.ENDLESS_CUP_ENTITY.get(),
                (blockEntity, side) -> blockEntity.fluidTank
        );
    }
}
