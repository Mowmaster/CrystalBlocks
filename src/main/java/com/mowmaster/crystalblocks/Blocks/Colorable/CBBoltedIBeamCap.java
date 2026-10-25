package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

// One tint region (the stone beam and its end plate)
public class CBBoltedIBeamCap extends CBBoltedBeamBlock
{
    // From models/block/bolted_i_beam_cap.json
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 2, 12, 14, 4),
                    new CBPixelBox(7, 0, 4, 9, 14, 12),
                    new CBPixelBox(4, 0, 12, 12, 14, 14),
                    new CBPixelBox(4, 14, 0, 12, 16, 16)));

    public CBBoltedIBeamCap(Properties properties)
    {
        super(properties);
    }

    @Override public Kind kind() { return Kind.I_BEAM; }
    @Override public boolean isCap() { return true; }
    @Override public int turnCount() { return 2; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBBoltedIBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBBoltedIBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBBoltedIBeam.OFFSET_Z; }
}
