package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// Region 0 = the log beam, region 1 = the metal straps
public class CBBoltedSupportBeam extends CBBoltedBeamBlock
{
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 8);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 8);

    public static final CBPixelBox FOOTPRINT = new CBPixelBox(4, 0, 4, 12, 16, 12);

    // From models/block/bolted_support_beam.json
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 4, 12, 16, 12)),
            List.of(new CBPixelBox(3.5, 6, 3.5, 4, 10, 12.5),
                    new CBPixelBox(12, 6, 3.5, 12.5, 10, 12.5),
                    new CBPixelBox(4, 6, 3.5, 12, 10, 4),
                    new CBPixelBox(4, 6, 12, 12, 10, 12.5)));

    public CBBoltedSupportBeam(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.SQUARE; }
    @Override public boolean isCap() { return false; }
    @Override public boolean isTurnable() { return false; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return OFFSET_Z; }
}
