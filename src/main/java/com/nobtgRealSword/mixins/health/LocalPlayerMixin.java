package com.nobtgRealSword.mixins.health;

import com.nobtgRealSword.utils.CoreMethod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LocalPlayer.class, priority = Integer.MAX_VALUE)
public abstract class LocalPlayerMixin extends LivingEntity {
    protected LocalPlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public float getHealth() {
        return CoreMethod.getHealth(this, super.getHealth());
    }

    @Override
    public boolean isAlive() {
        return CoreMethod.isAlive(this, super.isAlive());
    }

    @Override
    public boolean isDeadOrDying() {
        return CoreMethod.isDeadOrDying(this, super.isDeadOrDying());
    }
}
