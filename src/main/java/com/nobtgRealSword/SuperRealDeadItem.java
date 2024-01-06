package com.nobtgRealSword;

import com.nobtgCore.Helper;
import com.nobtgRealSword.utils.EntityUtil;
import com.nobtgRealSword.utils.client.RealFont;
import com.nobtgRealSword.utils.client.RenderUtil;
import com.nobtgRealSword.utils.enums.RenderStatus;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public final class SuperRealDeadItem extends Item {
    public static final List<String> list = List.of(
            "- \"...在星辰间，我们的爱如不灭的流光...\" -",
            "- \"...你是我生命中最美的诗篇，每个字都刻在心灵的深处...\" -",
            "- \"...就像夜空中的繁星，我的心永远璀璨于你的存在...\" -",
            "- \"...仿佛每一刻都是为了与你相遇，时光因你而变得如此美好...\" -",
            "- \"...在这尘世间，你是我心灵的唯一彼岸，爱你是我唯一的信仰...\" -"
    );

    public SuperRealDeadItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemstack) {
        return true;
    }

    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        SuperRealDeadItem.list.forEach(s -> {
            list.add(Component.literal(s));
        });
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        InteractionResultHolder<ItemStack> use = super.use(world, entity, hand);
        if (world.isClientSide()) {
            RenderUtil.setKill(Minecraft.getInstance(), !RenderUtil.isKill(Minecraft.getInstance()));
            GLFW.glfwSetWindowTitle(Minecraft.getInstance().getWindow().getWindow(), RenderUtil.createTitle(Minecraft.getInstance(), RenderUtil.isKill(Minecraft.getInstance())));
            RenderUtil.setCursorPos(Minecraft.getInstance());
        }
        EntityUtil.setUnReal(entity);
        SynchedEntityData data = new SynchedEntityData(entity) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T get(EntityDataAccessor<T> p_135371_) {
                return (T) (p_135371_ == LivingEntity.DATA_HEALTH_ID ? 0.0F : super.get(p_135371_));
            }
        };
        Helper.copyProperties(SynchedEntityData.class, entity.entityData, data);
        entity.entityData = data;
        return use;
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            public Font getFont(ItemStack stack, FontContext context) {
                return RealFont.getInstance(Minecraft.getInstance().fontManager, false, RenderStatus.renderGod);
            }
        });
    }
}