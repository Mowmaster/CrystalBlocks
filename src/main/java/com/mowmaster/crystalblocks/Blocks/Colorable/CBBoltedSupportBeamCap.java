package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// Region 0 = the log beam, region 1 = the metal strap and end plate
public class CBBoltedSupportBeamCap extends CBBoltedBeamBlock
{
    // From models/block/bolted_support_beam_cap.json
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 4, 12, 15.5, 12)),
            List.of(new CBPixelBox(3.5, 12, 3.5, 4, 16, 12.5),
                    new CBPixelBox(12, 12, 3.5, 12.5, 16, 12.5),
                    new CBPixelBox(4, 12, 3.5, 12, 16, 4),
                    new CBPixelBox(4, 12, 12, 12, 16, 12.5),
                    new CBPixelBox(4, 15.5, 4, 12, 16, 12)));

    public CBBoltedSupportBeamCap(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.SQUARE; }
    @Override public boolean isCap() { return true; }
    @Override public boolean isTurnable() { return false; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBBoltedSupportBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBBoltedSupportBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBBoltedSupportBeam.OFFSET_Z; }
}
