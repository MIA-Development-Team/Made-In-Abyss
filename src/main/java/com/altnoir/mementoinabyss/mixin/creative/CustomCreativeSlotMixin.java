package com.altnoir.mementoinabyss.mixin.creative;

import com.altnoir.mementoinabyss.client.creative.SectionedCreativeTabRenderer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$CustomCreativeSlot")
public abstract class CustomCreativeSlotMixin extends Slot {
    protected CustomCreativeSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean isHighlightable() {
        return !SectionedCreativeTabRenderer.isHeadingSlot(index);
    }
}
