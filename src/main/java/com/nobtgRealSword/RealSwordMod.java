package com.nobtgRealSword;

import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RealSwordMod.modID)
public final class RealSwordMod {
    public static final String modID = "real_sword";
    public static final DeferredRegister<Item> items = DeferredRegister.create(ForgeRegistries.ITEMS, modID);
    public static final RegistryObject<RealSwordItem> realSword = items.register("real_sword", () -> new RealSwordItem(Tiers.DIAMOND, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<SuperRealDeadItem> superRealDead = items.register("super_real_dead", SuperRealDeadItem::new);

    public RealSwordMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        items.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.COMBAT))
            event.getEntries().putAfter(Items.DIAMOND_SWORD.getDefaultInstance(), realSword.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES))
            event.getEntries().putFirst(superRealDead.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
