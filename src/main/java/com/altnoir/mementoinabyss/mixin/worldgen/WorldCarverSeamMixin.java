package com.altnoir.mementoinabyss.mixin.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.AbyssSeamRuntime;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldCarver.class)
public abstract class WorldCarverSeamMixin {
    @Inject(method = "carveBlock", at = @At("HEAD"), cancellable = true)
    private void mia$protectSeam(
            CarvingContext context,
            CarverConfiguration configuration,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeGetter,
            CarvingMask mask,
            BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos helper,
            Aquifer aquifer,
            MutableBoolean hasGrass,
            CallbackInfoReturnable<Boolean> ci) {
        var seam = AbyssSeamRuntime.seamOf(context.randomState());
        if (seam != null
                && AbyssSeamRuntime.protectedFromCarver(seam, pos.getX(), pos.getY(), pos.getZ()))
            ci.setReturnValue(false);
    }
}
