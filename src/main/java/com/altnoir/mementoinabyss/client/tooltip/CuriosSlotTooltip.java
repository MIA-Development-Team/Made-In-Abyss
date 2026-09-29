package com.altnoir.mementoinabyss.client.tooltip;

import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Curios inserts its slot line near the item name. Keep it after every other tooltip line. */
public final class CuriosSlotTooltip {
    private static final String KEY = "curios.tooltip.slot";

    private CuriosSlotTooltip() {}

    public static void moveToEnd(List<Either<FormattedText, TooltipComponent>> elements) {
        for (int index = 0; index < elements.size() - 1; index++) {
            if (isSlot(elements.get(index))) {
                elements.add(elements.remove(index));
                return;
            }
        }
    }

    private static boolean isSlot(Either<FormattedText, TooltipComponent> element) {
        return element.left().filter(CuriosSlotTooltip::isSlotText).isPresent();
    }

    private static boolean isSlotText(FormattedText text) {
        return text instanceof Component component
                && component.getContents() instanceof TranslatableContents contents
                && KEY.equals(contents.getKey());
    }
}
