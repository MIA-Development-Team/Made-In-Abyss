package com.altnoir.mementoinabyss.mixin.lighting;

import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLightStorage;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyLightSectionStorage.class)
public abstract class SkyLightSectionStorageMixin {
    @ModifyReturnValue(method = "getLightValue(JZ)I", at = @At("RETURN"))
    private int mia$maskVisibleSkyLight(int original, long blockNode, boolean updating) {
        RegionalSkyLight.Region region = ((RegionalSkyLightStorage) this).mia$getSkyLightRegion();
        return region == null ? original : region.clampSkyLight(
                BlockPos.getX(blockNode), BlockPos.getZ(blockNode), original);
    }
}
