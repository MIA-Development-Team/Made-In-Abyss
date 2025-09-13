package com.altnoir.mia.init.event;

import com.altnoir.mia.init.MiaAttributes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

public class PlayerAttributeEvent {
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        /*if (!event.has(EntityType.PLAYER, MiaAttributes.BLOCK_HARDNESS)) {
            event.add(EntityType.PLAYER, MiaAttributes.BLOCK_HARDNESS);
        }*/
    }
}
