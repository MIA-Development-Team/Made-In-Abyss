package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.content.beacon.CaveExplorerBeaconBlockEntity;
import com.altnoir.mementoinabyss.content.beacon.client.CaveExplorerBeaconRenderer;
import com.altnoir.mementoinabyss.content.cup.EndlessCupBlockEntity;
import com.altnoir.mementoinabyss.content.cup.client.EndlessCupRenderer;
import com.altnoir.mementoinabyss.content.lamptube.AmethystTubeBlockEntity;
import com.altnoir.mementoinabyss.content.pedestal.PedestalBlockEntity;
import com.altnoir.mementoinabyss.content.pedestal.client.PedestalRenderer;
import com.altnoir.mementoinabyss.content.rope.RopeConnectorBlockEntity;
import com.altnoir.mementoinabyss.content.rope.client.RopeConnectorRenderer;
import com.altnoir.mementoinabyss.foundation.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;

public class MiaBlockEntityTypes {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    public static final BlockEntityEntry<PedestalBlockEntity> PEDESTAL =
            REGISTRATE
                    .blockEntity("pedestal_entity", PedestalBlockEntity::new)
                    .validBlock(MiaBlocks.PEDESTAL)
                    .renderer(() -> PedestalRenderer::new)
                    .registerCapability(
                            event ->
                                    event.registerBlockEntity(
                                            Capabilities.Item.BLOCK,
                                            MiaBlockEntityTypes.PEDESTAL.get(),
                                            WorldlyContainerWrapper::new))
                    .register();

    public static final BlockEntityEntry<AmethystTubeBlockEntity> AMETHYST_LAMPTUBE =
            REGISTRATE
                    .blockEntity("amethyst_lamptube", AmethystTubeBlockEntity::new)
                    .validBlock(MiaBlocks.AMETHYST_LAMPTUBE)
                    .registerCapability(
                            event ->
                                    event.registerBlockEntity(
                                            MiaCapabilities.Heat.BLOCK,
                                            MiaBlockEntityTypes.AMETHYST_LAMPTUBE.get(),
                                            (blockEntity, side) -> blockEntity.heatHandler))
                    .register();

    public static final BlockEntityEntry<EndlessCupBlockEntity> ENDLESS_CUP =
            REGISTRATE
                    .blockEntity("endless_cup_entity", EndlessCupBlockEntity::new)
                    .validBlock(MiaBlocks.ENDLESS_CUP)
                    .renderer(() -> EndlessCupRenderer::new)
                    .registerCapability(
                            event ->
                                    event.registerBlockEntity(
                                            Capabilities.Fluid.BLOCK,
                                            MiaBlockEntityTypes.ENDLESS_CUP.get(),
                                            (blockEntity, side) -> blockEntity.fluidHandler))
                    .register();

    public static final BlockEntityEntry<RopeConnectorBlockEntity> ROPE_CONNECTOR =
            REGISTRATE
                    .blockEntity("rope_connector_entity", RopeConnectorBlockEntity::new)
                    .validBlock(MiaBlocks.ROPE_CONNECTOR)
                    .renderer(() -> RopeConnectorRenderer::new)
                    .register();

    public static final BlockEntityEntry<CaveExplorerBeaconBlockEntity> CAVE_EXPLORER_BEACON =
            REGISTRATE
                    .blockEntity("cave_explorer_beacon_entity", CaveExplorerBeaconBlockEntity::new)
                    .validBlock(MiaBlocks.CAVE_EXPLORER_BEACON)
                    .renderer(() -> CaveExplorerBeaconRenderer::new)
                    .register();

    public static void register(IEventBus bus) {
        bus.addListener(MiaBlockEntityTypes::addValidBlocks);
    }

    private static void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.BRUSHABLE_BLOCK, MiaBlocks.SUSPICIOUS_ABYSS_ANDESITE.get());
    }
}
