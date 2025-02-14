package com.altnoir.mia.content;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

public class RenderEvent extends TickEvent {
    public final float renderTickTime;

    public RenderEvent(TickEvent.Phase phase, float renderTickTime) {
        super(TickEvent.Type.RENDER, LogicalSide.CLIENT, phase);
        this.renderTickTime = renderTickTime;
    }
}