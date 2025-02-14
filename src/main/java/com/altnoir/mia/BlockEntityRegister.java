package com.altnoir.mia;

import com.altnoir.mia.content.blocks.seventhcrucible.SeventhCrucibleEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegister {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER =  DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,MIA.MOD_ID);

    public static final RegistryObject<BlockEntityType<SeventhCrucibleEntity>> SEVENTH_CRUCIBLE_ENTITY = REGISTER.register(
            "seventh_crucible_entity",
            () -> BlockEntityType.Builder.of(SeventhCrucibleEntity::new,BlocksRegister.SEVENTH_CRUCIBLE_BLOCK.get()).build(null)
    );

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
