package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// One tint region (the stone beam). 8 wide x 12 deep, so it can turn.
public class CBBoltedIBeam extends CBBoltedBeamBlock
{
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 8);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 4);

    static final CBPixelBox FOOTPRINT = new CBPixelBox(4, 0, 2, 12, 16, 14);

    // From models/block/bolted_i_beam.json: two flanges and the web
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 2, 12, 16, 4),
                    new CBPixelBox(7, 0, 4, 9, 16, 12),
                    new CBPixelBox(4, 0, 12, 12, 16, 14)));

    public CBBoltedIBeam(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.I_BEAM; }
    @Override public boolean isCap() { return false; }
    @Override public boolean isTurnable() { return true; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return OFFSET_Z; }
}
