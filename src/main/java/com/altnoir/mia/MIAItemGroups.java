package com.altnoir.mia;

import com.altnoir.mia.block.MIABlocks;
import com.altnoir.mia.item.MIAItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MIAItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MIA.MOD_ID);

    public static final Supplier<CreativeModeTab> MIA_TAB = CREATIVE_MODE_TAB.register("mia_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemgroup.mia"))
            .icon(() -> new ItemStack(MIAItems.ENDLESS_CUP.get()))
            .displayItems((parameters, output) -> {
                output.accept(MIAItems.ENDLESS_CUP);
                output.accept(MIABlocks.ABYSS_GRASS_BLOCK);
                output.accept(MIABlocks.COVERGRASS_ABYSS_ANDESITE);
                output.accept(MIABlocks.FORTITUDE_FLOWER);
                output.accept(MIABlocks.ABYSS_ANDESITE);
                output.accept(MIABlocks.ABYSS_COBBLED_ANDESITE);
            })
            .build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
