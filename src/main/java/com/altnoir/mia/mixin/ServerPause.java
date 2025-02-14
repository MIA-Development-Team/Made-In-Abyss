package com.altnoir.mia.mixin;

import com.altnoir.mia.Time;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;


@Mixin(IntegratedServer.class)
public class ServerPause {
    @Shadow
    private boolean paused;

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;"))
    public void tick(BooleanSupplier p_120049_, CallbackInfo ci) {
        if (Time.get())
            paused = false;
    }
}