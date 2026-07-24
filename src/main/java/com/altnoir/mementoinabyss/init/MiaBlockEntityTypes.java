package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.altnoir.mementoinabyss.content.block.entity.PedestalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MiaBlockEntityTypes {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MementoInAbyss.ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL =
            TYPES.register("pedestal_entity", () -> new BlockEntityType<>(
                    PedestalBlockEntity::new, java.util.Set.of(MiaBlocks.PEDESTAL.get())));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener(MiaBlockEntityTypes::addValidBlocks);
    }

    private static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BRUSHABLE_BLOCK, MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE.get());
    }
}
