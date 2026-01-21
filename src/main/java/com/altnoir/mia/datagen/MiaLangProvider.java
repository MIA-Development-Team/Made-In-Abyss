package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.block.ArtifactSmithingTableBlock;
import com.altnoir.mia.event.client.ClientCurseEvent;
import com.altnoir.mia.event.client.ClientTooltipEvent;
import com.altnoir.mia.event.client.KeyArrowEvent;
import com.altnoir.mia.init.*;
import com.altnoir.mia.item.abs.AbsArtifactBundle;
import com.altnoir.mia.item.abs.IArtifactItem;
import com.altnoir.mia.item.abs.IArtifactSkill;
import com.altnoir.mia.item.abs.IEArtifact;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MiaLangProvider extends LanguageProvider {
    public MiaLangProvider(PackOutput output) {
        super(output, MIA.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemgroup.mia", "Made In Abyss");
        add("itemgroup.mia_artifact", "Made In Abyss: Artifact");

        //Tooltip start
        add(ClientTooltipEvent.TOOLTIP_HOLD_SHIFT, "Hold [%s] to show details");
        add(ClientTooltipEvent.TOOLTIP_ATTRIBUTE_MODIFIER, "%s %s");
        add(ClientTooltipEvent.TOOLTIP_MODIFIERS_ARTIFACT_MATERIAL, "When Applying Artifact: ");

        add("tooltip.mia.whistle.level", "Whistle Level: %s");
        add("tooltip.mia.whistle.capacity", "Capacity: %s");
        add("tooltip.mia.item.red_whistle", "Red Whistles are novice cave raiders allowed only in the Abyss’s first layer.");

        add(IArtifactSkill.TOOLTIP_SKILL_COOLDOWN_VALUE, "Cooldown: %s");

        add(IArtifactItem.TOOLTIP_ARTIFACT_WEIGHT, "Weight: %s");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_S, "S");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_A, "A");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_B, "B");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_C, "C");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_D, "D");
        add(IArtifactItem.TOOLTIP_ARTIFACT_GRADE_UNKNOWN, "Unknown");

        add(IEArtifact.TOOLTIP_ARTIFACT_LEVEL, "Echo: %s/%s");
        add(IEArtifact.TOOLTIP_ARTIFACT_MAX, "Echo: MAX");
        add(AbsArtifactBundle.TOOLTIP_ARTIFACT_BUNDLE_CAPACITY, "Artifact Capacity: %s");

        add("tooltip.mia.description.abyss_compass", "It will point the way toward the abyss.");
        //Tooltip end

        // Painting Variant
        addPainting(MiaPaintingVariants.ABYSS_MAP);
        addPainting(MiaPaintingVariants.THE_ABYSS);
        addPainting(MiaPaintingVariants.THE_ABYSS_2);
        addPainting(MiaPaintingVariants.FOSSIL_TREE);
        addPainting(MiaPaintingVariants.FORTITUDE_FLOWER);

        // Container
        add(ArtifactSmithingTableBlock.TITLE, "Artifact Smithing Table");

        // Skill start
        add(KeyArrowEvent.SKILL_UNSKILL, "Unskill");
        add(KeyArrowEvent.SKILL_COMBO_SKILLS, "Combo Skills");
        add(KeyArrowEvent.SKILL_COOLDOWN, "Cooldown...");
        add(KeyArrowEvent.SKILL_COOLDOWN_TIME, "Cooldown T-");
        add(KeyArrowEvent.SKILL_MINUTE, ":");
        add(KeyArrowEvent.SKILL_SECOND, " ");
        // Skill end

        // kEY start
        add(MiaKeyBinding.KEY_CATEGORIES_MIA, "Made In Abyss");
        add(MiaKeyBinding.KEY_SKILL_DIAL, "Skill Dial");
        // KEY end

        //GUI start
        add(ClientCurseEvent.GUI_DISCONNECT, "Quit?");
        //GUI end

        //Biome start
        addBiome(MiaBiomes.THE_ABYSS, "The Abyss");
        addBiome(MiaBiomes.ABYSS_PLAINS, "Abyss Plains");
        addBiome(MiaBiomes.SKYFOG_FOREST, "Skyfog Forest");
        addBiome(MiaBiomes.PRASIOLITE_CAVES, "Prasiolite Caves");
        addBiome(MiaBiomes.ABYSS_LUSH_CAVES, "Abyss Lush Caves");
        addBiome(MiaBiomes.ABYSS_DRIPSTONE_CAVES, "Abyss Dripstone Caves");

        addBiome(MiaBiomes.TEMPTATION_FOREST, "Temptation Forest");
        //Biome end

        MiaItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(item -> !(item instanceof BlockItem))
                .forEach(item -> {
                    add(item, formatName(BuiltInRegistries.ITEM.getKey(item).getPath()));
                });

        MiaBlocks.BLOCKS.getEntries().stream()
                .map(DeferredHolder::get)
                .forEach(block -> {
                    add(block, formatName(BuiltInRegistries.BLOCK.getKey(block).getPath()));
                });

        MiaPotions.POTIONS.getEntries().forEach(holder -> {
            String name = holder.getId().getPath();
            addPotions(name);
        });

        MiaStats.CUSTOM_STATS.getEntries().forEach(holder -> {
            String name = holder.getId().getPath();
            add("stat.mia.interact_with_" + name, "Interactions with " + formatName(name));
        });

        // Sounds start
        add(MiaSoundsProvider.ABYSS_PORTAL_AMBIENT, "Abyss Portal: Ambient");
        add(MiaSoundsProvider.ABYSS_PORTAL_TRAVEL, "Abyss Portal: Travel");
        // Sounds end

        //Config start
        add("mia.configuration.title", "Made In Abyss Configuration");

        // 通用配置
        add("mia.configuration.abyss_radius", "Abyss Radius Value");
        add("mia.configuration.abyss_radius.tooltip", "Setting The Abyss Radius");

        // 服务端配置
        add("mia.configuration.curse", "Enable Curse");
        add("mia.configuration.curse.tooltip", "Whether to enable the Curse");
        add("mia.configuration.curse_god", "Curse the Creative and Spectator");
        add("mia.configuration.curse_god.tooltip", "Whether to Curse the Creative and Spectator");

        add("mia.configuration.cave_explorer_beacon", "Cave Explorer Beacon");
        add("mia.configuration.cave_explorer_beacon.tooltip", "Cave Explorer Beacon Configuration");
        add("mia.configuration.cave_explorer_beacon_horizontal", "Horizontal Range");
        add("mia.configuration.cave_explorer_beacon_horizontal.tooltip", "Setting The Cave Explorer Beacon Horizontal Range");
        add("mia.configuration.cave_explorer_beacon_vertical", "Vertical Range");
        add("mia.configuration.cave_explorer_beacon_vertical.tooltip", "Setting The Cave Explorer Beacon Vertical Range");
        add("mia.configuration.cave_explorer_beacon_max_vertical", "Max Vertical Range");
        add("mia.configuration.cave_explorer_beacon_max_vertical.tooltip", "Setting The Cave Explorer Beacon Max Vertical");

        // 客户端配置
        add("mia.configuration.curse_icon_position", "Curse Icon Position");
        add("mia.configuration.curse_icon_position.tooltip", "Switch Curse Icon Position");
        add("mia.configuration.curse_icon_position.middle", "Middle");
        add("mia.configuration.curse_icon_position.right", "Right");
        add("mia.configuration.curse_icon_position.hidden", "Hidden");
        add("mia.configuration.disconnect_button_state", "Disconnect Button State");
        add("mia.configuration.disconnect_button_state.tooltip", "Adjust the Disconnect Button");
        add("mia.configuration.disconnect_button_state.default", "Default");
        add("mia.configuration.disconnect_button_state.disabled", "Disabled");
        add("mia.configuration.disconnect_button_state.hidden", "Hidden");

        add("mia.configuration.hook", "Hook");
        add("mia.configuration.hook.tooltip", "Hook Configuration");
        add("mia.configuration.auto_hook", "auto hook");
        add("mia.configuration.auto_hook.tooltip", "Whether to enable auto hook");
        //Config end

        // Tags start
        add("tag.item.curios.whistle", "Whistle");
        add("tag.item.mia.fossilized_logs", "Fossilized Logs");
        add("tag.item.mia.stripped_fossilized_logs", "Stripped Fossilized Logs");
        add("tag.item.mia.skyfog_logs", "Skyfog Logs");
        // Tags end

        add("pack.mia.music_pack.name", "Made In Abyss: Music Pack");

        //Compat start
        add("curios.identifier.whistle", "Whistle");
        add("curios.modifiers.whistle", "Red Whistle Attributes");
        add("emi.category.mia.lamp_tube", "Laser Catalysis");
        //Compat end
    }

    private void addPainting(ResourceKey<PaintingVariant> key) {
        add("painting.mia." + key.location().getPath() + ".title", formatName(key.location().getPath()));
        add("painting.mia." + key.location().getPath() + ".author", "Made In Abyss");
    }

    private void addBiome(ResourceKey<Biome> biome, String name) {
        add("biome.mia." + biome.location().getPath(), name);
    }

    private void addPotions(String key) {
        String key1 = "item.minecraft.potion.effect." + key;
        String key2 = "item.minecraft.splash_potion.effect." + key;
        String key3 = "item.minecraft.lingering_potion.effect." + key;

        add(key1, "Potion of " + formatName(key));
        add(key2, "Splash Potion of " + formatName(key));
        add(key3, "Lingering Potion of " + formatName(key));
    }

    private String formatName(String name) {
        return Arrays.stream(name.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word ->
                        Character.toUpperCase(word.charAt(0)) +
                                word.substring(1).toLowerCase()
                )
                .collect(Collectors.joining(" "));
    }
}
