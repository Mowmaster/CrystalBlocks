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
                .add(CBBlockRegistry.CRYSTAL_STONE_RED.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_GREEN.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLUE.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_WHITE.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLACK.key())

                .add(CBBlockRegistry.TINTABLE_STONE.key());

        tag(BlockTags.WALLS)
                .add(CBBlockRegistry.CRYSTAL_STONE_RED_WALL.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_GREEN_WALL.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLUE_WALL.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_WHITE_WALL.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLACK_WALL.key());

        tag(CBTags.Blocks.CB_BLOCKS_DYABLE)
                .add(CBBlockRegistry.CRYSTAL_STONE_RED.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_GREEN.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLUE.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_WHITE.key())
                .add(CBBlockRegistry.CRYSTAL_STONE_BLACK.key());
    }
}
