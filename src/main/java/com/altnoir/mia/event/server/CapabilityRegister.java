package com.altnoir.mia.event.server;

import com.altnoir.mia.init.MiaAttachments;
import com.altnoir.mia.init.MiaBlockEntities;
import com.altnoir.mia.init.MiaCapabilities;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilityRegister {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                MiaCapabilities.CURSE,
                EntityType.PLAYER,
                (entity, side) -> entity.getData(MiaAttachments.CURSE)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                MiaBlockEntities.ENDLESS_CUP_ENTITY.get(),
                (blockEntity, side) -> blockEntity.fluidTank
        );
    }
}
