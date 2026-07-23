package com.altnoir.mementoinabyss.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

public record ItemDescription(
        List<Component> defaultLines,
        List<Component> shiftLines,
        List<Component> controlLines
) {
    public static ItemDescription create(Item item, TooltipPalette palette) {
        return create(item, palette, false);
    }

    public static ItemDescription create(Item item, TooltipPalette palette, boolean externalControls) {
        String translationKey = item.getDescriptionId() + ".tooltip";
        if (!I18n.exists(translationKey + ".summary")) {
            return null;
        }
        Builder builder = new Builder(palette);
        if (externalControls) {
            builder.externalControls();
        }
        builder.summary(I18n.get(translationKey + ".summary"));
        for (int i = 1; I18n.exists(translationKey + ".condition" + i); i++) {
            builder.behaviour(
                    I18n.get(translationKey + ".condition" + i),
                    I18n.get(translationKey + ".behaviour" + i)
            );
        }
        for (int i = 1; I18n.exists(translationKey + ".control" + i); i++) {
            builder.action(
                    I18n.get(translationKey + ".control" + i),
                    I18n.get(translationKey + ".action" + i)
            );
        }
        return builder.build();
    }

    public List<Component> currentLines() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hasShiftDown()) {
            return shiftLines;
        }
        if (minecraft.hasControlDown()) {
            return controlLines;
        }
        return defaultLines;
    }

    public static final class Modifier implements TooltipModifier {
        private final TooltipPalette palette;
        private final boolean externalControls;
        private String cachedLanguage = "";
        private ItemDescription description;

        public Modifier(TooltipPalette palette) {
            this(palette, false);
        }

        public Modifier(TooltipPalette palette, boolean externalControls) {
            this.palette = palette;
            this.externalControls = externalControls;
        }

        @Override
        public void modify(ItemTooltipEvent event) {
            String language = Minecraft.getInstance().getLanguageManager().getSelected();
            if (!language.equals(cachedLanguage)) {
                cachedLanguage = language;
                description = create(event.getItemStack().getItem(), palette, externalControls);
            }
            if (description != null) {
                event.getToolTip().addAll(Math.min(1, event.getToolTip().size()), description.currentLines());
            }
        }
    }

    public static final class Builder {
        private final TooltipPalette palette;
        private final List<String> summaries = new ArrayList<>();
        private final List<Entry> behaviours = new ArrayList<>();
        private final List<Entry> actions = new ArrayList<>();
        private boolean externalControls;

        public Builder(TooltipPalette palette) {
            this.palette = palette;
        }

        public Builder summary(String summary) {
            summaries.add(summary);
            return this;
        }

        public Builder behaviour(String condition, String behaviour) {
            behaviours.add(new Entry(condition, behaviour));
            return this;
        }

        public Builder action(String control, String action) {
            actions.add(new Entry(control, action));
            return this;
        }

        public Builder externalControls() {
            externalControls = true;
            return this;
        }

        public ItemDescription build() {
            List<Component> defaultLines = new ArrayList<>();
            List<Component> shiftLines = new ArrayList<>();
            List<Component> controlLines = new ArrayList<>();

            for (String summary : summaries) {
                shiftLines.addAll(TooltipHelper.wrap(summary, palette));
            }
            if (!summaries.isEmpty() && !behaviours.isEmpty()) {
                shiftLines.add(CommonComponents.EMPTY);
            }
            for (Entry behaviour : behaviours) {
                shiftLines.add(Component.literal(behaviour.heading()).withStyle(palette.highlight()));
                shiftLines.addAll(TooltipHelper.wrap(behaviour.body(), palette, 1));
            }
            for (Entry action : actions) {
                controlLines.add(Component.literal(action.heading()).withStyle(palette.highlight()));
                controlLines.addAll(TooltipHelper.wrap(action.body(), palette, 1));
            }

            boolean hasDescription = !shiftLines.isEmpty();
            boolean hasControls = externalControls || !controlLines.isEmpty();
            if (hasDescription || hasControls) {
                addTabs(defaultLines, false, false, hasDescription, hasControls, palette);
                addTabs(shiftLines, true, false, hasDescription, hasControls, palette);
                addTabs(controlLines, false, true, hasDescription, hasControls, palette);
            }
            if (!hasDescription) {
                shiftLines = List.copyOf(defaultLines);
            }
            if (!hasControls) {
                controlLines = List.copyOf(defaultLines);
            }
            return new ItemDescription(
                    List.copyOf(defaultLines),
                    List.copyOf(shiftLines),
                    List.copyOf(controlLines)
            );
        }

        private static void addTabs(
                List<Component> lines,
                boolean shiftSelected,
                boolean controlSelected,
                boolean hasDescription,
                boolean hasControls,
                TooltipPalette palette
        ) {
            int index = 0;
            if (hasDescription) {
                lines.add(index++, tab(
                        "tooltip.mementoinabyss.hold_for_description",
                        "tooltip.mementoinabyss.key_shift",
                        shiftSelected,
                        palette
                ));
            }
            if (hasControls) {
                lines.add(index++, tab(
                        "tooltip.mementoinabyss.hold_for_controls",
                        "tooltip.mementoinabyss.key_control",
                        controlSelected,
                        palette
                ));
            }
            if ((shiftSelected || controlSelected) && index > 0) {
                lines.add(index, CommonComponents.EMPTY);
            }
        }

        private static MutableComponent tab(
                String messageKey,
                String keyName,
                boolean selected,
                TooltipPalette palette
        ) {
            Component key = Component.translatable(keyName)
                    .withStyle(selected ? palette.highlight() : palette.muted());
            return Component.translatable(messageKey, key).withStyle(palette.primary());
        }

        private record Entry(String heading, String body) {}
    }
}
