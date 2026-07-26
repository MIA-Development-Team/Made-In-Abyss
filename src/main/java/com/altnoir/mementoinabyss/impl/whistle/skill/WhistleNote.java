package com.altnoir.mementoinabyss.impl.whistle.skill;

public enum WhistleNote {
    UP("\u2191"),
    DOWN("\u2193"),
    LEFT("\u2190"),
    RIGHT("\u2192");

    private final String symbol;

    WhistleNote(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
