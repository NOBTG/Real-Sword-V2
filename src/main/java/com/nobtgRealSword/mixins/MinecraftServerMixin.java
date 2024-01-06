package com.nobtgRealSword.mixins;

import com.nobtgRealSword.utils.RealProfiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.SayCommand;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class, priority = Integer.MAX_VALUE)
public abstract class MinecraftServerMixin {
    @Shadow
    public abstract ProfilerFiller getProfiler();

    @Shadow
    private ProfilerFiller profiler;

    @Unique
    private static final ScheduledExecutorService realSword$service = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tickServer(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        if (!(this.getProfiler() instanceof RealProfiler)) this.profiler = new RealProfiler();
    }
}
