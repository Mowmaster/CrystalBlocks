package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class CBTagProviderBlock extends BlockTagsProvider {
    public CBTagProviderBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CBReferences.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(CBBlockRegistry.TINTABLE_STONE.key())
                .add(CBBlockRegistry.TINTABLE_STONE_STAIRS.key())
                .add(CBBlockRegistry.TINTABLE_STONE_SLAB.key())
                .add(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.key())
                .add(CBBlockRegistry.TINTABLE_STONE_WALL.key())
                .add(CBBlockRegistry.TINTABLE_STONE_BUTTON.key())
                .add(CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.key())
                .add(CBBlockRegistry.TINTABLE_STONE_COLUMN.key())
                .add(CBBlockRegistry.TINTABLE_STONE_HORIZONTAL_COLUMN.key());

        tag(BlockTags.WALLS)
                .add(CBBlockRegistry.TINTABLE_STONE_WALL.key());

        tag(BlockTags.STAIRS)
                .add(CBBlockRegistry.TINTABLE_STONE_STAIRS.key());

        tag(BlockTags.STONE_BUTTONS)
                .add(CBBlockRegistry.TINTABLE_STONE_BUTTON.key());

        tag(BlockTags.STONE_PRESSURE_PLATES)
                .add(CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.key());

        tag(CBTags.Blocks.CB_BLOCKS_DYABLE)
                .add(CBBlockRegistry.TINTABLE_STONE.key())
                .add(CBBlockRegistry.TINTABLE_STONE_STAIRS.key())
                .add(CBBlockRegistry.TINTABLE_STONE_SLAB.key())
                .add(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.key())
                .add(CBBlockRegistry.TINTABLE_STONE_WALL.key())
                .add(CBBlockRegistry.TINTABLE_STONE_BUTTON.key())
                .add(CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.key());
    }
}
