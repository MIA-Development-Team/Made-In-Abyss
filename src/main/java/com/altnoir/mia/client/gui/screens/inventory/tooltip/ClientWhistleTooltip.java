package com.altnoir.mia.client.gui.screens.inventory.tooltip;

import com.altnoir.mia.component.WhistleInventoryComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientWhistleTooltip implements ClientTooltipComponent {
    private static final int SLOT_SIZE_X = 18;
    private static final int SLOT_SIZE_Y = 20;
    private static final int BORDER_WIDTH = 1;
    private final List<ItemStack> stacks;

    public ClientWhistleTooltip(WhistleInventoryComponent data) {
        this.stacks = data.getStacks();
    }

    public static ClientTooltipComponent create(TooltipComponent component) {
        if (component instanceof WhistleInventoryComponent whistle) {
            return new ClientWhistleTooltip(whistle);
        }
        var result = net.neoforged.neoforge.client.gui.ClientTooltipComponentManager.createClientTooltipComponent(component);
        if (result != null) return result;
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    @Override
    public int getHeight() {
        return getGridHeight() * SLOT_SIZE_Y + BORDER_WIDTH * 2;
    }

    @Override
    public int getWidth(Font font) {
        return getGridWidth() * SLOT_SIZE_X + BORDER_WIDTH * 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int cols = getGridWidth();
        int rows = getGridHeight();
        int total = stacks.size();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                int slotX = x + col * SLOT_SIZE_X + BORDER_WIDTH;
                int slotY = y + row * SLOT_SIZE_Y + BORDER_WIDTH;

                if (index < total) {
                    ItemStack stack = stacks.get(index);
                    guiGraphics.renderItem(stack, slotX + 1, slotY + 1, index);
                    guiGraphics.renderItemDecorations(font, stack, slotX + 1, slotY + 1);
                }
            }
        }
    }

    private int getGridWidth() {
        return Math.max(1, (int)Math.ceil(Math.sqrt(stacks.size())));
    }

    private int getGridHeight() {
        return (int)Math.ceil((double) stacks.size() / getGridWidth());
    }
}
