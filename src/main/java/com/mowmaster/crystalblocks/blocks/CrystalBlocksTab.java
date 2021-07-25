package com.mowmaster.crystalblocks.blocks;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CrystalBlocksTab extends CreativeModeTab {
    public CrystalBlocksTab(String label) {
        super("crystalblocks_tab");
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(CrystalBlock.ITM_BLOCK_STONE);
    }
}
