package com.altnoir.mia.mixin;

import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "getSituationalMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"), cancellable = true)
    private void modifyDimensionCheck(CallbackInfoReturnable<Music> cir) {
        Minecraft minecraft = (Minecraft) (Object) this;
        Player player = minecraft.player;
        Set<ResourceKey<Level>> dims = mia$getExcludedDimensions();

        if (player != null && dims.contains(player.level().dimension())) {
            Holder<Biome> holder = player.level().getBiome(player.blockPosition());

            cir.setReturnValue(holder.value().getBackgroundMusic().orElse(Musics.GAME));
        }
    }

    @Unique
    private Set<ResourceKey<Level>> mia$getExcludedDimensions() {
        return Set.of(
                MiaDimensions.ABYSS_EDGE_LEVEL
        );
    }
}