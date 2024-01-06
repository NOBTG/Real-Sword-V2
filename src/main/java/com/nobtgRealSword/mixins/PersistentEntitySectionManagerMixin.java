package com.nobtgRealSword.mixins;

import com.nobtgRealSword.utils.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PersistentEntitySectionManager.class, priority = Integer.MAX_VALUE)
public abstract class PersistentEntitySectionManagerMixin {
    @Inject(method = "addEntityWithoutEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void addEntityWithoutEvent(EntityAccess pEntity, boolean pWorldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (pEntity instanceof Entity entity)
            if (EntityUtil.isBan(entity))
                cir.setReturnValue(false);
    }

    @Inject(method = "addEntityUuid", at = @At("HEAD"), cancellable = true)
    private void addEntityUuid(EntityAccess pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (pEntity instanceof Entity entity)
            if (EntityUtil.isBan(entity))
                cir.setReturnValue(false);
    }

    @Inject(method = "startTracking", at = @At("HEAD"), cancellable = true)
    private void startTracking(EntityAccess p_157576_, CallbackInfo ci) {
        if (p_157576_ instanceof Entity entity)
            if (EntityUtil.isBan(entity))
                ci.cancel();
    }

    @Inject(method = "startTicking", at = @At("HEAD"), cancellable = true)
    private void startTicking(EntityAccess p_157565_, CallbackInfo ci) {
        if (p_157565_ instanceof Entity entity)
            if (EntityUtil.isBan(entity))
                ci.cancel();
    }

    @Inject(method = "stopTicking", at = @At("HEAD"), cancellable = true)
    private void stopTicking(EntityAccess p_157565_, CallbackInfo ci) {
        if (p_157565_ instanceof Entity entity)
            if (EntityUtil.isReal(entity))
                ci.cancel();
    }

    @Inject(method = "stopTracking", at = @At("HEAD"), cancellable = true)
    private void stopTracking(EntityAccess p_157565_, CallbackInfo ci) {
        if (p_157565_ instanceof Entity entity)
            if (EntityUtil.isReal(entity))
                ci.cancel();
    }
}
