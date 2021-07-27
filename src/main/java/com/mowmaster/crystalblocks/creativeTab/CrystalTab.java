package com.mowmaster.crystalblocks.creativeTab;

import com.mowmaster.crystalblocks.items.ColorApplicator;
import com.mowmaster.crystalblocks.reference.References;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CrystalTab extends CreativeModeTab
{
    public CrystalTab() {
        super(References.MODID+"_tab");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ColorApplicator.ITM_APPLICATOR);
    }
}