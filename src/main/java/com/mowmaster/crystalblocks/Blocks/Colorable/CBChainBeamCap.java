package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// A chain with a mounting plate on its end; the plate always sits against the surface it was placed on.
// Small by default; the registry makes the medium and large caps by overriding size().
public class CBChainBeamCap extends CBBoltedBeamBlock
{
    public CBChainBeamCap(Properties properties)
    {
        super(properties);
    }

    // Must return a constant: it's read while the block is being built
    public CBChainSize size()
    {
        return CBChainSize.SMALL;
    }

    @Override public Kind kind() { return size().kind(); }
    @Override public boolean isCap() { return true; }
    // Modelled with the plate on the north end: three quarter turns around X put the plate on top
    @Override public int modelXTurns() { return 3; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return size().capRegions(); }
    @Override protected CBPixelBox footprint() { return size().footprint(); }
    @Override public IntegerProperty offsetXProperty() { return size().offsetX(); }
    @Override public IntegerProperty offsetZProperty() { return size().offsetZ(); }
}