package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.Blocks.CBMaterial;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.concurrent.CompletableFuture;

public class CBTagProviderBlock extends BlockTagsProvider {
    public CBTagProviderBlock(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CBReferences.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (CBBlockSet set : CBBlockRegistry.SETS) {
            CBMaterial material = set.material();

            // Every block: mined with the material's tool, and dyeable
            for (DeferredBlock<Block> block : set.blocks()) {
                tag(material.mineableTag()).add(block.key());
                tag(CBTags.Blocks.CB_BLOCKS_DYABLE).add(block.key());
            }

            tag(BlockTags.STAIRS).add(set.stairs().key());
            tag(BlockTags.SLABS).add(set.slab().key());
            tag(BlockTags.WALLS).add(set.wall().key());
            tag(material.buttonTag()).add(set.button().key());
            tag(material.pressurePlateTag()).add(set.pressurePlate().key());
            for (DeferredBlock<Block> block : CBBlockRegistry.BOLTED) {
                tag(CBMaterial.METAL.mineableTag()).add(block.key());
                tag(CBTags.Blocks.CB_BLOCKS_DYABLE).add(block.key());
            }
            // Storage blocks: wooden, mined with an axe
            for (DeferredBlock<Block> block : CBBlockRegistry.STORAGE) {
                tag(BlockTags.MINEABLE_WITH_AXE).add(block.key());
                tag(CBTags.Blocks.CB_BLOCKS_DYABLE).add(block.key());
            }
        }
    }
}
