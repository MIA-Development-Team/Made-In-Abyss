package com.altnoir.mia;

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
                        pOutput.accept(BlocksRegister.CHLOROPHTRE_ORE.get());
                        pOutput.accept(BlocksRegister.ALEXANDRITE_BLOCK.get());
                        pOutput.accept(ItemsRegister.PURIN.get());
                        pOutput.accept(ItemsRegister.ENDLESS_CUP.get());
                        pOutput.accept(ItemsRegister.CLOCK.get());
                        pOutput.accept(ItemsRegister.Knife.get());
                        pOutput.accept(ItemsRegister.YOUR_WORTH.get());
                        pOutput.accept(ItemsRegister.BLOODY_YOUR_WORTH.get());
                        pOutput.accept(ItemsRegister.BLUE_WHISTLE.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
