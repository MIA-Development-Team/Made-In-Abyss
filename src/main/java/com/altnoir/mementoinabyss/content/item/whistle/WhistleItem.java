package com.altnoir.mementoinabyss.content.item.whistle;

import com.altnoir.mementoinabyss.impl.whistle.component.WhistleLoadout;
import com.altnoir.mementoinabyss.impl.whistle.grid.WhistleGrid;
import com.altnoir.mementoinabyss.init.MiaDataComponents;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

@Getter
public final class WhistleItem extends Item implements ICurioItem {
    private final WhistleGrid grid;

    public WhistleItem(Properties properties, WhistleGrid grid) {
        super(properties
                .component(MiaDataComponents.WHISTLE_LOADOUT.get(), WhistleLoadout.EMPTY)
                .stacksTo(1));
        this.grid = grid;
    }

    @Override
    public List<Component> getAttributesTooltip(
            List<Component> tooltips,
            TooltipContext context,
            ItemStack stack
    ) {
        return List.of();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
