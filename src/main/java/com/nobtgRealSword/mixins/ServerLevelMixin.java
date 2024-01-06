package com.nobtgRealSword.mixins;

import com.nobtgRealSword.utils.EntityUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(value = ServerLevel.class, priority = Integer.MAX_VALUE)
public abstract class ServerLevelMixin extends Level {
    protected ServerLevelMixin(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
        super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
    }

    @Shadow
    @Final
    private ServerChunkCache chunkSource;

    @Shadow
    protected abstract boolean shouldDiscardEntity(Entity pEntity);

    @Shadow
    public abstract void tickNonPassenger(Entity p_8648_);

    @Shadow
    @Nonnull
    public abstract MinecraftServer getServer();

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
    private void forEach(EntityTickList instance, Consumer<Entity> pEntity) {
        ProfilerFiller profilerfiller = this.getProfiler();
        instance.forEach(entity0 -> {
            if (EntityUtil.isBan(entity0)) {
                EntityUtil.addRemoveTask(entity0);
                return;
            }
            if (!entity0.isRemoved()) {
                if (this.shouldDiscardEntity(entity0)) {
                    entity0.discard();
                } else {
                    profilerfiller.push("checkDespawn");
                    entity0.checkDespawn();
                    profilerfiller.pop();
                    if (this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(entity0.chunkPosition().toLong())) {
                        Entity entity = entity0.getVehicle();
                        if (entity != null) {
                            if (!entity.isRemoved() && entity.hasPassenger(entity0)) {
                                return;
                            }

                            entity0.stopRiding();
                        }

                        profilerfiller.push("tick");
                        if (!entity0.isRemoved() && !(entity0 instanceof PartEntity)) {
                            this.guardEntityTick(this::tickNonPassenger, entity0);
                        }
                        profilerfiller.pop();
                    }
                }
            }
        });
    }
}
