package com.nobtgRealSword.mixins;

import com.nobtgRealSword.RealSwordMod;
import com.nobtgRealSword.utils.EntityUtil;
import com.nobtgRealSword.utils.client.RealGameRenderer;
import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = Minecraft.class, priority = Integer.MAX_VALUE)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    public GameRenderer gameRenderer;
    @Shadow
    @Nullable
    public ClientLevel level;
    @Unique
    private boolean realSword$onDefense = false;
    @Unique
    private boolean realSword$kill = false;
    @Unique
    private GameRenderer realSword$oldGameRender;

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onEmptyLeftClick(Lnet/minecraft/world/entity/player/Player;)V"))
    private void startAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && this.player.isShiftKeyDown() && EntityUtil.isReal(this.player) && this.player.getMainHandItem().getItem().equals(RealSwordMod.realSword.get())) {
            realSword$onDefense = !realSword$onDefense;
            if (realSword$onDefense) {
                realSword$oldGameRender = this.gameRenderer;
                RealGameRenderer.overrideGameRenderer((Minecraft) (Object) this);
            } else this.gameRenderer = this.realSword$oldGameRender;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreen(Screen pGuiScreen, CallbackInfo ci) {
        if (realSword$kill && !(pGuiScreen instanceof ConfirmScreen)) ci.cancel();
    }
}
