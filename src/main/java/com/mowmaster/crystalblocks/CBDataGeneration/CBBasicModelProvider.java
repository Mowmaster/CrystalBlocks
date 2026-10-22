package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class CBBasicModelProvider  extends ModelProvider {
    public CBBasicModelProvider(PackOutput output) {
        super(output, CBReferences.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        itemModels.generateFlatItem(CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), ModelTemplates.FLAT_ITEM);

        new CBTintedBlockFamily(blockModels, CBBlockRegistry.TINTABLE_STONE.get(), new Dye(0xFFFFFFFF))
                .stairs(CBBlockRegistry.TINTABLE_STONE_STAIRS.get())
                .twoToneSlab(CBBlockRegistry.TINTABLE_STONE_SLAB.get())
                .verticalSlab(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get())
                .wall(CBBlockRegistry.TINTABLE_STONE_WALL.get())
                .button(CBBlockRegistry.TINTABLE_STONE_BUTTON.get())
                .pressurePlate(CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.get())
                .column(CBBlockRegistry.TINTABLE_STONE_COLUMN.get())
                .horizontalColumn(CBBlockRegistry.TINTABLE_STONE_HORIZONTAL_COLUMN.get());

    }
}
