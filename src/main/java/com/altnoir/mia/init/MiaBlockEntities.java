package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.entity.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MIA.MOD_ID);


    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY = BLOCK_ENTITY_TYPES.register("pedestal_entity", () ->
            BlockEntityType.Builder.of(PedestalBlockEntity::new, MiaBlocks.PEDESTAL.get()).build(null)
    );
    public static final Supplier<BlockEntityType<AbyssSpawnerBlockEntity>> ABYSS_SPAWNER_ENTITY = BLOCK_ENTITY_TYPES.register("abyss_spawner_entity", () ->
            BlockEntityType.Builder.of(AbyssSpawnerBlockEntity::new, MiaBlocks.ABYSS_SPAWNER.get()).build(null)
    );

    public static final Supplier<BlockEntityType<EndlessCupBlockEntity>> ENDLESS_CUP_ENTITY = BLOCK_ENTITY_TYPES.register("endless_cup_entity", () ->
            BlockEntityType.Builder.of(EndlessCupBlockEntity::new, MiaBlocks.ENDLESS_CUP.get()).build(null)
    );

    public static final Supplier<BlockEntityType<SunStoneBlockEntity>> SUN_STONE_ENTITY = BLOCK_ENTITY_TYPES.register("sun_stone_entity", () ->
            BlockEntityType.Builder.of(SunStoneBlockEntity::new, MiaBlocks.SUN_STONE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<CaveExplorerBeaconBlockEntity>> CAVE_EXPLORER_BEACON_ENTITY = BLOCK_ENTITY_TYPES.register("cave_explorer_beacon_entity", () ->
            BlockEntityType.Builder.of(CaveExplorerBeaconBlockEntity::new, MiaBlocks.CAVE_EXPLORER_BEACON.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
