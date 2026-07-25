package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.client.render.PedestalRenderer;
import com.altnoir.mementoinabyss.content.block.entity.PedestalBlockEntity;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

public class MiaBlockEntityTypes {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    public static final BlockEntityEntry<PedestalBlockEntity> PEDESTAL = REGISTRATE
            .blockEntity("pedestal_entity", PedestalBlockEntity::new)
            .validBlock(MiaBlocks.PEDESTAL)
            .renderer(() -> PedestalRenderer::new)
            .registerCapability(event -> event.registerBlockEntity(
                    Capabilities.Item.BLOCK,
                    MiaBlockEntityTypes.PEDESTAL.get(),
                    WorldlyContainerWrapper::new))
            .register();

    public static void register(IEventBus bus) {
        bus.addListener(MiaBlockEntityTypes::addValidBlocks);
    }

    private static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BRUSHABLE_BLOCK, MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE.get());
    }
}
