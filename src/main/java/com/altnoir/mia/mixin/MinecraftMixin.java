package com.altnoir.mia.mixin;

import com.altnoir.mia.content.items.ClockItem;
import com.altnoir.mia.Time;
import com.altnoir.mia.content.RenderEvent;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.TimerQuery;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Locale;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public static int fps;
    @Shadow
    public int rightClickDelay;
    @Shadow
    public ProfilerFiller profiler;
    @Shadow
    public Gui gui;
    @Shadow
    public volatile boolean pause;
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    public int missTime;

    @Shadow
    @Nullable
    public Overlay overlay;
    @Shadow
    public GameRenderer gameRenderer;
    @Mutable
    @Shadow
    public Timer timer;
    @Shadow
    public float pausePartialTick;
    @Shadow
    @Nullable
    public IntegratedServer singleplayerServer;
    @Shadow
    public float realPartialTick;
    @Shadow
    public ParticleEngine particleEngine;
    @Shadow
    public Window window;
    @Shadow
    public int frames;
    @Shadow
    public long lastNanoTime;
    @Shadow
    public Options options;
    @Shadow
    public MetricsRecorder metricsRecorder;
    @Shadow
    public double gpuUtilization;
    @Shadow
    @Nullable
    public TimerQuery.FrameProfile currentFrameProfile;
    @Shadow
    public long savedCpuDuration;
    @Shadow
    public FrameTimer frameTimer;
    @Shadow
    public long lastTime;
    @Shadow
    public String fpsString;

    @Shadow
    public abstract void setScreen(@org.jetbrains.annotations.Nullable Screen p_91153_);

    @Shadow
    protected abstract void handleKeybinds();

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract boolean hasSingleplayerServer();

    @Shadow
    protected abstract int getFramerateLimit();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        if (Time.get()) {
            pause = true;
            ClockItem.stopping_time++;
            if (this.rightClickDelay > 0) {
                --this.rightClickDelay;
            }
            this.profiler.push("gui");
            this.profiler.pop();
            if (this.screen == null && this.player != null) {
                if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
                    this.setScreen(null);
                } else if (this.player.isSleeping() && this.level != null) {
                    this.setScreen(new InBedChatScreen());
                }
            } else {
                Screen $$4 = this.screen;
                if ($$4 instanceof InBedChatScreen inbedchatscreen) {
                    if (this.player != null && !this.player.isSleeping()) {
                        inbedchatscreen.onPlayerWokeUp();
                    }
                }
            }

            if (this.screen != null) {
                missTime = 10000;
            }

            if (this.screen != null) {
                Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
            }

            if (this.overlay == null && this.screen == null) {
                this.handleKeybinds();
                if (this.missTime > 0) {
                    --this.missTime;
                }
            }
            if (level != null) {
                level.tickingEntities.forEach((s) -> {
                    if (!s.isRemoved() && !s.isPassenger()) {
                        if (s instanceof Player && s != player)
                            level.guardEntityTick(level::tickNonPassenger, s);
                        if (s.tickCount < 1)
                            level.guardEntityTick(level::tickNonPassenger, s);

                    }
                });
            }
            realPartialTick = pausePartialTick;
            ci.cancel();
        } else {
            ClockItem.stopping_time = 0;
            timer.msPerTick = 50;
        }
    }


    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"))
    public void runTick(boolean p_91384_, CallbackInfo ci) {
        for (int i = 0; i < Time.timer.advanceTime(Util.getMillis()); i++)
            if (/*Time.times % 2 == 0 && */level != null && player != null && Time.get()) {
                level.guardEntityTick(level::tickNonPassenger, player);
                this.gui.tick(this.pause);
                gameRenderer.itemInHandRenderer.tick();
                this.gameRenderer.tick();
            }
    }


    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    public void postEvent(boolean p_91384_, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new RenderEvent(TickEvent.Phase.START, realPartialTick));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setErrorSection(Ljava/lang/String;)V", ordinal = 2), cancellable = true)
    public void pauseGame(boolean p_91384_, CallbackInfo ci) {
        if (Time.get()) {
            this.window.setErrorSection("Post render");
            ++frames;
            boolean flag;
            if (!options.renderDebug && !this.metricsRecorder.isRecording()) {
                flag = false;
                this.gpuUtilization = 0.0D;
            } else {
                flag = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
                if (flag) {
                    TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
                }
            }
            long l = Util.getNanos();
            long i1 = l - lastNanoTime;
            if (flag) {
                this.savedCpuDuration = i1;
            }

            this.frameTimer.logFrameDuration(i1);
            this.lastNanoTime = l;
            this.profiler.push("fpsUpdate");
            if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
                this.gpuUtilization = (double) this.currentFrameProfile.get() * 100.0D / (double) this.savedCpuDuration;
            }

            while (Util.getMillis() >= this.lastTime + 1000L) {
                String s;
                if (this.gpuUtilization > 0.0D) {
                    s = " GPU: " + (this.gpuUtilization > 100.0D ? ChatFormatting.RED + "100%" : Math.round(this.gpuUtilization) + "%");
                } else {
                    s = "";
                }
                int k1 = this.getFramerateLimit();
                fps = this.frames;
                this.fpsString = String.format(Locale.ROOT, "%d fps T: %s%s%s%s B: %d%s", fps, k1 == 260 ? "inf" : k1, this.options.enableVsync().get() ? " vsync" : "", this.options.graphicsMode().get(), this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"), this.options.biomeBlendRadius().get(), s);
                this.lastTime += 1000L;
                this.frames = 0;
            }

            this.profiler.pop();
            ci.cancel();
        }
    }
}