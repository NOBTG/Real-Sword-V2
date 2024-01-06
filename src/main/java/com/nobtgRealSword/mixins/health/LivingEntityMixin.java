package com.nobtgRealSword.mixins.health;

import com.nobtgRealSword.utils.CoreMethod;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = Integer.MAX_VALUE)
public abstract class LivingEntityMixin {
    @Inject(method = "getHealth", at = @At("RETURN"), cancellable = true)
    private void getHealth(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(CoreMethod.getHealth((LivingEntity) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
    private void getMaxHealth(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(CoreMethod.getHealth((LivingEntity) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "isAlive", at = @At("RETURN"), cancellable = true)
    private void isAlive(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CoreMethod.isAlive((LivingEntity) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "isDeadOrDying", at = @At("RETURN"), cancellable = true)
    private void isDeadOrDying(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CoreMethod.isDeadOrDying((LivingEntity) (Object) this, cir.getReturnValue()));
    }
}
