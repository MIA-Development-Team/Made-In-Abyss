package com.altnoir.mia;

import com.altnoir.mia.content.block.BlocksRegister;
import com.altnoir.mia.content.items.ItemsRegister;
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
            .create(Registries.CREATIVE_MODE_TAB, MIA.MODID);

    public static final RegistryObject<CreativeModeTab> MIA_TAB = REGISTER.register("mia",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemsRegister.PURIN.get()))
                    .title(Component.translatable("itemGroup.mia.mia"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(Blocks.SPAWNER);
                        pOutput.accept(BlocksRegister.ABYSS_GRASS_BLOCK.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_COBBLESTONE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_STONE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_DEEPSLATE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_GRANITE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_DIORITE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_ANDESITE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_CALCITE.get());
                        pOutput.accept(BlocksRegister.COVERGRASS_TUFF.get());
                        pOutput.accept(BlocksRegister.CHLOROPHTRE_ORE.get());
                        pOutput.accept(BlocksRegister.SUSPICIOUS_ANDESITE.get());
                        pOutput.accept(BlocksRegister.VOLCANO_CRUCIBLE.get());
                        pOutput.accept(ItemsRegister.PURIN.get());
                        pOutput.accept(ItemsRegister.ENDLESS_CUP.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}
