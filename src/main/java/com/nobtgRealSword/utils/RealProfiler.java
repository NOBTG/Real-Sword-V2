package com.nobtgRealSword.utils;

import com.google.common.collect.ImmutableSet;
import com.nobtgRealSword.utils.client.RenderUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.JNI;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Supplier;

public final class RealProfiler implements ProfileCollector {
    private final List<Task> taskList = new ArrayList<>();
    public final Object lock = new Object();

    public void addTask(Task task) {
        synchronized (lock) {
            taskList.add(task);
        }
    }

    public List<Task> getTaskList() {
        synchronized (lock) {
            return taskList;
        }
    }

    private void startTask() {
        Minecraft mc = Minecraft.getInstance();

        if (RenderUtil.isDefense(mc)) {
            mc.screen = null;
            mc.setOverlay(null);
            RenderUtil.grabMouse(mc);
        }

        synchronized (lock) {
            ListIterator<Task> iterator = taskList.listIterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                task.runnable().run();
                if (!task.isWhile()) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public ProfileResults getResults() {
        return EmptyProfileResults.EMPTY;
    }

    @Nullable
    @Override
    public ActiveProfiler.PathEntry getEntry(String pEntryId) {
        return null;
    }

    @Override
    public Set<Pair<String, MetricCategory>> getChartedPaths() {
        return ImmutableSet.of();
    }

    @Override
    public void startTick() {
        startTask();
    }

    @Override
    public void endTick() {
        startTask();
    }

    @Override
    public void push(String pName) {
        startTask();
    }

    @Override
    public void push(Supplier<String> pNameSupplier) {
        startTask();
    }

    @Override
    public void pop() {
        startTask();
    }

    @Override
    public void popPush(String pName) {
        startTask();
    }

    @Override
    public void popPush(Supplier<String> pNameSupplier) {
        startTask();
    }

    @Override
    public void markForCharting(MetricCategory pCategory) {
    }

    @Override
    public void incrementCounter(String pCounterName, int pIncrement) {
    }

    @Override
    public void incrementCounter(Supplier<String> pCounterNameSupplier, int pIncrement) {
    }

    public record Task(Runnable runnable, boolean isWhile, Entity entity) {
    }
}
