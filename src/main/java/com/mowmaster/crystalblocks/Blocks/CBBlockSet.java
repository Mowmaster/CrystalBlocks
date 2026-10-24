package com.mowmaster.crystalblocks.Blocks;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;

// All the shapes for one texture. name is the base block's id, which is also its texture name
public record CBBlockSet(
        String name,
        String displayName,
        CBMaterial material,
        DeferredBlock<Block> base,
        DeferredBlock<Block> stairs,
        DeferredBlock<Block> slab,
        DeferredBlock<Block> verticalSlab,
        DeferredBlock<Block> wall,
        DeferredBlock<Block> button,
        DeferredBlock<Block> pressurePlate,
        DeferredBlock<Block> column,
        DeferredBlock<Block> horizontalColumn,
        DeferredBlock<Block> accentedPillar,
        DeferredBlock<Block> accentedPillarCap,
        DeferredBlock<Block> steppedPillar,
        DeferredBlock<Block> steppedPillarCap,
        DeferredBlock<Block> window)
{
    // Every block in the set, in creative tab order
    public List<DeferredBlock<Block>> blocks()
    {
        return List.of(base, stairs, slab, verticalSlab, wall, button, pressurePlate,
                column, horizontalColumn, accentedPillar, accentedPillarCap, steppedPillar, steppedPillarCap, window);
    }
}
