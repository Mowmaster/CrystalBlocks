package com.mowmaster.crystalblocks.Registry;


import com.mowmaster.mowlib.Tabs.MowLibTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mowmaster.crystalblocks.Util.References.MODID;


public class DeferredRegisterItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> CHISEL = ITEMS.register("crystal_stone_chisel",
            () -> new Item(new Item.Properties().stacksTo(1).tab(MowLibTab.TAB_ITEMS)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
