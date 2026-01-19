package com.altnoir.mia.event.client;

import com.altnoir.mia.client.particle.LeavesParticles;
import com.altnoir.mia.init.MiaParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@OnlyIn(Dist.CLIENT)
public class RegisterParticlesEvent {
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MiaParticles.SKYFOG_LEAVES.get(),
                spriteSet -> (
                        simpleParticleType, clientLevel, x, y, z, xSpeed, ySpeed, zSpeed) ->
                        new LeavesParticles(clientLevel, x, y, z, spriteSet));
    }
}
