package com.altnoir.mia.core.curse;

public interface ICurse {
    int getCurse();
    void setCurse(int value);
    void setCurseByYDelta(double height);
    
    int getMaxCurse();
    void setMaxCurse(int value);
    boolean isCurseOverflow();
    
    int getCurseGradient();
    void setCurseGradient(int value);
    
    void addCurse(int amount);
    void minusCurse(int delta);
}
