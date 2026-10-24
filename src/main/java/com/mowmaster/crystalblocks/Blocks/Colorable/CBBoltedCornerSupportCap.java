package com.mowmaster.crystalblocks.Blocks.Colorable;

import java.util.List;

// Same placement and rotation as the accented cap. One tint region (the stone bracket); the metal bolts are untinted.
public class CBBoltedCornerSupportCap extends CBAccentedPillarCap
{
    // From models/block/bolted_corner_support_cap.json. The diagonal braces and bolts are left out of the shape.
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(3, 0, 14, 13, 14, 16),   // back plate
                    new CBPixelBox(3, 14, 0, 13, 16, 16))); // top plate

    public CBBoltedCornerSupportCap(Properties properties)
    {
        super(properties, REGIONS);
    }

    // The model has its back on the south side; turn it half a turn so it faces the same way as the accented cap
    @Override
    protected int extraYTurns()
    {
        return 2;
    }
}
