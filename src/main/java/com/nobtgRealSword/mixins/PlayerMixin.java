package com.nobtgRealSword.mixins;

import com.nobtgRealSword.utils.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = Integer.MAX_VALUE)
public abstract class PlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Entity pTarget, CallbackInfo ci) {
        if (!EntityUtil.attack((Player) (Object) this, pTarget)) ci.cancel();
    }
}
