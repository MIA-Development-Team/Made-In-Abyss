package com.altnoir.mementoinabyss.content.pedestal.client;

import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public final class PedestalRenderState extends BlockEntityRenderState {
    public ItemStackRenderState inputItem = new ItemStackRenderState();
    public List<ItemStackRenderState> outputItems = List.of();
    public float animationTime;
}
