package com.altnoir.mia.init;

import com.altnoir.mia.MIA;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MiaItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MIA.MOD_ID);

    public static final Supplier<CreativeModeTab> MIA_TAB = CREATIVE_MODE_TAB.register("mia_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemgroup.mia"))
            .icon(() -> new ItemStack(MiaItems.RED_WHISTLE.get()))
            .displayItems((parameters, output) -> {
                output.accept(MiaItems.RED_WHISTLE);
                output.accept(MiaBlocks.ABYSS_GRASS_BLOCK);
                output.accept(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
                output.accept(MiaBlocks.ABYSS_ANDESITE);
                output.accept(MiaBlocks.ABYSS_COBBLED_ANDESITE);
                output.accept(MiaBlocks.SKYFOGWOOD_LOG);
                output.accept(MiaBlocks.SKYFOGWOOD);
                output.accept(MiaBlocks.STRIPPED_SKYFOGWOOD_LOG);
                output.accept(MiaBlocks.STRIPPED_SKYFOGWOOD);
                output.accept(MiaBlocks.SKYFOGWOO_PLANKS);
                output.accept(MiaBlocks.FORTITUDE_FLOWER);
                output.accept(MiaBlocks.LAMP_TUBE);
                output.accept(MiaBlocks.ABYSS_PORTAL);
                output.accept(MiaItems.ENDLESS_CUP);
            })
            .build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
