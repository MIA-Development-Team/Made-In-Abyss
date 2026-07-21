package com.altnoir.mementoinabyss.mixin.lighting;

import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLightListener;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightInterface", remap = false)
public abstract class ScalableLuxStarLightInterfaceMixin {
    @Unique
    private RegionalSkyLight.Region mia$skyLightRegion;
    @Unique
    private RegionalSkyLightListener mia$skyReader;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void mia$resolveSkyLightRegion(
            LightChunkGetter lightAccess, boolean hasSkyLight, boolean hasBlockLight,
            LevelLightEngine lightEngine, CallbackInfo ci) {
        this.mia$skyLightRegion = RegionalSkyLight.resolve(lightAccess);
    }

    @ModifyReturnValue(method = "getSkyLightValue", at = @At("RETURN"), remap = false)
    private int mia$maskSkyLight(int original, BlockPos pos, ChunkAccess chunk) {
        return this.mia$skyLightRegion == null ? original
                : this.mia$skyLightRegion.clampSkyLight(pos.getX(), pos.getZ(), original);
    }

    @Inject(method = "getSkyReader", at = @At("RETURN"), cancellable = true, remap = false)
    private void mia$wrapSkyReader(CallbackInfoReturnable<LayerLightEventListener> cir) {
        if (this.mia$skyLightRegion == null) return;

        LayerLightEventListener delegate = cir.getReturnValue();
        if (this.mia$skyReader == null || !this.mia$skyReader.wraps(delegate)) {
            this.mia$skyReader = new RegionalSkyLightListener(delegate, this.mia$skyLightRegion);
        }
        cir.setReturnValue(this.mia$skyReader);
    }
}
