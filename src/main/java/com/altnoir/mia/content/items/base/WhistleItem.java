package com.altnoir.mia.content.items.base;

import com.altnoir.mia.SoundsRegister;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class WhistleItem extends Item
{
    // 冷却时间（单位：tick）
    private int cooldownTicks;
    private float vol;
    private float pit;

    public WhistleItem(Properties properties, int cooldownTicks, float vol, float pit) {
        super(properties);
        this.cooldownTicks = cooldownTicks;
        this.pit = pit;
        this.vol = vol;
    }

    // 获取冷却时间
    public int getCooldownTicks() {
        return cooldownTicks;
    }

    // 设置冷却时间
    public void setCooldownTicks(int cooldownTicks)
    {
        this.cooldownTicks = cooldownTicks;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        // 播放声音
        if (!level.isClientSide)
        {
            SoundEvent soundEvent = getSoundEvent(); // 获取音效
            float vol = getvol();
            float pit = getpit();
            player.playSound(soundEvent,vol,pit);
        }

        // 设置冷却时间
        player.getCooldowns().addCooldown(this, cooldownTicks);

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    protected float getvol()
    {
        return vol;
    }
    protected float getpit()
    {
        return pit;
    }
    protected SoundEvent getSoundEvent()
    {
        return SoundsRegister.YOUR_WORTH.get(); // 默认音效
    }
}