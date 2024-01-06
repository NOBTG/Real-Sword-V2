package com.nobtgRealSword.utils.client;

import com.nobtgRealSword.utils.RealProfiler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RealMinecraft extends Minecraft {
    public RealMinecraft(GameConfig pGameConfig) {
        super(pGameConfig);
    }

    @Override
    public void tick() {
        if (!(this.profiler instanceof RealProfiler))
            this.profiler = new RealProfiler();
        super.tick();
        RealFont.tick += 0.888F;
        if (RealFont.tick >= 720.0f) RealFont.tick = 0.0F;
    }
}
