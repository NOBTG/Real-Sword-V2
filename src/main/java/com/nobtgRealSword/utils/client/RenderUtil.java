package com.nobtgRealSword.utils.client;

import com.mojang.blaze3d.platform.Window;
import com.nobtgCore.Helper;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.SharedLibrary;

import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
public final class RenderUtil {
    private static SharedLibrary GLFW;
    public static final int CURSOR = 208897, CURSOR_DISABLED = 212995, CURSOR_NORMAL = 212993;
    public static long SetCursorPos, SetInputMode;

    static {
        SharedLibrary GLFW = Helper.getFieldValue(GLFW.class, "GLFW", SharedLibrary.class);
        assert GLFW != null;
        RenderUtil.GLFW = GLFW;

        RenderUtil.SetCursorPos = RenderUtil.GLFW.getFunctionAddress("glfwSetCursorPos");
        RenderUtil.SetInputMode = RenderUtil.GLFW.getFunctionAddress("glfwSetInputMode");
    }

    public static void grabMouse(Minecraft mc) {
        if (mc.isWindowActive()) {
            MouseHandler handler = mc.mouseHandler;
            Window window = mc.getWindow();
            if (!Minecraft.ON_OSX)
                KeyMapping.setAll();
            handler.mouseGrabbed = true;
            RenderUtil.grabOrReleaseMouse(window.getWindow(), CURSOR_DISABLED);
        }
    }

    public static void releaseMouse(Minecraft mc) {
        MouseHandler handler = mc.mouseHandler;
        Window window = mc.getWindow();
        handler.mouseGrabbed = false;
        RenderUtil.grabOrReleaseMouse(window.getWindow(), CURSOR_NORMAL);
    }

    public static void grabOrReleaseMouse(@NativeType("GLFWwindow *") long window, int value) {
        JNI.invokePV(window, CURSOR, value, RenderUtil.SetInputMode);
    }

    public static void setCursorPos(Minecraft mc) {
        MouseHandler handler = mc.mouseHandler;
        Window window = mc.getWindow();
        handler.xpos = (double) window.getScreenWidth() / 2;
        handler.ypos = (double) window.getScreenHeight() / 2;
        JNI.invokePV(window.getWindow(), handler.xpos, handler.ypos, RenderUtil.SetCursorPos);
    }

    public static boolean isDefense(Minecraft mc) {
        try {
            Field field = Minecraft.class.getDeclaredField("realSword$onDefense");
            field.setAccessible(true);
            Boolean returnVal = (Boolean) field.get(mc);
            return returnVal != null && returnVal;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isKill(Minecraft mc) {
        try {
            Field field = Minecraft.class.getDeclaredField("realSword$kill");
            field.setAccessible(true);
            Boolean returnVal = (Boolean) field.get(mc);
            return returnVal != null && returnVal;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setKill(Minecraft mc, boolean val) {
        try {
            Field field = Minecraft.class.getDeclaredField("realSword$kill");
            field.setAccessible(true);
            field.set(mc, val);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createTitle(Minecraft minecraft, boolean isDead) {
        StringBuilder stringbuilder = new StringBuilder("Minecraft");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringbuilder.append(' ').append(net.minecraftforge.forge.snapshots.ForgeSnapshotsMod.BRANDING_NAME).append('*');
        }

        stringbuilder.append(" ");
        stringbuilder.append(SharedConstants.getCurrentVersion().getName());
        ClientPacketListener clientpacketlistener = minecraft.getConnection();
        if (clientpacketlistener != null && clientpacketlistener.getConnection().isConnected()) {
            stringbuilder.append(" - ");
            if (minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished()) {
                stringbuilder.append(I18n.get("title.singleplayer"));
            } else if (minecraft.isConnectedToRealms()) {
                stringbuilder.append(I18n.get("title.multiplayer.realms"));
            } else if (minecraft.getSingleplayerServer() == null && (minecraft.getCurrentServer() == null || !minecraft.getCurrentServer().isLan())) {
                stringbuilder.append(I18n.get("title.multiplayer.other"));
            } else {
                stringbuilder.append(I18n.get("title.multiplayer.lan"));
            }
        }

        return stringbuilder + (isDead ? " | You have fallen from God" : "");
    }

    public static void renderScrollingString(GuiGraphics p_281620_, Font p_282651_, Component p_281467_, int p_283621_, int p_282084_, int p_283398_, int p_281938_, int p_283471_) {
        int i = p_282651_.width(p_281467_);
        int j = (p_282084_ + p_281938_ - 9) / 2 + 1;
        int k = p_283398_ - p_283621_;
        if (i > k) {
            int l = i - k;
            double d0 = (double) Util.getMillis() / 1000.0D;
            double d1 = Math.max((double)l * 0.5D, 3.0D);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / 2.0D + 0.5D;
            double d3 = Mth.lerp(d2, 0.0D, (double)l);
            p_281620_.enableScissor(p_283621_, p_282084_, p_283398_, p_281938_);
            p_281620_.drawString(p_282651_, p_281467_, p_283621_ - (int)d3, j, p_283471_);
            p_281620_.disableScissor();
        } else {
            p_281620_.drawCenteredString(p_282651_, p_281467_, (p_283621_ + p_283398_) / 2, j, p_283471_);
        }
    }
}
