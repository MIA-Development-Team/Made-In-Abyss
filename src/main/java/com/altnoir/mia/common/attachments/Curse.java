package com.altnoir.mia.common.attachments;

import com.altnoir.mia.common.core.curse.ICurse;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class Curse implements INBTSerializable<CompoundTag>, ICurse {
    private int curse = 0;
    private int maxCurse = 10;

    public Curse() {}

    @Override public int getCurse() { return curse; }

    @Override public void setCurse(int val) {
        if (val > maxCurse) val = maxCurse;
        if (val < 0) val = 0;
        curse = val;
    }

    @Override public int getMaxCurse() { return maxCurse; }

    @Override public void setMaxCurse(int val) {
        if (val < 0) val = 1;
        maxCurse = val;
    }

    @Override
    public void addCurse(int delta) {
        if (this.curse < this.maxCurse) {
            this.curse += delta;
        }
    }

    @Override
    public void minusCurse(int delta) {
        if (this.curse > delta) {
            this.curse -= delta;
        } else {
            this.curse = 0;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("curse", curse);
        tag.putInt("maxCurse", maxCurse);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        curse = compoundTag.getInt("curse");
        maxCurse = compoundTag.getInt("maxCurse");
    }
}
