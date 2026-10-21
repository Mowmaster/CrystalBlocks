package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class CBTagProviderItem extends ItemTagsProvider {
    public CBTagProviderItem(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, CBReferences.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CBTags.Items.CB_CHISELS)
                .add(CBItemRegistry.CRYSTAL_CHISEL_BASE.key());

    }
}
