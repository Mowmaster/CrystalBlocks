package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

public class CBBasicModelProvider  extends ModelProvider {
    public CBBasicModelProvider(PackOutput output) {
        super(output, CBReferences.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        itemModels.generateFlatItem(CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), ModelTemplates.FLAT_ITEM);


        blockModels.createTintedLeaves(CBBlockRegistry.CRYSTAL_STONE_RED.get(), TexturedModel.LEAVES, 16711680);
        blockModels.createTintedLeaves(CBBlockRegistry.CRYSTAL_STONE_GREEN.get(), TexturedModel.LEAVES, 65280);
        blockModels.createTintedLeaves(CBBlockRegistry.CRYSTAL_STONE_BLUE.get(), TexturedModel.LEAVES, 255);
        blockModels.createTintedLeaves(CBBlockRegistry.CRYSTAL_STONE_WHITE.get(), TexturedModel.LEAVES, 16777215);
        blockModels.createTintedLeaves(CBBlockRegistry.CRYSTAL_STONE_BLACK.get(), TexturedModel.LEAVES, 2763306);

        /*blockModels.family(CBBlockRegistry.CRYSTAL_STONE_RED.get())
                .stairs(CBBlockRegistry.CRYSTAL_STONE_RED_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_RED_SLAB.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_RED_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_RED_PRESSUREPLATE.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_RED_WALL.get());*/

    }
}
