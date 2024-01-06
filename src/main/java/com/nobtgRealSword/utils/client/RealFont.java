package com.nobtgRealSword.utils.client;

import com.nobtgRealSword.SuperRealDeadItem;
import com.nobtgRealSword.utils.enums.RenderStatus;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class RealFont extends Font {
    public static float tick = 0.0F;
    private final RenderStatus status;

    private RealFont(Function<ResourceLocation, FontSet> pFonts, boolean pFilterFishyGlyphs, RenderStatus status) {
        super(pFonts, pFilterFishyGlyphs);
        this.status = status;
    }

    public static RealFont getInstance(FontManager manger, boolean filterFishyGlyphs, RenderStatus status) {
        return new RealFont(location -> manger.fontSets.getOrDefault(manger.renames.getOrDefault(location, location), manger.missingFontSet), filterFishyGlyphs, status);
    }

    @Override
    public int drawInBatch(String p_272751_, float p_272661_, float p_273129_, int p_273272_, boolean p_273209_, Matrix4f p_272940_, MultiBufferSource p_273017_, DisplayMode p_272608_, int p_273365_, int p_272755_) {
        return this.rainbowDraw(p_272751_, p_272661_, p_273129_, p_273209_, p_272940_, p_273017_, p_272608_, p_273365_, p_272755_, this.isBidirectional());
    }

    @Override
    public int drawInBatch(Component p_273032_, float p_273249_, float p_273594_, int p_273714_, boolean p_273050_, Matrix4f p_272974_, MultiBufferSource p_273695_, DisplayMode p_272782_, int p_272603_, int p_273632_) {
        return this.rainbowDraw(p_273032_.getString(), p_273249_, p_273594_, p_273050_, p_272974_, p_273695_, p_272782_, p_272603_, p_273632_, this.isBidirectional());
    }

    @Override
    public int drawInBatch(FormattedCharSequence p_273262_, float x, float y, int color, boolean b, Matrix4f matrix4f, MultiBufferSource source, DisplayMode mode, int i, int i1) {
        StringBuilder builder = new StringBuilder();
        p_273262_.accept((p_13746_, p_13747_, p_13748_) -> {
            builder.appendCodePoint(p_13748_);
            return true;
        });
        return this.rainbowDraw(builder.toString(), x, y, b, matrix4f, source, mode, i, i1, this.isBidirectional());
    }

    @Override
    public int drawInBatch(String p_272780_, float p_272811_, float p_272610_, int p_273422_, boolean p_273016_, Matrix4f p_273443_, MultiBufferSource p_273387_, DisplayMode p_273551_, int p_272706_, int p_273114_, boolean p_273022_) {
        return this.rainbowDraw(p_272780_, p_272811_, p_272610_, p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
    }

    public int rainbowDraw(String text, float x, float y, boolean p_273016_, Matrix4f p_273443_, MultiBufferSource p_273387_, DisplayMode p_273551_, int p_272706_, int p_273114_, boolean p_273022_) {
        if (this.status.equals(RenderStatus.normal) && text.equals(Component.translatable("item.real_sword.real_sword").getString())) {
            for (int index = 0; index < text.length(); index++) {
                String s = String.valueOf(text.charAt(index));
                super.drawInternal(s, x, y, calculateColor(index), p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
                x += this.width(s);
            }
        } else if (this.status.equals(RenderStatus.normal) && text.equals(Component.translatable("item.modifiers.mainhand").getString())) {
            for (int index = 0; index < text.length(); index++) {
                String s = String.valueOf(text.charAt(index));
                super.drawInternal(s, x, y, ChatFormatting.GRAY.getColor(), p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
                x += this.width(s);
            }
        } else if (this.status.equals(RenderStatus.normal) && (text.contains(Component.translatable("attribute.name.generic.attack_damage").getString()) || text.contains(Component.translatable("attribute.name.generic.attack_speed").getString()))) {
            for (int index = 0; index < text.length(); index++) {
                String s = String.valueOf(text.charAt(index));
                super.drawInternal(s, x, y, ChatFormatting.DARK_GREEN.getColor(), p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
                x += this.width(s);
            }
        } else if (this.status.equals(RenderStatus.renderGod) && text.equals(Component.translatable("item.real_sword.super_real_dead").getString())) {
            float huehuehue = (float) Util.getMillis() / 80L / 32.0F;
            float huehuehueStep = 0.03125F;
            for (int index = 0; index < text.length(); index++) {
                String s = String.valueOf(text.charAt(index));
                int c = Mth.hsvToRgb(huehuehue, 1.0F, 1.0F);
                float yOffset = (float) Math.sin((float) index + (float) Util.getMillis() / 300.0F);
                super.drawInternal(s, x, y + yOffset, c, p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
                huehuehue += huehuehueStep;
                huehuehue %= 1.0F;
                x += this.width(s);
            }
        } else if (this.status.equals(RenderStatus.renderGod)) {
            super.drawInternal(text, x, y, calculateColor(SuperRealDeadItem.list.indexOf(text) * 2), p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
        }
        return (int) x;
    }

    public static int calculateColor(int index) {
        float adjustedValue = (RealFont.tick + index) % 720.0f >= 360.0f
                ? 720.0f - (RealFont.tick + index) % 720.0f
                : (RealFont.tick + index) % 720.0f;

        float hue = adjustedValue / 100.0f;

        return Color.HSBtoRGB(hue, 0.8f, 0.8f);
    }
}
