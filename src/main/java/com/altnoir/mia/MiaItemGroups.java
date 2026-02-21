package com.altnoir.mia;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaItems;
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
                        output.accept(MiaBlocks.COVERGRASS_TUFF);
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
                        // 化石树
                        output.accept(MiaBlocks.FOSSILIZED_LOG);
                        output.accept(MiaBlocks.FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_LOG);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.MOSSY_FOSSILIZED_LOG);
                        output.accept(MiaBlocks.MOSSY_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_LOG);
                        output.accept(MiaBlocks.MOSSY_STRIPPED_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.POLISHED_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.POLISHED_FOSSILIZED_WOOD_STAIRS);
                        output.accept(MiaBlocks.POLISHED_FOSSILIZED_WOOD_SLAB);
                        output.accept(MiaBlocks.POLISHED_FOSSILIZED_WOOD_WALL);
                        output.accept(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_STAIRS);
                        output.accept(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_SLAB);
                        output.accept(MiaBlocks.POLISHED_STRIPPED_FOSSILIZED_WOOD_WALL);
                        output.accept(MiaBlocks.CHISLED_STRIPPED_FOSSILIZED_WOOD);
                        output.accept(MiaBlocks.FOSSILIZED_WOOD_BRICKS);
                        output.accept(MiaBlocks.FOSSILIZED_WOOD_BRICKS_STAIRS);
                        output.accept(MiaBlocks.FOSSILIZED_WOOD_BRICKS_SLAB);
                        output.accept(MiaBlocks.FOSSILIZED_WOOD_BRICKS_WALL);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_STAIRS);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_SLAB);
                        output.accept(MiaBlocks.STRIPPED_FOSSILIZED_WOOD_BRICKS_WALL);

                        output.accept(MiaBlocks.HOPPER_FARMLAND);
                        output.accept(MiaBlocks.SUN_STONE);
                        // 翡翠
                        output.accept(MiaBlocks.PRASIOLITE_BLOCK);
                        output.accept(MiaBlocks.BUDDING_PRASIOLITE);
                        output.accept(MiaBlocks.SMALL_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.MEDIUM_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.LARGE_PRASIOLITE_BUD);
                        output.accept(MiaBlocks.PRASIOLITE_CLUSTER);
                        output.accept(MiaItems.PRASIOLITE_SHARD);
                        output.accept(MiaItems.PRASIOLITE_PICKAXE);
                        output.accept(MiaItems.PRASIOLITE_HOE);


                        // woods
                        output.accept(MiaBlocks.SKYFOG_LOG);
                        output.accept(MiaBlocks.SKYFOG_WOOD);
                        output.accept(MiaBlocks.STRIPPED_SKYFOG_LOG);
                        output.accept(MiaBlocks.STRIPPED_SKYFOG_WOOD);
                        output.accept(MiaBlocks.SKYFOG_PLANKS);
                        output.accept(MiaBlocks.SKYFOG_STAIRS);
                        output.accept(MiaBlocks.SKYFOG_SLAB);
                        output.accept(MiaBlocks.SKYFOG_FENCE);
                        output.accept(MiaBlocks.SKYFOG_FENCE_GATE);
                        output.accept(MiaBlocks.SKYFOG_DOOR);
                        output.accept(MiaBlocks.SKYFOG_TRAPDOOR);

                        output.accept(MiaBlocks.VERDANT_STEM);
                        output.accept(MiaBlocks.VERDANT_HYPHAE);
                        output.accept(MiaBlocks.STRIPPED_VERDANT_STEM);
                        output.accept(MiaBlocks.STRIPPED_VERDANT_HYPHAE);
                        output.accept(MiaBlocks.VERDANT_PLANKS);
                        output.accept(MiaBlocks.VERDANT_STAIRS);
                        output.accept(MiaBlocks.VERDANT_SLAB);
                        output.accept(MiaBlocks.VERDANT_FENCE);
                        output.accept(MiaBlocks.VERDANT_FENCE_GATE);
                        output.accept(MiaBlocks.VERDANT_DOOR);
                        output.accept(MiaBlocks.VERDANT_TRAPDOOR);

                        output.accept(MiaBlocks.INVERTED_LOG);
                        output.accept(MiaBlocks.INVERTED_WOOD);
                        output.accept(MiaBlocks.STRIPPED_INVERTED_LOG);
                        output.accept(MiaBlocks.STRIPPED_INVERTED_WOOD);
                        output.accept(MiaBlocks.INVERTED_PLANKS);
                        output.accept(MiaBlocks.INVERTED_STAIRS);
                        output.accept(MiaBlocks.INVERTED_SLAB);
                        output.accept(MiaBlocks.INVERTED_FENCE);
                        output.accept(MiaBlocks.INVERTED_FENCE_GATE);
                        output.accept(MiaBlocks.INVERTED_DOOR);
                        output.accept(MiaBlocks.INVERTED_TRAPDOOR);

                        output.accept(MiaBlocks.SKYFOG_LEAVES);
                        output.accept(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS);
                        output.accept(MiaBlocks.VERDANT_LEAVES);
                        output.accept(MiaBlocks.INVERTED_LEAVES);
                        output.accept(MiaBlocks.SKYFOG_SAPLING);
                        output.accept(MiaBlocks.VERDANT_FUNGUS);
                        output.accept(MiaBlocks.INVERTED_SAPLING);

                        // 植物
                        output.accept(MiaBlocks.MARGINAL_WEED);
                        output.accept(MiaBlocks.CRIMSON_VEILGRASS);
                        output.accept(MiaBlocks.SCORCHLEAF);
                        output.accept(MiaBlocks.FORTITUDE_FLOWER);
                        output.accept(MiaBlocks.REED);
                        output.accept(MiaBlocks.BALLOON_PLANT);
                        output.accept(MiaBlocks.GREEN_PERILLA);
                        output.accept(MiaBlocks.KONJAC_ROOT);
                        output.accept(MiaBlocks.SILVEAF_FUNGUS);

                        // 红石
                        output.accept(MiaBlocks.SKYFOG_PRESSURE_PLATE);
                        output.accept(MiaBlocks.SKYFOG_BUTTON);
                        output.accept(MiaBlocks.VERDANT_PRESSURE_PLATE);
                        output.accept(MiaBlocks.VERDANT_BUTTON);
                        output.accept(MiaBlocks.INVERTED_PRESSURE_PLATE);
                        output.accept(MiaBlocks.INVERTED_BUTTON);

                        // 设备
                        output.accept(MiaBlocks.ABYSS_PORTAL);
                        output.accept(MiaItems.STAR_COMPASS);
                        output.accept(MiaBlocks.ABYSS_PORTAL_FRAME);
                        output.accept(MiaBlocks.ABYSS_SPAWNER);

                        // foods
                        output.accept(MiaItems.MISTFUZZ_PEACH);

                        // tools
                        output.accept(MiaItems.RED_WHISTLE);
                        output.accept(MiaItems.BLUE_WHISTLE);

                        output.accept(MiaItems.ROPE);
                    })
                    .build());

    public static final Supplier<CreativeModeTab> MIA_TAB_ARIFACT = CREATIVE_MODE_TAB.register("mia_tab_artifact", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.mia_artifact"))
                    .icon(() -> new ItemStack(MiaItems.FANCY_ARTIFACT_BUNDLE.get()))
                    .displayItems((parameters, output) -> {
                        // artifacts
                        output.accept(MiaBlocks.ARTIFACT_SMITHING_TABLE);
                        output.accept(MiaBlocks.PRASIOLITE_LAMPTUBE);
                        output.accept(MiaBlocks.AMETHYST_LAMPTUBE);
                        output.accept(MiaBlocks.PEDESTAL);
                        output.accept(MiaBlocks.CAVE_EXPLORER_BEACON);
                        output.accept(MiaBlocks.ENDLESS_CUP);

                        output.accept(MiaItems.GRAY_ARTIFACT_BUNDLE);
                        output.accept(MiaItems.FANCY_ARTIFACT_BUNDLE);
                        output.accept(MiaItems.TEST_ARTIFACT_1);
                        output.accept(MiaItems.TEST_ARTIFACT_2);
                        output.accept(MiaItems.TEST_ARTIFACT_3);
                        output.accept(MiaItems.HEALTH_JUNKIE);
                        output.accept(MiaItems.ARTIFACT_HASTE);
                        output.accept(MiaItems.HOOK);
                        output.accept(MiaItems.DEBUG_ATTRIBUTE_TOOL);

                        // Weapons
                        output.accept(MiaItems.GROW_SWORD);
                        output.accept(MiaItems.BLAZE_REAP);

                        // Misc
                        output.accept(MiaItems.PEACE_PHOBIA);
                    }).build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
