package com.altnoir.mia.mixin;

import com.altnoir.mia.Time;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerMixin extends Level {
    @Shadow
    @Final
    private ServerChunkCache chunkSource;

    protected ServerMixin(WritableLevelData p_270739_, ResourceKey<Level> p_270683_, RegistryAccess p_270200_, Holder<DimensionType> p_270240_, Supplier<ProfilerFiller> p_270692_, boolean p_270904_, boolean p_270470_, long p_270248_, int p_270466_) {
        super(p_270739_, p_270683_, p_270200_, p_270240_, p_270692_, p_270904_, p_270470_, p_270248_, p_270466_);
    }

    @Override
    public <T extends Entity> void guardEntityTick(Consumer<T> p_46654_, T p_46655_) {
        if (Time.get()) {
            if (!(p_46655_ instanceof Player) && p_46655_.tickCount > 0)
                return;
            chunkSource.tick(() -> true, true);
        }
        super.guardEntityTick(p_46654_, p_46655_);
    }

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    public void tickTime(CallbackInfo ci) {
        if (Time.get())
            ci.cancel();
    }

    @Inject(method = "tickBlock", at = @At("HEAD"), cancellable = true)
    public void tickBlock(BlockPos p_184113_, Block p_184114_, CallbackInfo ci) {
        if (Time.get())
            ci.cancel();
    }
}