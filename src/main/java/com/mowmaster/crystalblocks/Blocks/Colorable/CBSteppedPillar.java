package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import java.util.List;

// Same placement and rotation as the accented pillar, with four stepped layers each tinted separately
public class CBSteppedPillar extends CBAccentedPillar
{
    // From models/block/stepped_pillar.json, facing north. Index = tintindex.
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(0, 0, 0, 16, 16, 2)),  // outter
            List.of(new CBPixelBox(2, 0, 2, 14, 16, 4)),  // inner_one
            List.of(new CBPixelBox(4, 0, 4, 12, 16, 6)),  // inner_two
            List.of(new CBPixelBox(6, 0, 6, 10, 16, 8))); // inner_three

    public CBSteppedPillar(Properties properties)
    {
        super(properties, REGIONS);
    }
}