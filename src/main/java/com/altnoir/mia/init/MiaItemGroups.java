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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MIA.MOD_ID);
    public static final Supplier<CreativeModeTab> MIA_TAB = CREATIVE_MODE_TAB.register("mia_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.mia"))
                    .icon(() -> new ItemStack(MiaItems.RED_WHISTLE.get()))
                    .displayItems((parameters, output) -> {
                        // decoration blocks
                        output.accept(MiaBlocks.COVERGRASS_ABYSS_ANDESITE);
                        output.accept(MiaBlocks.ABYSS_ANDESITE);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_STAIRS);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_SLAB);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_WALL);
                        output.accept(MiaBlocks.ABYSS_COBBLED_ANDESITE);
                        output.accept(MiaBlocks.ABYSS_COBBLED_ANDESITE_STAIRS);
                        output.accept(MiaBlocks.ABYSS_COBBLED_ANDESITE_SLAB);
                        output.accept(MiaBlocks.ABYSS_COBBLED_ANDESITE_WALL);
                        output.accept(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE);
                        output.accept(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_STAIRS);
                        output.accept(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_SLAB);
                        output.accept(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE_WALL);
                        output.accept(MiaBlocks.POLISHED_ABYSS_ANDESITE);
                        output.accept(MiaBlocks.POLISHED_ABYSS_ANDESITE_STAIRS);
                        output.accept(MiaBlocks.POLISHED_ABYSS_ANDESITE_SLAB);
                        output.accept(MiaBlocks.POLISHED_ABYSS_ANDESITE_WALL);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_PILLAR);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_BRICKS);
                        output.accept(MiaBlocks.CRACKED_ABYSS_ANDESITE_BRICKS);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_BRICKS_STAIRS);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB);
                        output.accept(MiaBlocks.ABYSS_ANDESITE_BRICKS_WALL);
                        output.accept(MiaBlocks.CHISLED_ABYSS_ANDESITE);
                        output.accept(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS);
                        output.accept(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_STAIRS);
                        output.accept(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB);
                        output.accept(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_WALL);

                        output.accept(MiaBlocks.PRASIOLITE_BLOCK);
                        output.accept(MiaBlocks.BUDDING_PRASIOLITE);
                        output.accept(MiaBlocks.SMALL_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.MEDIUM_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.LARGE_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.PRASIOLITE_CLUSTER);
                        output.accept(MiaItems.PRASIOLITE);

                        // woods
                        output.accept(MiaBlocks.SKYFOG_LOG);
                        output.accept(MiaBlocks.SKYFOG_WOOD);
                        output.accept(MiaBlocks.STRIPPED_SKYFOG_LOG);
                        output.accept(MiaBlocks.STRIPPED_SKYFOG_WOOD);
                        output.accept(MiaBlocks.SKYFOG_PLANKS);
                        output.accept(MiaBlocks.SKYFOG_STARIS);
                        output.accept(MiaBlocks.SKYFOG_SLAB);
                        output.accept(MiaBlocks.SKYFOG_FENCE);
                        output.accept(MiaBlocks.SKYFOG_FENCE_GATE);
                        output.accept(MiaBlocks.SKYFOG_DOOR);
                        output.accept(MiaBlocks.SKYFOG_TRAPDOOR);
                        output.accept(MiaBlocks.SKYFOG_LEAVES);
                        output.accept(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS);
                        output.accept(MiaBlocks.SKYFOG_SAPLING);
                        output.accept(MiaBlocks.FORTITUDE_FLOWER);
                        output.accept(MiaBlocks.SKYFOG_PRESSURE_PLATE);
                        output.accept(MiaBlocks.SKYFOG_BUTTON);

                        // props
                        output.accept(MiaBlocks.HOPPER_FARMLAND);
                        output.accept(MiaBlocks.ABYSS_PORTAL);
                        output.accept(MiaBlocks.ABYSS_SPAWNER);

                        // foods
                        output.accept(MiaItems.MISTFUZZ_PEACH);

                        // tools
                        output.accept(MiaItems.RED_WHISTLE);
                        output.accept(MiaItems.BLUE_WHISTLE);
                        output.accept(MiaItems.MOON_WHISTLE);
                        output.accept(MiaItems.BLACK_WHISTLE);
                        output.accept(MiaItems.WHITE_WHISTLE);
                        output.accept(MiaItems.ROPE);
                        output.accept(MiaBlocks.LAMP_TUBE);
                        output.accept(MiaBlocks.PEDESTAL);
                        output.accept(MiaBlocks.ARTIFACT_SMITHING_TABLE);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> MIA_TAB_ARIFACT = CREATIVE_MODE_TAB.register("mia_tab_artifact", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.mia_artifact"))
                    .icon(() -> new ItemStack(MiaItems.FANCY_ARTIFACT_BUNDLE.get()))
                    .displayItems((parameters, output) -> {
                        // artifacts
                        output.accept(MiaBlocks.ARTIFACT_SMITHING_TABLE);
                        output.accept(MiaItems.GRAY_ARTIFACT_BUNDLE);
                        output.accept(MiaItems.FANCY_ARTIFACT_BUNDLE);

                        output.accept(MiaItems.TEST_ARTIFACT_1);
                        output.accept(MiaItems.TEST_ARTIFACT_2);
                        output.accept(MiaItems.TEST_ARTIFACT_3);

                        output.accept(MiaBlocks.ENDLESS_CUP);
                    }).build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
