package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// A chain, placed like the other beams. One tint region (metal). Small by default; the registry makes the
// medium and large chains by overriding size().
public class CBChainBeam extends CBBoltedBeamBlock
{
    public CBChainBeam(Properties properties)
    {
        super(properties);
    }

    // Must return a constant: it's read while the block is being built
    public CBChainSize size()
    {
        return CBChainSize.SMALL;
    }

    @Override public Kind kind() { return size().kind(); }
    @Override public boolean isCap() { return false; }
    // Modelled running north-south; three quarter turns around X stand it upright
    @Override public int modelXTurns() { return 3; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return size().beamRegions(); }
    @Override protected CBPixelBox footprint() { return size().footprint(); }
    @Override public IntegerProperty offsetXProperty() { return size().offsetX(); }
    @Override public IntegerProperty offsetZProperty() { return size().offsetZ(); }
}