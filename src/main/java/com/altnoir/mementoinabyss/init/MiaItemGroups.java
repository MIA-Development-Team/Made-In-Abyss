package com.altnoir.mementoinabyss.init;

import com.altnoir.abysslib.creative.CreativeTabSection;
import com.altnoir.abysslib.creative.SectionedCreativeModeTab;
import com.altnoir.abysslib.reginth.Reginth;
import com.altnoir.abysslib.reginth.util.entry.RegistryEntry;
import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class MiaItemGroups {
    private static final Reginth REGISTRATE = MementoInAbyss.registrate();

    public static final ResourceKey<CreativeModeTab> BASE_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, MementoInAbyss.asResource("mia"));
    public static final ResourceKey<CreativeModeTab> ARTIFACT_KEY =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB, MementoInAbyss.asResource("mia_artifact"));

    public static final CreativeTabSection BASE_BUILDING_BLOCKS =
            section(
                    BASE_KEY,
                    "mia/building_blocks",
                    "itemGroup.mementoinabyss.mia.section.building_blocks",
                    "creative_inventory/section/building_blocks");
    public static final CreativeTabSection BASE_NATURE_BLOCKS =
            section(
                    BASE_KEY,
                    "mia/nature_blocks",
                    "itemGroup.mementoinabyss.mia.section.nature_blocks",
                    "creative_inventory/section/nature_blocks");
    public static final CreativeTabSection BASE_ITEMS =
            section(
                    BASE_KEY,
                    "mia/items",
                    "itemGroup.mementoinabyss.mia.section.items",
                    "creative_inventory/section/items");
    public static final CreativeTabSection ARTIFACTS =
            section(
                    ARTIFACT_KEY,
                    "mia_artifact/artifacts",
                    "itemGroup.mementoinabyss.mia_artifact.section.artifacts",
                    "creative_inventory/section/artifacts");
    public static final CreativeTabSection FUNCTIONAL_BLOCKS =
            section(
                    ARTIFACT_KEY,
                    "mia_artifact/functional_blocks",
                    "itemGroup.mementoinabyss.mia_artifact.section.functional_blocks",
                    "creative_inventory/section/functional_blocks");

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> BASE =
            REGISTRATE
                    .object("mia")
                    .creativeTab(
                            tab ->
                                    SectionedCreativeModeTab.configure(
                                            tab.icon(MiaBlocks.COVERGRASS_ABYSS_ANDESITE::asStack),
                                            BASE_BUILDING_BLOCKS,
                                            BASE_NATURE_BLOCKS,
                                            BASE_ITEMS))
                    .register();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> ARTIFACT =
            REGISTRATE
                    .object("mia_artifact")
                    .creativeTab(
                            tab ->
                                    SectionedCreativeModeTab.configure(
                                            tab.icon(MiaArtifactItems.STAR_COMPASS::asStack),
                                            ARTIFACTS,
                                            FUNCTIONAL_BLOCKS))
                    .register();

    public static void register() {}

    private static CreativeTabSection section(
            ResourceKey<CreativeModeTab> tab,
            String id,
            String translationKey,
            String bannerSprite) {
        return new CreativeTabSection(
                tab,
                MementoInAbyss.asResource(id),
                Component.translatable(translationKey),
                MementoInAbyss.asResource(bannerSprite));
    }
}
