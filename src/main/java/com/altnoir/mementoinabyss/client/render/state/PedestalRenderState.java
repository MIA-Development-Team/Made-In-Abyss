package com.altnoir.mementoinabyss.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.List;

public final class PedestalRenderState extends BlockEntityRenderState {
    public ItemStackRenderState inputItem = new ItemStackRenderState();
    public List<ItemStackRenderState> outputItems = List.of();
    public float animationTime;
}
