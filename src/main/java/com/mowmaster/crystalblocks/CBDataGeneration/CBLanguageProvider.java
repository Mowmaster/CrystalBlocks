package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class CBLanguageProvider extends LanguageProvider
{
    public CBLanguageProvider(PackOutput output)
    {
        super(output, CBReferences.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        add("itemGroup.crystalblocks", "Crystal Blocks");
        addItem(CBItemRegistry.CRYSTAL_CHISEL_BASE, "Chisel (Basic)");

        // Every set's blocks are named "<display name> <shape>"
        for (CBBlockSet set : CBBlockRegistry.SETS)
        {
            String name = set.displayName();
            addBlock(set.base(), name);
            addBlock(set.stairs(), name + " Stairs");
            addBlock(set.slab(), name + " Slab");
            addBlock(set.verticalSlab(), name + " Slab (Vertical)");
            addBlock(set.wall(), name + " Wall");
            addBlock(set.button(), name + " Button");
            addBlock(set.pressurePlate(), name + " Pressure Plate");
            addBlock(set.column(), name + " Column");
            addBlock(set.horizontalColumn(), name + " Column (Horizontal)");
            addBlock(set.accentedPillar(), name + " Accented Pillar");
            addBlock(set.accentedPillarCap(), name + " Accented Pillar Cap");
            addBlock(set.window(), name + " Window");
        }
    }
}
