package com.nobtgRealSword.mixins.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = Integer.MAX_VALUE)
public abstract class GameRendererMixin {
    @Shadow
    public abstract Minecraft getMinecraft();

    @Unique
    private GuiGraphics realSword$guiGraphics;

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"), name = "guigraphics")
    private GuiGraphics render(GuiGraphics value) {
        this.realSword$guiGraphics = value;
        return value;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"))
    private void render(float p_109094_, long p_109095_, boolean p_109096_, CallbackInfo ci) {
        if (RenderUtil.isKill(this.getMinecraft())) {
            int cX = (int) (this.getMinecraft().mouseHandler.xpos() * (double) this.getMinecraft().getWindow().getGuiScaledWidth() / (double) this.getMinecraft().getWindow().getScreenWidth());
            int cY = (int) (this.getMinecraft().mouseHandler.ypos() * (double) this.getMinecraft().getWindow().getGuiScaledHeight() / (double) this.getMinecraft().getWindow().getScreenHeight());

            realSword$guiGraphics.fillGradient(RenderType.gui(), 0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 1615855616, -1602211792, 0);
            realSword$guiGraphics.pose().pushPose();
            realSword$guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
            realSword$guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("deathScreen.title"), Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 / 2, 30, 16777215);
            realSword$guiGraphics.pose().popPose();
            realSword$guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("deathScreen.score").append(": ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)), Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2, 100, 16777215);

            {
                int x = this.getMinecraft().getWindow().getGuiScaledWidth() / 2 - 100;
                int y = this.getMinecraft().getWindow().getGuiScaledHeight() / 4 + 72;
                boolean isHovered = cX >= x && cY >= y && cX < x + 200 && cY < y + 20;

                realSword$guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                realSword$guiGraphics.blitNineSliced(AbstractWidget.WIDGETS_LOCATION, Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 72, 200, 20, 20, 4, 200, 20, 0, (46 + (isHovered ? 2 : 1) * 20));
                realSword$guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                int i = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100) + 2;
                int j = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100) + 200 - 2;
                RenderUtil.renderScrollingString(realSword$guiGraphics, this.getMinecraft().font, this.getMinecraft().level.getLevelData().isHardcore() ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn"), i, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 72, j, (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 72) + 20, 16777215 | Mth.ceil(255.0F) << 24);
            }

            {
                int x = this.getMinecraft().getWindow().getGuiScaledWidth() / 2 - 100;
                int y = this.getMinecraft().getWindow().getGuiScaledHeight() / 4 + 96;
                boolean isHovered = cX >= x && cY >= y && cX < x + 200 && cY < y + 20;

                realSword$guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                realSword$guiGraphics.blitNineSliced(AbstractWidget.WIDGETS_LOCATION, Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 96, 200, 20, 20, 4, 200, 20, 0, 46 + (isHovered ? 2 : 1) * 20);
                realSword$guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

                int i = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100) + 2;
                int j = (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100) + 200 - 2;
                RenderUtil.renderScrollingString(realSword$guiGraphics, this.getMinecraft().font, Component.translatable("deathScreen.titleScreen"), i, Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 96, j, (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 96) + 20, 16777215 | Mth.ceil(255.0F) << 24);
            }
            
            if (this.getMinecraft().getReportingContext().hasDraftReport()) {
                realSword$guiGraphics.blit(AbstractWidget.WIDGETS_LOCATION, (Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - 100) + 200 - 17, (Minecraft.getInstance().getWindow().getGuiScaledHeight() / 4 + 96) + 3, 182, 24, 15, 15);
            }

            RenderUtil.releaseMouse(this.getMinecraft());
        }
    }
}
