package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MIA.MOD_ID);

    /*public static final Supplier<BlockEntityType<AbyssPortalBlockEntity>> ABYSS_PORTAL = BLOCK_ENTITY_TYPES.register("abyss_portal", () ->
            BlockEntityType.Builder.of(AbyssPortalBlockEntity::new, MiaBlocks.ABYSS_PORTAL.get()).build(null)
    );*/

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
