package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// A chain, placed like the other beams. One tint region (metal).
public class CBChainBeam extends CBBoltedBeamBlock
{
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 12);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 12);

    // The chain as modelled: running north-south through the middle of the block
    public static final CBPixelBox FOOTPRINT = new CBPixelBox(6.75, 6.75, 0, 9.25, 9.25, 16);

    private static final List<List<CBPixelBox>> REGIONS = List.of(List.of(FOOTPRINT));

    public CBChainBeam(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.CHAIN; }
    @Override public boolean isCap() { return false; }
    // Modelled running north-south; three quarter turns around X stand it upright
    @Override public int modelXTurns() { return 3; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return OFFSET_Z; }
}
