package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

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
        // Plain drops: the item has no dyed_color component, so blocks always drop with the default colour
        dropSelf(CBBlockRegistry.TINTABLE_STONE.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_STAIRS.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_WALL.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_BUTTON.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_COLUMN.get());
        dropSelf(CBBlockRegistry.TINTABLE_STONE_HORIZONTAL_COLUMN.get());

        // Drops 1, or 2 when the slab is a double slab
        add(CBBlockRegistry.TINTABLE_STONE_SLAB.get(), createSlabItemTable(CBBlockRegistry.TINTABLE_STONE_SLAB.get()));
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