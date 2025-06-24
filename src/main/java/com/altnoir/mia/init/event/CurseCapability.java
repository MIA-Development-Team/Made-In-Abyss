package com.altnoir.mia.init.event;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaAttachments;
import com.altnoir.mia.init.MiaCapabilities;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MIA.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CurseCapability {
    @SubscribeEvent
    public static void attachEntityCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                MiaCapabilities.CURSE,
                EntityType.PLAYER,
                (entity, side) -> entity.getData(MiaAttachments.CURSE)
        );
    }
}
