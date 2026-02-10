package com.altnoir.mia.core.curse;

public interface ICurse {
    int getCurse();
    void setCurse(int value);
    int getMaxCurse();
    void setMaxCurse(int value);
    void addCurse(int amount);
    void minusCurse(int delta);
}
