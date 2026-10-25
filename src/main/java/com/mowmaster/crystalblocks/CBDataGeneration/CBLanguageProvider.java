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
            addBlock(set.accentedPillar(), name + " Accented Pillar");
            addBlock(set.accentedPillarCap(), name + " Accented Pillar Cap");
            addBlock(set.steppedPillar(), name + " Stepped Pillar");
            addBlock(set.steppedPillarCap(), name + " Stepped Pillar Cap");
            addBlock(set.window(), name + " Window");
        }
        addBlock(CBBlockRegistry.BOLTED_CORNER_SUPPORT_CAP, "Bolted Corner Support");
        addBlock(CBBlockRegistry.BOLTED_I_BEAM, "Bolted I-Beam");
        addBlock(CBBlockRegistry.BOLTED_I_BEAM_CAP, "Bolted I-Beam Cap");
        addBlock(CBBlockRegistry.BOLTED_SUPPORT_BEAM, "Bolted Support Beam");
        addBlock(CBBlockRegistry.BOLTED_SUPPORT_BEAM_CAP, "Bolted Support Beam Cap");

        addItem(CBItemRegistry.COLOR_PICKER, "Color Picker");
        addItem(CBItemRegistry.CUSTOM_DYE, "Custom Dye");
        add("message.crystalblocks.color_picker.picked", "Picked color %s");
        add("message.crystalblocks.color_picker.none", "This block has no color to pick");
        addBlock(CBBlockRegistry.CHAINED_SUPPORT_BEAM, "Chained Support Beam");
        addBlock(CBBlockRegistry.CHAIN_BEAM, "Chain Beam");
        addBlock(CBBlockRegistry.CHAIN_BEAM_CAP, "Chain Beam Cap");
        addBlock(CBBlockRegistry.CHAIN_BEAM_MEDIUM, "Medium Chain Beam");
        addBlock(CBBlockRegistry.CHAIN_BEAM_MEDIUM_CAP, "Medium Chain Beam Cap");
        addBlock(CBBlockRegistry.CHAIN_BEAM_LARGE, "Large Chain Beam");
        addBlock(CBBlockRegistry.CHAIN_BEAM_LARGE_CAP, "Large Chain Beam Cap");
        addBlock(CBBlockRegistry.CHAINED_SUPPORT_BEAM_MEDIUM, "Medium Chained Support Beam");

        addBlock(CBBlockRegistry.SCONCE_TORCH, "Torch Sconce");
    }
}
