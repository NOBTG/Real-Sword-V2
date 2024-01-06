package com.nobtgCore;

import com.google.common.collect.Iterables;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RealLaunchPluginService implements ILaunchPluginService {
    private static final String owner = "com/nobtgRealSword/utils/CoreMethod";

    @Override
    public String name() {
        return "Real Sword LaunchPluginService";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return EnumSet.of(ILaunchPluginService.Phase.BEFORE);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        return transform(classNode);
    }

    private boolean transform(ClassNode classNode) {
        if (!classNode.name.startsWith("net/minecraft/") && !classNode.name.startsWith("net/minecraftforge/"))
            return false;
        AtomicBoolean returnZ = new AtomicBoolean(false);
        classNode.methods.forEach(method -> Iterables.unmodifiableIterable(method.instructions).forEach(insn -> {
            boolean rewrite = false;
            if (insn instanceof MethodInsnNode call && call.getOpcode() != Opcodes.INVOKESPECIAL) {
                switch (call.name) {
                    case "m_21223_" -> {
                        rMethod(call, "getHealth", "(Lnet/minecraft/world/entity/LivingEntity;)F");
                        rewrite = true;
                    }
                    case "m_146965_" -> {
                        rMethod(call, "shouldDestroy", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)Z");
                        rewrite = true;
                    }
                    case "m_146966_" -> {
                        rMethod(call, "shouldSave", "(Lnet/minecraft/world/entity/Entity$RemovalReason;)Z");
                        rewrite = true;
                    }
                    case "m_213877_" -> {
                        rMethod(call, "isRemoved", "(Lnet/minecraft/world/entity/Entity;)Z");
                        rewrite = true;
                    }
                    case "m_6084_" -> {
                        rMethod(call, "isAlive", "(Lnet/minecraft/world/entity/Entity;)Z");
                        rewrite = true;
                    }
                    case "m_21224_" -> {
                        rMethod(call, "isDeadOrDying", "(Lnet/minecraft/world/entity/LivingEntity;)Z");
                        rewrite = true;
                    }
                }
            } else if (insn instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD) {
                switch (field.name) {
                    case "f_20916_" -> {
                        rField(method, field, "getHurtTime", "(Lnet/minecraft/world/entity/LivingEntity;)I");
                        rewrite = true;
                    }
                    case "f_20917_" -> {
                        rField(method, field, "getHurtDuration", "(Lnet/minecraft/world/entity/LivingEntity;)I");
                        rewrite = true;
                    }
                    case "f_20919_" -> {
                        rField(method, field, "getDeathTime", "(Lnet/minecraft/world/entity/LivingEntity;)I");
                        rewrite = true;
                    }
                    case "f_146795_" -> {
                        rField(method, field, "getRemovalReason", "(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity$RemovalReason;");
                        rewrite = true;
                    }
                }
            }
            returnZ.set(rewrite);
        }));
        return returnZ.get();
    }

    private static void rMethod(MethodInsnNode call, String name, String desc) {
        call.setOpcode(Opcodes.INVOKESTATIC);
        call.owner = owner;
        call.name = name;
        call.desc = desc;
    }

    private static void rField(MethodNode method, FieldInsnNode field, String name, String desc) {
        method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc, false));
    }
}
