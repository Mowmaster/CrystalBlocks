package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// A chain with a mounting plate on its end. The plate always sits against the surface it was placed on.
public class CBChainBeamCap extends CBBoltedBeamBlock
{
    private static final List<List<CBPixelBox>> REGIONS = List.of(List.of(
            CBChainBeam.FOOTPRINT,
            new CBPixelBox(4, 4, 0, 12, 12, 0.25)));  // plate_top

    public CBChainBeamCap(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.CHAIN; }
    @Override public boolean isCap() { return true; }
    // Modelled running north-south with the plate on the north end: three quarter turns around X put the plate on top
    @Override public int modelXTurns() { return 3; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBChainBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBChainBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBChainBeam.OFFSET_Z; }
}
