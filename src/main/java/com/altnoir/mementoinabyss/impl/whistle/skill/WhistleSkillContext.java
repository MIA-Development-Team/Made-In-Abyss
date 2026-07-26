package com.altnoir.mementoinabyss.impl.whistle.skill;

import com.altnoir.mementoinabyss.impl.whistle.component.PlacedWhistleFragment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record WhistleSkillContext(
        ServerPlayer player,
        ItemStack whistle,
        PlacedWhistleFragment fragment,
        double powerMultiplier,
        int cooldownTicks
) {}
