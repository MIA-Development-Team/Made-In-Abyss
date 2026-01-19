package com.altnoir.mia.event.server;

import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

public class PlayerAttributeEvent {
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        /*if (!event.has(EntityType.PLAYER, MiaAttributes.BLOCK_HARDNESS)) {
            event.add(EntityType.PLAYER, MiaAttributes.BLOCK_HARDNESS);
        }*/
    }
}
