package com.nobtgRealSword.mixins;

import com.nobtgCore.Helper;
import com.nobtgRealSword.RealSwordMod;
import com.nobtgRealSword.utils.EntityUtil;
import com.nobtgRealSword.utils.enums.EntityCategory;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public abstract class EntityMixin {

    @Shadow
    public abstract Iterable<ItemStack> getAllSlots();

    @Shadow
    public SynchedEntityData entityData;
    @Unique
    private EntityCategory realSword$entityCategory = EntityCategory.normal;

    @Unique
    private static final List<String> realSword$banList = new CopyOnWriteArrayList<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        AtomicBoolean hasRealSword = new AtomicBoolean(false);
        if ((Entity) (Object) this instanceof Player player) {
            Inventory inventory = player.getInventory();
            hasRealSword.set(player.getMainHandItem().getItem().equals(RealSwordMod.realSword.get()) || inventory.getSelected().getItem().equals(RealSwordMod.realSword.get()) || Stream.of(inventory.items, inventory.armor, inventory.offhand).flatMap(List::stream).parallel().anyMatch(stack -> stack.getItem().equals(RealSwordMod.realSword.get())));
        } else
            hasRealSword.set(StreamSupport.stream(this.getAllSlots().spliterator(), true).anyMatch(stack -> stack.getItem().equals(RealSwordMod.realSword.get())));
        if (hasRealSword.get()) EntityUtil.setCategory((Entity) (Object) this, EntityCategory.real);
        if (EntityUtil.isReal((Entity) (Object) this)) {
            SynchedEntityData data = new SynchedEntityData((Entity) (Object) this) {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T get(EntityDataAccessor<T> p_135371_) {
                    return (T) (p_135371_ == LivingEntity.DATA_HEALTH_ID ? 20.0F : super.get(p_135371_));
                }
            };
            Helper.copyProperties(SynchedEntityData.class, this.entityData, data);
            this.entityData = data;
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void setRemoved(Entity.RemovalReason pRemovalReason, CallbackInfo ci) {
        if (EntityUtil.isReal((Entity) (Object) this)) ci.cancel();
    }

    @Inject(method = "isRemoved", at = @At("RETURN"), cancellable = true)
    private void isRemoved(CallbackInfoReturnable<Boolean> cir) {
        if (EntityUtil.isReal((Entity) (Object) this))
            cir.setReturnValue(false);
    }
}
