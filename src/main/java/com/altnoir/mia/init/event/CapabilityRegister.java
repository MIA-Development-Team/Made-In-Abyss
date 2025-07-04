package com.altnoir.mia.init.event;

import com.altnoir.mia.init.MiaBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;

public class CapabilityRegister {
    public static List<CapabilityConsumer> CAPABILITIES = new ArrayList<>();

    static {
        CapabilityRegister.CAPABILITIES.add((event -> {
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    MiaBlockEntities.ENDLESS_CUP_BLOCK_ENTITY.get(),
                    (blockEntity, side) -> blockEntity.fluidTank
            );
        }));
    }

    public static void RegisterCapabilitiesEvent(RegisterCapabilitiesEvent event) {
        for (CapabilityConsumer consumer : CAPABILITIES) {
            consumer.accept(event);
        }
    }

    @FunctionalInterface
    public interface CapabilityConsumer {
        void accept(RegisterCapabilitiesEvent event);
    }
}
