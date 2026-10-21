package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
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
        new CBTintedBlockFamily(blockModels, CBBlockRegistry.CRYSTAL_STONE_RED.get(), 16711680).stairs(CBBlockRegistry.CRYSTAL_STONE_RED_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_RED_SLAB.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_RED_WALL.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_RED_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_RED_PRESSUREPLATE.get());;
        new CBTintedBlockFamily(blockModels, CBBlockRegistry.CRYSTAL_STONE_GREEN.get(), 65280).stairs(CBBlockRegistry.CRYSTAL_STONE_GREEN_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_GREEN_SLAB.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_GREEN_WALL.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_GREEN_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_GREEN_PRESSUREPLATE.get());
        new CBTintedBlockFamily(blockModels, CBBlockRegistry.CRYSTAL_STONE_BLUE.get(), 255).stairs(CBBlockRegistry.CRYSTAL_STONE_BLUE_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_BLUE_SLAB.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_BLUE_WALL.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_BLUE_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_BLUE_PRESSUREPLATE.get());
        new CBTintedBlockFamily(blockModels, CBBlockRegistry.CRYSTAL_STONE_WHITE.get(), 16777215).stairs(CBBlockRegistry.CRYSTAL_STONE_WHITE_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_WHITE_SLAB.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_WHITE_WALL.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_WHITE_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_WHITE_PRESSUREPLATE.get());
        new CBTintedBlockFamily(blockModels, CBBlockRegistry.CRYSTAL_STONE_BLACK.get(), 2763306).stairs(CBBlockRegistry.CRYSTAL_STONE_BLACK_STAIRS.get())
                .slab(CBBlockRegistry.CRYSTAL_STONE_BLACK_SLAB.get())
                .wall(CBBlockRegistry.CRYSTAL_STONE_BLACK_WALL.get())
                .button(CBBlockRegistry.CRYSTAL_STONE_BLACK_BUTTON.get())
                .pressurePlate(CBBlockRegistry.CRYSTAL_STONE_BLACK_PRESSUREPLATE.get());

    }
}
