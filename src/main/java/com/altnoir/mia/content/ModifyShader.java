package com.altnoir.mia.content;

import com.altnoir.mia.client.ShaderGetter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModifyShader {
    public static float time_the_world = 0F;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void setup(TickEvent.RenderTickEvent event) {
        if (ShaderGetter.nameEquals("post/the_world.json")) {
            time_the_world += 0.01F;

            ShaderGetter.updateUniform_post("time", time_the_world)
            ;
        } else time_the_world = 0f;
    }
}