package com.altnoir.mia.mixin;

import com.altnoir.mia.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FlowingFluid.class)
public class FlowingFluidMixin {
    @Inject(method = "canHoldFluid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
            shift = At.Shift.AFTER,
            ordinal = 0),
            cancellable = true)
    private void injectAfterNetherPortalCheck(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(MiaBlocks.ABYSS_PORTAL.get())) {
            cir.setReturnValue(false);
        }
    }
}