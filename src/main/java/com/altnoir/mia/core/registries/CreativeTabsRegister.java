package com.altnoir.mia.core.registries;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabsRegister {
    private static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MIA.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MIA_TAB = REGISTER.register("mia",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemsRegister.PURIN.get()))
                    .title(Component.translatable("itemGroup.mia.mia"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(Blocks.SPAWNER);
                        pOutput.accept(BlocksRegister.GRASS_BLOCK_D1.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_COBBLESTONE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_ANDESITE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_DEEPSLATE.get());
                        pOutput.accept(BlocksRegister.CHLOROPHTRE_ORE.get());
                        pOutput.accept(BlocksRegister.SEVENTH_CRUCIBLE_BLOCK.get());
                        pOutput.accept(ItemsRegister.PURIN.get());
                        pOutput.accept(ItemsRegister.ENDLESS_CUP.get());
                        pOutput.accept(ItemsRegister.CLOCK.get());
                        pOutput.accept(ItemsRegister.KNIFE.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
