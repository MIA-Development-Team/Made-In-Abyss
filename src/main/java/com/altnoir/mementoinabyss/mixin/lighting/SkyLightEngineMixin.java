package com.altnoir.mementoinabyss.mixin.lighting;

import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
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
    private RegionalSkyLight.Region mia$skyLightRegion;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LightChunkGetter;Lnet/minecraft/world/level/lighting/SkyLightSectionStorage;)V", at = @At("RETURN"))
    private void mia$resolveSkyLightRegion(
            LightChunkGetter chunkSource, SkyLightSectionStorage storage, CallbackInfo ci) {
        this.mia$skyLightRegion = RegionalSkyLight.resolve(chunkSource);
    }

    @Inject(method = "checkNode", at = @At("HEAD"), cancellable = true)
    private void mia$skipChecksOutsideRegion(long blockNode, CallbackInfo ci) {
        if (this.mia$skyLightRegion != null
                && !this.mia$skyLightRegion.allowsSkyLight(
                        BlockPos.getX(blockNode), BlockPos.getZ(blockNode))) {
            ci.cancel();
        }
    }

    @Inject(method = "setLightEnabled", at = @At("HEAD"), cancellable = true)
    private void mia$skipEnablingOutsideRegion(ChunkPos pos, boolean enable, CallbackInfo ci) {
        if (enable && !mia$intersects(pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "propagateLightSources", at = @At("HEAD"), cancellable = true)
    private void mia$skipSourcesOutsideRegion(ChunkPos pos, CallbackInfo ci) {
        if (!mia$intersects(pos)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean mia$intersects(ChunkPos pos) {
        return this.mia$skyLightRegion == null || this.mia$skyLightRegion.intersectsChunk(pos.x(), pos.z());
    }
}
