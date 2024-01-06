package com.nobtgRealSword.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerList.class, priority = Integer.MAX_VALUE)
public abstract class PlayerListMixin {
    @Unique
    private String realSword$last = "";

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    private void broadcastSystemMessage(Component p_240618_, boolean p_240644_, CallbackInfo ci) {
        String msg = p_240618_.getString();
        if (realSword$last.contains(msg) || msg.contains(realSword$last)) ci.cancel();
        else realSword$last = msg;
        if (realSword$hasExcessiveDuplicateCharacters(msg)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean realSword$hasExcessiveDuplicateCharacters(String str) {
        int c = 0;
        for (int i = 0; i < str.length(); i++) {
            for (int j = i + 1; j < str.length(); j++) {
                if (str.charAt(i) == str.charAt(j)) {
                    c += 1;
                }
            }
        }
        return c > 4;
    }
}
