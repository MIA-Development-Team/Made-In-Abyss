package com.altnoir.mementoinabyss.mixin.lighting;

import com.altnoir.mementoinabyss.worldgen.lighting.RegionalSkyLight;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection", remap = false)
public abstract class SodiumClonedChunkSectionMixin {
    @ModifyReturnValue(method = "copyLightArray", at = @At("RETURN"), remap = false)
    private static DataLayer mia$maskCopiedSkyLight(
            DataLayer original, Level level, LightLayer layer, SectionPos pos) {
        if (layer != LightLayer.SKY) return original;
        RegionalSkyLight.Region region = RegionalSkyLight.resolve(level.dimension());
        return region == null ? original : RegionalSkyLight.maskDataLayer(region, pos, original);
    }
}
