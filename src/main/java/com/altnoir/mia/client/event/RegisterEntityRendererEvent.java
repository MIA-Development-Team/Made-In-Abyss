package com.altnoir.mia.client.event;

import com.altnoir.mia.client.particle.LeavesParticles;
import com.altnoir.mia.init.MiaEntities;
import com.altnoir.mia.init.MiaParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@OnlyIn(Dist.CLIENT)
public class RegisterEntityRendererEvent {

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // event.registerEntityRenderer(MiaEntities.BEAM_ENTITY.get(), new BeamEntityRenderer());
    }
}
