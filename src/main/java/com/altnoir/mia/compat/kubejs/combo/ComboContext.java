package com.altnoir.mia.compat.kubejs.combo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComboContext {
    private final List<Integer> combo = new ArrayList<>();

    public ComboContext up() {
        combo.add(0);
        return this;
    }

    public ComboContext down() {
        combo.add(1);
        return this;
    }

    public ComboContext left() {
        combo.add(2);
        return this;
    }

    public ComboContext right() {
        combo.add(3);
        return this;
    }

    public ComboContext number(Integer... combos) {
        combo.addAll(Arrays.asList(combos));
        return this;
    }

    public List<Integer> build() {
        return combo;
    }
}