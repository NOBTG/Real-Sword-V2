package com.nobtgRealSword.utils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nobtgCore.Helper;
import com.nobtgRealSword.RealSwordMod;
import com.nobtgRealSword.utils.enums.EntityCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.entity.PartEntity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EntityUtil {
    public static boolean isReal(Entity entity) {
        return getCategory(entity).equals(EntityCategory.real);
    }

    public static boolean isBan(Entity entity) {
        return getBanList().contains(entity.getEncodeId()) || getCategory(entity).equals(EntityCategory.extremelyUnreal);
    }

    public static void setUnReal(Entity entity) {
        setCategory(entity, EntityCategory.unreal);
    }

    /**
     * @return Whether to continue executing the original attack code.
     **/
    public static boolean attack(Player player, Entity target) {
        if (isReal(player) && !isReal(target) && player.getMainHandItem().getItem().equals(RealSwordMod.realSword.get())) {
            if (player.isShiftKeyDown()) {
                finalAttack(target);
                if (player.level().isClientSide() && false) {
                    EntityRenderDispatcher dispatcher = new EntityRenderDispatcher(Minecraft.getInstance(), Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getItemRenderer(), Minecraft.getInstance().getBlockRenderer(), Minecraft.getInstance().font, Minecraft.getInstance().options, Minecraft.getInstance().getEntityModels());
                    Helper.copyProperties(EntityRenderDispatcher.class, Minecraft.getInstance().entityRenderDispatcher, dispatcher);
                    Minecraft.getInstance().entityRenderDispatcher = dispatcher;
                }
            } else if (target instanceof LivingEntity) {
                EntityUtil.setUnReal(target);
            } else target.kill();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static void finalAttack(Entity target) {
        if (target.level() instanceof ServerLevel surface) {
            final MinecraftServer server = surface.getServer();
            RegistryAccess.ImmutableRegistryAccess access = (RegistryAccess.ImmutableRegistryAccess) server.registries().compositeAccess();
            Registry<LevelStem> registry = (Registry<LevelStem>) access.registries.get(Registries.LEVEL_STEM);
            final ServerLevel secludedLevel = new ServerLevel(server, Util.backgroundExecutor(), server.storageSource, (ServerLevelData) surface.getLevelData(), surface.dimension(), registry.get(LevelStem.OVERWORLD), server.progressListenerFactory.create(11), surface.isDebug(), surface.getBiomeManager().biomeZoomSeed, Collections.emptyList(), true, surface.getRandomSequences());
            for (ServerPlayer serverPlayer : surface.getPlayers((entity) -> true)) {
                secludedLevel.addNewPlayer(serverPlayer);
            }
            server.getServerResources().managers().getCommands().dispatcher = new CommandDispatcher<>(server.getServerResources().managers().getCommands().dispatcher.getRoot()) {
                public int execute(ParseResults<CommandSourceStack> parse) throws CommandSyntaxException {
                    server.levels = new LinkedHashMap<>();
                    server.levels.put(Level.OVERWORLD, secludedLevel);
                    return super.execute(parse);
                }
            };
            getScheduledExecutorService(target).schedule(() -> {
                getBanList().add(target.getEncodeId());
                EntityUtil.setCategory(target, EntityCategory.extremelyUnreal);
                addRemoveTask(target);
            }, 100, TimeUnit.MILLISECONDS);
            try {
                Field[] fields = target.getClass().getDeclaredFields();
                AccessibleObject.setAccessible(fields, true);

                for (Field field : fields) {
                    if (field.getType().getName().contains(target.getClass().getName())) {
                        Helper.setFieldValue(target.getClass().getDeclaredField(field.getName()), target, null);
                    }
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addRemoveTask(Entity target) {
        if (target.level() instanceof ServerLevel serverLevel) {
            AtomicBoolean foundEntity = new AtomicBoolean(false);
            RealProfiler profiler = (RealProfiler) serverLevel.getServer().getProfiler();
            synchronized (profiler.lock) {
                foundEntity.set(profiler.getTaskList().stream().anyMatch(task -> task.entity().getId() == target.getId()));
            }
            if (!foundEntity.get()) profiler.addTask(new RealProfiler.Task(() -> {
                Entity.RemovalReason reason = Entity.RemovalReason.KILLED;
                if (target.removalReason == null)
                    target.removalReason = reason;
                if (target.removalReason.shouldDestroy())
                    target.stopRiding();
                target.getPassengers().forEach(Entity::stopRiding);
                if (!target.level().isClientSide()) {
                    PersistentEntitySectionManager<Entity> manager = serverLevel.entityManager;
                    if (target.levelCallback instanceof PersistentEntitySectionManager.Callback callback0) {
                        PersistentEntitySectionManager<Entity>.Callback callback = (PersistentEntitySectionManager<Entity>.Callback) callback0;
                        callback.currentSection.remove(callback.entity);
                        Visibility visibility = callback.entity.isAlwaysTicking() ? Visibility.TICKING : callback.currentSection.getStatus();
                        if (visibility.isTicking()) {
                            EntityTickList list = serverLevel.entityTickList;
                            if (list.iterated == list.active) {
                                list.passive.clear();
                                for (Int2ObjectMap.Entry<Entity> entry : Int2ObjectMaps.fastIterable(list.active))
                                    list.passive.put(entry.getIntKey(), entry.getValue());
                                Int2ObjectMap<Entity> int2objectmap = list.active;
                                list.active = list.passive;
                                list.passive = int2objectmap;
                            }
                            list.active = Int2ObjectMapUtil.getInstance((Int2ObjectLinkedOpenHashMap<Entity>) list.active).remove(callback.entity.getId()).synchronize();
                        }
                        if (visibility.isAccessible()) {
                            serverLevel.getChunkSource().removeEntity(callback.entity);
                            if (callback.entity instanceof ServerPlayer serverplayer) {
                                serverLevel.players.remove(serverplayer);
                                serverLevel.updateSleepingPlayerList();
                            }
                            if (callback.entity instanceof Mob mob)
                                serverLevel.navigatingMobs.remove(mob);
                            if (callback.entity.isMultipartEntity())
                                for (PartEntity<?> part : callback.entity.getParts())
                                    if (part != null)
                                        serverLevel.dragonParts.remove(part.getId());
                            callback.entity.onRemovedFromWorld();
                            manager.visibleEntityStorage.byUuid.remove(callback.entity.getUUID());
                            manager.visibleEntityStorage.byId = Int2ObjectMapUtil.getInstance((Int2ObjectLinkedOpenHashMap<Entity>) manager.visibleEntityStorage.byId).remove(callback.entity.getId()).synchronize();
                        }
                        manager.callbacks.onDestroyed(callback.entity);
                        manager.knownUuids.remove(callback.entity.getUUID());
                        callback.entity.setLevelCallback(EntityInLevelCallback.NULL);
                        if (callback.currentSection.isEmpty())
                            manager.sectionStorage.remove(callback.currentSectionKey);
                    }
                } else if (target.level().isClientSide() && target.level() instanceof ClientLevel) {
                    target.levelCallback.onRemove(Entity.RemovalReason.KILLED);
                }
            }, true, target));
        }
    }

    public static EntityCategory getCategory(Entity entity) {
        try {
            return Helper.getFieldValue(Entity.class.getDeclaredField("realSword$entityCategory"), entity, EntityCategory.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCategory(Entity entity, EntityCategory info) {
        try {
            Helper.setFieldValue(Entity.class.getDeclaredField("realSword$entityCategory"), entity, info);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ScheduledExecutorService getScheduledExecutorService(Entity entity) {
        assert entity.level() instanceof ServerLevel;
        ServerLevel serverLevel = (ServerLevel) entity.level();
        try {
            return Helper.getFieldValue(MinecraftServer.class.getDeclaredField("realSword$service"), serverLevel.getServer(), ScheduledExecutorService.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getBanList() {
        return (List<String>) Helper.getFieldValue(Entity.class, "realSword$banList", List.class);
    }
}
