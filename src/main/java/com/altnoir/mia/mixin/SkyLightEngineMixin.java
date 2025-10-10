package com.altnoir.mia.mixin;

import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.altnoir.mia.worldgen.noise_setting.densityfunction.AbyssBrinkHole;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.SkyLightEngine;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyLightEngine.class)
public abstract class SkyLightEngineMixin {
    @Unique
    private LightChunkGetter mia$chunkSource;

    @Inject(method = "<init>*", at = @At("TAIL"))
    private void onInit(LightChunkGetter chunkSource, SkyLightSectionStorage storage, CallbackInfo ci) {
        this.mia$chunkSource = chunkSource;
    }

    @Inject(method = "propagateLightSources", at = @At("TAIL"), cancellable = true)
    private void onPropagateLightSources(ChunkPos chunkPos, CallbackInfo ci) {
        net.minecraft.world.level.BlockGetter level = this.mia$chunkSource.getLevel();

        if (level instanceof ServerLevel serverLevel) {
            ResourceKey<Level> dimension = serverLevel.dimension();

            if (dimension == MiaDimensions.ABYSS_BRINK_LEVEL) {
                double distance = Math.sqrt(chunkPos.x * chunkPos.x + chunkPos.z * chunkPos.z);

                if (distance > AbyssBrinkHole.getAbyssRadius() / 16) {
                    ci.cancel();
                }
            }
        }
    }
}
