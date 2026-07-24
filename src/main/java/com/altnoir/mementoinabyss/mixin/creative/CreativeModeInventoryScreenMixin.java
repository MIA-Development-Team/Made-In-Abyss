package com.altnoir.mementoinabyss.mixin.creative;

import com.altnoir.mementoinabyss.client.creative.SectionedCreativeTabRenderer;
import com.altnoir.mementoinabyss.impl.creative.SectionedCreativeModeTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    private float scrollOffs;

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void mementoinabyss$extractSectionHeadings(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        if (selectedTab instanceof SectionedCreativeModeTab sectionedTab) {
            SectionedCreativeTabRenderer.extract(
                    (CreativeModeInventoryScreen) (Object) this,
                    graphics,
                    sectionedTab,
                    scrollOffs
            );
        } else {
            SectionedCreativeTabRenderer.clearHeadingSlots();
        }
    }
}
