package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.CBBoltedBeamBlock;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import net.minecraft.resources.Identifier;

public class CBBasicModelProvider  extends ModelProvider {
    public CBBasicModelProvider(PackOutput output) {
        super(output, CBReferences.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        itemModels.generateFlatItem(CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), ModelTemplates.FLAT_ITEM);
        // Shared by every set's column copies; created once so the file is only written once
        Identifier columnTemplate = CBBoltedModels.columnTemplate(blockModels);

        for (CBBlockSet set : CBBlockRegistry.SETS) {
            new CBTintedBlockFamily(blockModels, set.base().get(), new Dye(0xFFFFFFFF))
                    .stairs(set.stairs().get())
                    .twoToneSlab(set.slab().get())
                    .verticalSlab(set.verticalSlab().get())
                    .wall(set.wall().get())
                    .button(set.button().get())
                    .pressurePlate(set.pressurePlate().get())
                    .accentedPillar(set.accentedPillar().get())
                    .accentedPillarCap(set.accentedPillarCap().get())
                    .steppedPillar(set.steppedPillar().get())
                    .steppedPillarCap(set.steppedPillarCap().get());

            // The window's frame uses the set's own texture
            CBWindowModels.generate(blockModels, set.window().get(), set.name());
            CBBoltedModels.beam(blockModels, (CBBoltedBeamBlock) set.column().get(), columnTemplate,
                    CBBoltedModels.textures(set.name(), set.name()));
        }
        CBBoltedModels.generate(blockModels);
    }
}
