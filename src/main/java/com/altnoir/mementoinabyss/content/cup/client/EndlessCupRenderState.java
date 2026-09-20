package com.altnoir.mementoinabyss.content.cup.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;

public final class EndlessCupRenderState extends BlockEntityRenderState {
    public boolean showWater;
    public int waterColor = 0x3F76E4;
    public @Nullable TextureAtlasSprite waterSprite;
}
