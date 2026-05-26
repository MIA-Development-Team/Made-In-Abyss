package com.altnoir.mementoinabyss.impl.lang;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MiaLang {
    private MutableComponent component;

    public MiaLang() {
    }

    public MiaLang text(String text) {
        return add(Component.literal(text));
    }

    public MiaLang text(ChatFormatting format, String text) {
        return add(Component.literal(text).withStyle(format));
    }

    public MiaLang translate(String key, Object... args) {
        return add(Component.translatable(MementoInAbyss.ID + "." + key, resolve(args)));
    }

    public MiaLang add(Component comp) {
        if (component == null)
            component = comp.copy();
        else
            component.append(comp);
        return this;
    }

    public MiaLang style(ChatFormatting format) {
        check();
        component.withStyle(format);
        return this;
    }

    public MiaLang color(int color) {
        check();
        component.withStyle(s -> s.withColor(color));
        return this;
    }

    public MutableComponent build() {
        check();
        return component;
    }

    public String string() {
        return build().getString();
    }

    private void check() {
        if (component == null)
            throw new IllegalStateException("Empty LangBuilder");
    }

    private static Object[] resolve(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof MiaLang lb)
                args[i] = lb.build();
        }
        return args;
    }
}
