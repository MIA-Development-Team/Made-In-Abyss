package com.altnoir.mia.content.items;
import com.altnoir.mia.SoundsRegister;
import com.altnoir.mia.content.items.base.WhistleItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

public class Blue_Whistle extends WhistleItem {

    public Blue_Whistle(Item.Properties properties) {
        super(properties, 120,1.0F,1.0F); // 设置冷却时间为120tick
    }

    @Override
    protected SoundEvent getSoundEvent() {
        // 覆盖默认音效
        return SoundsRegister.COMMON_WHISTLE.get();
    }
}