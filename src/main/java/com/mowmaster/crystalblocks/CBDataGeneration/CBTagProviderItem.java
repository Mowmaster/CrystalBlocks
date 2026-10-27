package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.concurrent.CompletableFuture;

public class CBTagProviderItem extends ItemTagsProvider {
    public CBTagProviderItem(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CBReferences.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CBTags.Items.CB_CHISELS)
                .add(CBItemRegistry.CRYSTAL_CHISEL_BASE.key());

        // The item versions of the set tags. Each block's item has the same id as the block, so the item's
        // registry key is built from the block's id.
        for (CBBlockSet set : CBBlockRegistry.SETS) {
            for (DeferredBlock<Block> block : CBTags.members(set)) {
                tag(CBTags.forSet(set).items()).add(ResourceKey.create(Registries.ITEM, block.getId()));
            }
        }
    }
}
