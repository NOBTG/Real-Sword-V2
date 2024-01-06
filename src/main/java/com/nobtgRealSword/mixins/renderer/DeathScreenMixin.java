package com.nobtgRealSword.mixins.renderer;

import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeathScreen.class, priority = Integer.MAX_VALUE)
public abstract class DeathScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(GuiGraphics p_283488_, int p_283551_, int p_283002_, float p_281981_, CallbackInfo ci) {
        if (RenderUtil.isDefense(Minecraft.getInstance())) ci.cancel();
    }
}
