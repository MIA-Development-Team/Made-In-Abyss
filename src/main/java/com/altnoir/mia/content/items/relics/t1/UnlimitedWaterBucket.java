package com.altnoir.mia.content.items.relics.t1;

import com.altnoir.mia.content.items.base.UnlimitedBucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

public class UnlimitedWaterBucket extends UnlimitedBucketItem {
    public UnlimitedWaterBucket() {
        super(Fluids.WATER, (new Item.Properties()).stacksTo(1), 0);
    }
}
