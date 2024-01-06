package com.nobtgRealSword.utils.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import com.nobtgCore.Helper;
import com.nobtgRealSword.utils.RealGui;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public final class RealGameRenderer extends GameRenderer {
    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    private RealGui gui;

    public RealGameRenderer(Minecraft pMinecraft, ItemInHandRenderer pItemInHandRenderer, ResourceManager pResourceManager, RenderBuffers pRenderBuffers) {
        super(pMinecraft, pItemInHandRenderer, pResourceManager, pRenderBuffers);
    }

    @Override
    public void render(float pPartialTicks, long pNanoTime, boolean pRenderLevel) {
        if (!this.getMinecraft().isWindowActive() && this.getMinecraft().options.pauseOnLostFocus && (!this.getMinecraft().options.touchscreen().get() || !this.getMinecraft().mouseHandler.isRightPressed())) {
            if (Util.getMillis() - this.lastActiveTime > 500L)
                this.getMinecraft().pauseGame(false);
        } else this.lastActiveTime = Util.getMillis();
        double[] cursorPosX = new double[1];
        double[] cursorPosY = new double[1];
        GLFW.glfwGetCursorPos(this.getMinecraft().getWindow().getWindow(), cursorPosX, cursorPosY);
        int i = (int)(cursorPosX[0] * this.getMinecraft().getWindow().getGuiScaledWidth() / this.getMinecraft().getWindow().getScreenWidth());
        int j = (int)(cursorPosY[0] * this.getMinecraft().getWindow().getGuiScaledHeight() / this.getMinecraft().getWindow().getScreenHeight());
        RenderSystem.viewport(0, 0, this.getMinecraft().getWindow().getWidth(), this.getMinecraft().getWindow().getHeight());
        if (pRenderLevel && this.getMinecraft().level != null) {
            this.getMinecraft().getProfiler().push("level");
            this.renderLevel(pPartialTicks, pNanoTime, new PoseStack());
            this.tryTakeScreenshotIfNeeded();
            this.getMinecraft().levelRenderer.doEntityOutline();
            if (this.postEffect != null && this.effectActive) {
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.resetTextureMatrix();
                this.postEffect.process(pPartialTicks);
            }

            this.getMinecraft().getMainRenderTarget().bindWrite(true);
        }

        Window window = this.getMinecraft().getWindow();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()), (float) ((double) window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, ForgeHooksClient.getGuiFarPlane());
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, 1000F - ForgeHooksClient.getGuiFarPlane());
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        GuiGraphics guigraphics = new GuiGraphics(this.getMinecraft(), this.renderBuffers.bufferSource());

        if (pRenderLevel && this.getMinecraft().level != null) {
            this.getMinecraft().getProfiler().popPush("gui");
            if (this.getMinecraft().player != null) {
                float f = Mth.lerp(pPartialTicks, this.getMinecraft().player.oSpinningEffectIntensity, this.getMinecraft().player.spinningEffectIntensity);
                float f1 = this.getMinecraft().options.screenEffectScale().get().floatValue();
                if (f > 0.0F && this.getMinecraft().player.hasEffect(MobEffects.CONFUSION) && f1 < 1.0F) {
                    this.renderConfusionOverlay(guigraphics, f * (1.0F - f1));
                }
            }

            if (!this.getMinecraft().options.hideGui || this.getMinecraft().screen != null) {
                this.renderItemActivationAnimation(this.getMinecraft().getWindow().getGuiScaledWidth(), this.getMinecraft().getWindow().getGuiScaledHeight(), pPartialTicks);
                if (!RenderUtil.isDefense(this.getMinecraft())) {
                    this.getMinecraft().gui.render(guigraphics, pPartialTicks);
                } else gui.render(guigraphics, pPartialTicks);
                RenderSystem.clear(256, Minecraft.ON_OSX);
            }

            this.getMinecraft().getProfiler().pop();
        }
        if (!RenderUtil.isDefense(this.getMinecraft())) {
            if (this.getMinecraft().getOverlay() != null) {
                try {
                    this.getMinecraft().getOverlay().render(guigraphics, i, j, this.getMinecraft().getDeltaFrameTime());
                } catch (Throwable throwable2) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable2, "Rendering overlay");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Overlay render details");
                    crashreportcategory.setDetail("Overlay name", () -> this.getMinecraft().getOverlay().getClass().getCanonicalName());
                    throw new ReportedException(crashreport);
                }
            } else if (this.getMinecraft().screen != null) {
                try {
                    ForgeHooksClient.drawScreen(this.getMinecraft().screen, guigraphics, i, j, this.getMinecraft().getDeltaFrameTime());
                } catch (Throwable throwable1) {
                    CrashReport crashreport1 = CrashReport.forThrowable(throwable1, "Rendering screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen render details");
                    crashreportcategory1.setDetail("Screen name", () -> this.getMinecraft().screen.getClass().getCanonicalName());
                    crashreportcategory1.setDetail("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.getMinecraft().mouseHandler.xpos(), this.getMinecraft().mouseHandler.ypos()));
                    crashreportcategory1.setDetail("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.getMinecraft().getWindow().getGuiScaledWidth(), this.getMinecraft().getWindow().getGuiScaledHeight(), this.getMinecraft().getWindow().getWidth(), this.getMinecraft().getWindow().getHeight(), this.getMinecraft().getWindow().getGuiScale()));
                    throw new ReportedException(crashreport1);
                }

                try {
                    if (this.getMinecraft().screen != null)
                        this.getMinecraft().screen.handleDelayedNarration();
                } catch (Throwable throwable) {
                    CrashReport crashreport2 = CrashReport.forThrowable(throwable, "Narrating screen");
                    CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Screen details");
                    crashreportcategory2.setDetail("Screen name", () -> this.getMinecraft().screen.getClass().getCanonicalName());
                    throw new ReportedException(crashreport2);
                }
            }

            this.getMinecraft().getProfiler().push("toasts");
            this.getMinecraft().getToasts().render(guigraphics);
            this.getMinecraft().getProfiler().pop();
        }
        guigraphics.flush();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void overrideGameRenderer(Minecraft mc) {
        RealGameRenderer gameRenderer = new RealGameRenderer(mc, mc.getEntityRenderDispatcher().getItemInHandRenderer(), mc.getResourceManager(), mc.renderBuffers());
        ReloadableResourceManager manager = (ReloadableResourceManager) mc.getResourceManager();
        manager.registerReloadListener(gameRenderer.createReloadListener());

        Helper.copyProperties(GameRenderer.class, mc.gameRenderer, gameRenderer);

        RealGui realGui = new RealGui(mc, mc.getItemRenderer());
        Helper.copyProperties(Gui.class, mc.gui, realGui);

        gameRenderer.gui = realGui;
        mc.gameRenderer = gameRenderer;
    }

    private void renderConfusionOverlay(GuiGraphics p_282460_, float p_282656_) {
        int i = p_282460_.guiWidth();
        int j = p_282460_.guiHeight();
        p_282460_.pose().pushPose();
        float f = Mth.lerp(p_282656_, 2.0F, 1.0F);
        p_282460_.pose().translate((float) i / 2.0F, (float) j / 2.0F, 0.0F);
        p_282460_.pose().scale(f, f, f);
        p_282460_.pose().translate((float) (-i) / 2.0F, (float) (-j) / 2.0F, 0.0F);
        float f1 = 0.2F * p_282656_;
        float f2 = 0.4F * p_282656_;
        float f3 = 0.2F * p_282656_;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        p_282460_.setColor(f1, f2, f3, 1.0F);
        p_282460_.blit(NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
        p_282460_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        p_282460_.pose().popPose();
    }

    private void renderItemActivationAnimation(int p_109101_, int p_109102_, float p_109103_) {
        if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
            int i = 40 - this.itemActivationTicks;
            float f = ((float) i + p_109103_) / 40.0F;
            float f1 = f * f;
            float f2 = f * f1;
            float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
            float f4 = f3 * (float) Math.PI;
            float f5 = this.itemActivationOffX * (float) (p_109101_ / 4);
            float f6 = this.itemActivationOffY * (float) (p_109102_ / 4);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            posestack.translate((float) (p_109101_ / 2) + f5 * Mth.abs(Mth.sin(f4 * 2.0F)), (float) (p_109102_ / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F)), -50.0F);
            float f7 = 50.0F + 175.0F * Mth.sin(f4);
            posestack.scale(f7, -f7, f7);
            posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f4))));
            posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
            this.getMinecraft().getItemRenderer().renderStatic(this.itemActivationItem, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, multibuffersource$buffersource, this.getMinecraft().level, 0);
            posestack.popPose();
            multibuffersource$buffersource.endBatch();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }
    }

    private void tryTakeScreenshotIfNeeded() {
        if (!this.hasWorldScreenshot && this.getMinecraft().isLocalServer()) {
            long i = Util.getMillis();
            if (i - this.lastScreenshotAttempt >= 1000L) {
                this.lastScreenshotAttempt = i;
                IntegratedServer integratedserver = this.getMinecraft().getSingleplayerServer();
                if (integratedserver != null && !integratedserver.isStopped()) {
                    integratedserver.getWorldScreenshotFile().ifPresent((p_234239_) -> {
                        if (Files.isRegularFile(p_234239_)) {
                            this.hasWorldScreenshot = true;
                        } else this.takeAutoScreenshot(p_234239_);
                    });
                }
            }
        }
    }

    private void takeAutoScreenshot(Path p_182643_) {
        if (this.getMinecraft().levelRenderer.countRenderedChunks() > 10 && this.getMinecraft().levelRenderer.hasRenderedAllChunks()) {
            NativeImage nativeimage = Screenshot.takeScreenshot(this.getMinecraft().getMainRenderTarget());
            Util.ioPool().execute(() -> {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }
                try (NativeImage nativeimage1 = new NativeImage(64, 64, false)) {
                    nativeimage.resizeSubRectTo(k, l, i, j, nativeimage1);
                    nativeimage1.writeToFile(p_182643_);
                } catch (IOException ioexception) {
                    LOGGER.warn("Couldn't save auto screenshot", ioexception);
                } finally {
                    nativeimage.close();
                }
            });
        }
    }
}
