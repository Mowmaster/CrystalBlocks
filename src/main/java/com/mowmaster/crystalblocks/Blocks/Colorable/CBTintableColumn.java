package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// The plain 8x8 column for every block set: upright or horizontal, any position, one tint region.
// Same size as the support beam, so the two line up and pass colours to each other.
public class CBTintableColumn extends CBBoltedBeamBlock
{
    private static final List<List<CBPixelBox>> REGIONS = List.of(List.of(CBBoltedSupportBeam.FOOTPRINT));

    public CBTintableColumn(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.SQUARE; }
    @Override public boolean isCap() { return false; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBBoltedSupportBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBBoltedSupportBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBBoltedSupportBeam.OFFSET_Z; }
}