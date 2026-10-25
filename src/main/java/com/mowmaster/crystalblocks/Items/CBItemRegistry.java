package com.mowmaster.crystalblocks.Items;

import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.Tools.BaseChisel;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.mowmaster.crystalblocks.Items.Tools.CBColorPickerItem;

public class CBItemRegistry {

    public static final DeferredRegister.Items CBITEMS = DeferredRegister.createItems(CBReferences.MODID);

    public static final DeferredItem<Item> CRYSTAL_CHISEL_BASE = CBITEMS.registerItem("base_chisel", properties -> new BaseChisel(properties.durability(100)));
    public static final DeferredItem<Item> COLOR_PICKER = CBITEMS.registerItem("color_picker",
            properties -> new CBColorPickerItem(properties.stacksTo(1)));
    // A dye of any colour, stored in its dyed_color component
    public static final DeferredItem<Item> CUSTOM_DYE = CBITEMS.registerItem("custom_dye", Item::new);


    public static void register(IEventBus eventBus)
    {
        CBITEMS.register(eventBus);
    }
}
