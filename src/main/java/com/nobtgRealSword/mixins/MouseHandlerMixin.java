package com.nobtgRealSword.mixins;

import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHandler.class, priority = Integer.MAX_VALUE)
public abstract class MouseHandlerMixin {
    @Shadow
    public double xpos;

    @Shadow
    public double ypos;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;", ordinal = 0))
    private void onPress(long p_91531_, int p_91532_, int p_91533_, int p_91534_, CallbackInfo ci) {
        if (!RenderUtil.isKill(this.minecraft)) return;
        double d0 = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double d1 = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
        {
            int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 100;
            int y = this.minecraft.getWindow().getGuiScaledHeight() / 4 + 72;
            boolean isHovered = d0 >= x && d1 >= y && d0 < x + 200 && d1 < y + 20;
            if (isHovered) {
                RenderUtil.setKill(this.minecraft, false);
            }
        }

        {
            int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 100;
            int y = this.minecraft.getWindow().getGuiScaledHeight() / 4 + 96;
            boolean isHovered = d0 >= x && d1 >= y && d0 < x + 200 && d1 < y + 20;
            if (isHovered) {
                if (this.minecraft.level.getLevelData().isHardcore()) {
                    if (this.minecraft.level != null) {
                        this.minecraft.level.disconnect();
                    }

                    this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
                    this.minecraft.setScreen(new TitleScreen());
                } else {
                    RenderUtil.setKill(this.minecraft, false);
                    ConfirmScreen confirmscreen = new DeathScreen.TitleConfirmScreen((p_280795_) -> {
                        if (p_280795_) {
                            if (this.minecraft.level != null) {
                                this.minecraft.level.disconnect();
                            }

                            this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
                            this.minecraft.setScreen(new TitleScreen());
                        } else {
                            this.minecraft.player.respawn();
                            this.minecraft.setScreen(null);
                        }

                    }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
                    this.minecraft.setScreen(confirmscreen);
                    confirmscreen.setDelay(20);
                }
            }
        }
    }
}
