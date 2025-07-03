package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.entity.AbyssSpawnerBlockEntity;
import com.altnoir.mia.block.entity.LavaGeneratorBlockEntity;
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
    public static final Supplier<BlockEntityType<AbyssSpawnerBlockEntity>> ABYSS_SPAWNER = BLOCK_ENTITY_TYPES.register("abyss_spawner", () ->
            BlockEntityType.Builder.of(AbyssSpawnerBlockEntity::new, MiaBlocks.ABYSS_SPAWNER.get()).build(null)
    );

    public static final Supplier<BlockEntityType<LavaGeneratorBlockEntity>> LAVA_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("lava_generator_block_entity", () ->
            BlockEntityType.Builder.of(LavaGeneratorBlockEntity::new, MiaBlocks.LAVA_GENERATOR_BLOCK.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
