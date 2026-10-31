package com.mowmaster.crystalblocks.Items;

import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.crystalblocks.Items.Tools.BaseChisel;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CBItemRegistry {

    public static final DeferredRegister.Items CBITEMS = DeferredRegister.createItems(CBReferences.MODID);

    // The colour picker and custom dye now come from MowLib (MowLibItems). These aliases turn the old crystalblocks:
    // items in saved worlds (inventories, chests) into MowLib's.
    static
    {
        CBITEMS.addAlias(CBReferences.id("color_picker"), MowLibReferences.id("color_picker"));
        CBITEMS.addAlias(CBReferences.id("custom_dye"), MowLibReferences.id("custom_dye"));
    }

    public static final DeferredItem<Item> CRYSTAL_CHISEL_BASE = CBITEMS.registerItem("base_chisel", properties -> new BaseChisel(properties.durability(100)));


    public static void register(IEventBus eventBus)
    {
        CBITEMS.register(eventBus);
    }
}
