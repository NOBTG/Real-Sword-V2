package com.nobtgRealSword.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public final class CoreMethod {
    public static float getHealth(LivingEntity living) {
        return switch (EntityUtil.getCategory(living)) {
            case real -> 20.0F;
            case unreal, extremelyUnreal -> 0.0F;
            case normal -> living.getHealth();
        };
    }

    public static int getDeathTime(LivingEntity living) {
        return switch (EntityUtil.getCategory(living)) {
            case real -> 0;
            case unreal, normal, extremelyUnreal -> living.deathTime;
        };
    }

    public static int getHurtTime(LivingEntity living) {
        return switch (EntityUtil.getCategory(living)) {
            case real -> 0;
            case unreal, normal, extremelyUnreal -> living.hurtTime;
        };
    }

    public static int getHurtDuration(LivingEntity living) {
        return switch (EntityUtil.getCategory(living)) {
            case real -> 0;
            case unreal, normal, extremelyUnreal -> living.hurtDuration;
        };
    }

    public static Entity.RemovalReason getRemovalReason(Entity entity) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> null;
            case unreal, extremelyUnreal -> Entity.RemovalReason.KILLED;
            case normal -> entity.removalReason;
        };
    }

    public static boolean isRemoved(Entity entity) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> false;
            case unreal, extremelyUnreal -> true;
            case normal -> entity.isRemoved();
        };
    }

    public static boolean isAlive(Entity entity) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> true;
            case unreal, extremelyUnreal -> false;
            case normal -> entity.isAlive();
        };
    }

    public static boolean isDeadOrDying(LivingEntity living) {
        return switch (EntityUtil.getCategory(living)) {
            case real -> false;
            case unreal, extremelyUnreal -> true;
            case normal -> living.isDeadOrDying();
        };
    }

    public static boolean shouldDestroy(Entity.RemovalReason reason) {
        return reason != null && reason.shouldDestroy();
    }

    public static boolean shouldSave(Entity.RemovalReason reason) {
        return reason == null || reason.shouldSave();
    }

    public static float getHealth(Entity entity, Float original) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> 20.0F;
            case unreal, extremelyUnreal -> 0.0F;
            case normal -> original;
        };
    }

    public static boolean isAlive(Entity entity, Boolean original) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> true;
            case unreal, extremelyUnreal -> false;
            case normal -> original;
        };
    }

    public static boolean isDeadOrDying(Entity entity, Boolean original) {
        return switch (EntityUtil.getCategory(entity)) {
            case real -> false;
            case unreal, extremelyUnreal -> true;
            case normal -> original;
        };
    }
}
