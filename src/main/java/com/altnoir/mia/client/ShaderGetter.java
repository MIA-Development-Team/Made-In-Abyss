package com.altnoir.mia.client;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class ShaderGetter {
    public static Minecraft mc = Minecraft.getInstance();
    public static GameRenderer gameRenderer = mc.gameRenderer;

    public static PostChain currentEffect() {
        return gameRenderer.currentEffect();
    }

    public static void load(ResourceLocation rs) {
        mc.gameRenderer.loadEffect(rs);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void updateUniform_post(String name, float value) {
        if (Minecraft.getInstance().gameRenderer.currentEffect() == null)
            return;
        for (PostPass s : Minecraft.getInstance().gameRenderer.currentEffect().passes) {
            Uniform su = s.getEffect().getUniform(name);
            if (su != null) {
                su.set(value);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void updateUniform_core(ShaderInstance ins, String name, float value) {
        if (ins == null)
            return;
        ins.getUniform(name).set(value);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void updateUniform_post(String name, float[] value) {
        if (Minecraft.getInstance().gameRenderer.currentEffect() == null)
            return;
        for (PostPass s : Minecraft.getInstance().gameRenderer.currentEffect().passes) {
            Uniform su = s.getEffect().getUniform(name);
            if (su != null) {
                su.set(value);
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void updateUniform_core(ShaderInstance ins, String name, float[] value) {
        if (ins == null)
            return;
        ins.getUniform(name).set(value);
    }

    public static boolean nameEquals(String name) throws NullPointerException {
        return currentEffect() != null && currentEffect().getName().contains(name);
    }
}