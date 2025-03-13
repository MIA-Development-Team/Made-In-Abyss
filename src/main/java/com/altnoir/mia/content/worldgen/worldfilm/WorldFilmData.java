package com.altnoir.mia.content.worldgen.worldfilm;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class WorldFilmData implements INBTSerializable<CompoundTag> {
    private final float[] thicknessMap = new float[16 * 16];
    private static final int PRECISION = 1000;
    private boolean initialized = false;

    public float getThickness(int x, int z) {
        return thicknessMap[x + z * 16];
    }

    public void setThickness(int x, int z, float value) {
        thicknessMap[x + z * 16] = value;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        int[] intData = new int[thicknessMap.length];

        for (int i = 0; i < thicknessMap.length; i++) {
            intData[i] = (int)(thicknessMap[i] * PRECISION);
        }

        tag.putIntArray("ThicknessData", intData);
        tag.putBoolean("Initialized", initialized); // 持久化初始化状态
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        int[] intData = tag.getIntArray("ThicknessData");
        for (int i = 0; i < Math.min(intData.length, thicknessMap.length); i++) {
            thicknessMap[i] = intData[i] / (float)PRECISION;
        }
        initialized = tag.getBoolean("Initialized"); // 恢复初始化状态
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized() {
        initialized = true;
    }
}
