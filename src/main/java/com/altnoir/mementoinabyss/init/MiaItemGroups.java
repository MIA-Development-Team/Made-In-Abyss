package com.altnoir.mementoinabyss.init;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.impl.creative.CreativeTabSection;
import com.altnoir.mementoinabyss.impl.creative.SectionedCreativeModeTab;
import com.altnoir.mementoinabyss.impl.registrate.MiaRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;

public class MiaItemGroups {
    private static final MiaRegistrate REGISTRATE = MementoInAbyss.registrate();

    public static final ResourceKey<CreativeModeTab> BASE_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, MementoInAbyss.asResource("base"));
    public static final ResourceKey<CreativeModeTab> ARTIFACT_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, MementoInAbyss.asResource("artifact"));

    public static final CreativeTabSection BASE_BUILDING_BLOCKS = section(
            BASE_KEY, "base/building_blocks", "itemGroup.mementoinabyss.base.section.building_blocks"
    );
    public static final CreativeTabSection BASE_NATURE_BLOCKS = section(
            BASE_KEY, "base/nature_blocks", "itemGroup.mementoinabyss.base.section.nature_blocks"
    );
    public static final CreativeTabSection BASE_ITEMS = section(
            BASE_KEY, "base/items", "itemGroup.mementoinabyss.base.section.items"
    );
    public static final CreativeTabSection ARTIFACTS = section(
            ARTIFACT_KEY, "artifact/artifacts", "itemGroup.mementoinabyss.artifact.section.artifacts"
    );
    public static final CreativeTabSection FUNCTIONAL_BLOCKS = section(
            ARTIFACT_KEY, "artifact/functional_blocks", "itemGroup.mementoinabyss.artifact.section.functional_blocks"
    );

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> BASE = REGISTRATE.object("base")
            .creativeTab(tab -> SectionedCreativeModeTab.configure(
                    tab.icon(MiaBlocks.COVERGRASS_ABYSS_ANDESITE::asStack),
                    BASE_BUILDING_BLOCKS,
                    BASE_NATURE_BLOCKS,
                    BASE_ITEMS
            ))
            .register();

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> ARTIFACT = REGISTRATE.object("artifact")
            .creativeTab(tab -> SectionedCreativeModeTab.configure(
                    tab.icon(MiaArtifactItems.STAR_COMPASS::asStack),
                    ARTIFACTS,
                    FUNCTIONAL_BLOCKS
            ))
            .register();

    public static void register() {}

    private static CreativeTabSection section(
            ResourceKey<CreativeModeTab> tab,
            String id,
            String translationKey
    ) {
        return new CreativeTabSection(tab, MementoInAbyss.asResource(id), Component.translatable(translationKey));
    }
}
