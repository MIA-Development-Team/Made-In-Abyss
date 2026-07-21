package com.altnoir.mementoinabyss.mixin.lighting;

import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLightStorage;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerLightSectionStorage.class)
public abstract class LayerLightSectionStorageMixin implements RegionalSkyLightStorage {
    @Shadow @Final private LightLayer layer;
    @Shadow @Final protected LightChunkGetter chunkSource;

    @Unique
    private RegionalSkyLight.Region mia$skyLightRegion;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mia$resolveSkyLightRegion(CallbackInfo ci) {
        if (this.layer == LightLayer.SKY) {
            this.mia$skyLightRegion = RegionalSkyLight.resolve(this.chunkSource);
        }
    }

    @Override
    public RegionalSkyLight.Region mia$getSkyLightRegion() {
        return this.mia$skyLightRegion;
    }

    @ModifyReturnValue(method = "getStoredLevel", at = @At("RETURN"))
    private int mia$maskStoredSkyLight(int original, long blockNode) {
        return this.mia$skyLightRegion == null ? original : this.mia$skyLightRegion.clampSkyLight(
                BlockPos.getX(blockNode), BlockPos.getZ(blockNode), original);
    }

    @Inject(method = "setStoredLevel", at = @At("HEAD"), cancellable = true)
    private void mia$preventStoredSkyLightOutsideRegion(long blockNode, int level, CallbackInfo ci) {
        if (!mia$allows(blockNode)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateSectionStatus", at = @At("HEAD"), cancellable = true)
    private void mia$skipSectionsOutsideRegion(long sectionNode, boolean sectionEmpty, CallbackInfo ci) {
        if (!mia$intersectsSection(sectionNode)) {
            ci.cancel();
        }
    }

    @Inject(method = "setLightEnabled", at = @At("HEAD"), cancellable = true)
    private void mia$skipLightColumnsOutsideRegion(long zeroNode, boolean enable, CallbackInfo ci) {
        if (enable && !mia$intersectsSection(zeroNode)) {
            ci.cancel();
        }
    }

    @Inject(method = "queueSectionData", at = @At("HEAD"), cancellable = true)
    private void mia$discardQueuedSkyLightOutsideRegion(long sectionNode, @Nullable DataLayer data, CallbackInfo ci) {
        if (data != null && !mia$intersectsSection(sectionNode)) {
            ci.cancel();
        }
    }

    @Inject(method = "retainData", at = @At("HEAD"), cancellable = true)
    private void mia$skipRetainedSkyLightOutsideRegion(long zeroNode, boolean retain, CallbackInfo ci) {
        if (retain && !mia$intersectsSection(zeroNode)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean mia$allows(long blockNode) {
        return this.mia$skyLightRegion == null
                || this.mia$skyLightRegion.allowsSkyLight(BlockPos.getX(blockNode), BlockPos.getZ(blockNode));
    }

    @Unique
    private boolean mia$intersectsSection(long sectionNode) {
        return this.mia$skyLightRegion == null
                || this.mia$skyLightRegion.intersectsChunk(SectionPos.x(sectionNode), SectionPos.z(sectionNode));
    }
}
