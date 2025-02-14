package com.altnoir.mia.content.items;

import com.altnoir.mia.Time;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClockItem extends Item {
    public static int stopping_time = 0;

    public ClockItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        p_41433_.setItemInHand(p_41434_, p_41433_.getItemInHand(p_41434_));
        if (!Time.get()) {
            Minecraft.getInstance().gameRenderer.loadEffect(new ResourceLocation("shaders/post/the_world.json"));
        }

        if (p_41433_.level().isClientSide)
            Time.abs(p_41433_);
        p_41433_.getCooldowns().addCooldown(this, 10); //物品冷却时间
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public int getUseDuration(ItemStack p_41454_) {
        return 10;
    }
}