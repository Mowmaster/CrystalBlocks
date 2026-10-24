package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

import java.util.Set;

public class CBBlockLootProvider extends BlockLootSubProvider
{
    public CBBlockLootProvider(LootTableSubProvider.Context output)
    {
        super(
                // Items that ignore the "survives explosion" check (none for us)
                Set.of(),
                // Feature flags to generate for
                FeatureFlags.REGISTRY.allFlags(),
                output);
    }

    @Override
    protected void generate()
    {
        // Plain drops for everything (no dyed_color, so blocks always drop with the default colour).
        // Slabs drop 2 when they're a double slab.
        for (var holder : CBBlockRegistry.BCBLOCKS.getEntries())
        {
            Block block = holder.get();
            if (block instanceof SlabBlock)
            {
                add(block, createSlabItemTable(block));
            }
            else
            {
                dropSelf(block);
            }
        }
    }

    // Only check our own blocks; datagen fails if any of these is missing a loot table
    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        return CBBlockRegistry.BCBLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.get())
                .toList();
    }
}