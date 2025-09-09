package com.altnoir.mia.item;

import com.altnoir.mia.init.MiaBlockTags;
import com.google.common.collect.Sets;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeItem extends DiggerItem {
    public static final Set<ItemAbility> DEFAULT_COMPOSITE_ACTIONS = of(ItemAbilities.PICKAXE_DIG, ItemAbilities.AXE_DIG, ItemAbilities.SHOVEL_DIG);

    public CompositeItem(Tier tier, Properties properties) {
        super(tier, MiaBlockTags.MINEABLE_WITH_COMPOSITE, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return DEFAULT_COMPOSITE_ACTIONS.contains(itemAbility);
    }

    private static Set<ItemAbility> of(ItemAbility... actions) {
        return Stream.of(actions).collect(Collectors.toCollection(Sets::newIdentityHashSet));
    }
}
