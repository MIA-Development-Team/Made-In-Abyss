package com.altnoir.mia.common.attachments;

import com.altnoir.mia.core.curse.ICurse;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class Curse implements INBTSerializable<CompoundTag>, ICurse {
    private int curse = 0;
    private int maxCurse = 10;
    private int curseGradient = 1;

    public Curse() {}

    @Override public int getCurse() { return curse; }

    @Override public void setCurse(int val) {
        if (val > maxCurse) val = maxCurse;
        if (val < 0) val = 0;
        curse = val;
    }
    
    @Override
    public void setCurseByYDelta(double delta) {
        this.curse = Math.max(0, (int) Math.floor(delta) * curseGradient);
    }

    @Override public int getMaxCurse() { return maxCurse; }

    @Override public void setMaxCurse(int val) {
        if (val < 0) val = 1;
        maxCurse = val;
    }
    
    @Override
    public boolean isCurseOverflow() {
        return this.curse >= this.maxCurse;
    }
    
    @Override
    public int getCurseGradient() {
        return curseGradient;
    }
    
    @Override
    public void setCurseGradient(int val) {
        this.curseGradient = Math.max(val, 1);
    }
    
    @Override
    public void addCurse(int delta) {
        this.curse = Math.min(this.maxCurse, this.curse + delta * this.curseGradient);
    }
    
    @Override
    public void minusCurse(int delta) {
        this.curse = Math.max(0, this.curse - delta * curseGradient);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("curse", curse);
        tag.putInt("maxCurse", maxCurse);
        tag.putInt("curseGradient", curseGradient);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        curse = compoundTag.getInt("curse");
        maxCurse = compoundTag.getInt("maxCurse");
        curseGradient = compoundTag.getInt("curseGradient");
    }
}
