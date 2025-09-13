package com.altnoir.mia.mixin;

import com.altnoir.mia.init.MiaAttributes;
import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
  /*  @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
    private void modifyDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        float originalValue = cir.getReturnValue();
        float destroySpeed = MiaUtil.format2(Objects.requireNonNull(player.getAttribute(MiaAttributes.BLOCK_HARDNESS)).getValue());

        cir.setReturnValue(originalValue * (1 / destroySpeed));
    }*/
}
