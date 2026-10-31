package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.List;

// Same placement and rotation as the accented cap, with four stepped layers each tinted separately
public class CBSteppedPillarCap extends CBAccentedPillarCap
{
    // From models/block/stepped_pillar_cap.json, facing north. Index = tintindex; each layer is a back + a front piece.
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(0, 0, 2, 16, 2, 16),     // outter_back
                    new CBPixelBox(0, 0, 0, 16, 16, 2)),    // outter_front
            List.of(new CBPixelBox(2, 2, 4, 14, 4, 16),     // inner_one_back
                    new CBPixelBox(2, 2, 2, 14, 16, 4)),    // inner_one_front
            List.of(new CBPixelBox(4, 4, 6, 12, 6, 16),     // inner_two_back
                    new CBPixelBox(4, 4, 4, 12, 16, 6)),    // inner_two_front
            List.of(new CBPixelBox(6, 6, 8, 10, 8, 16),     // inner_three_back
                    new CBPixelBox(6, 6, 6, 10, 16, 8)));   // inner_three_front

    public CBSteppedPillarCap(Properties properties)
    {
        super(properties, REGIONS);
    }

    // This model has its flat plate at the bottom, so vanilla's normal stair rule gives the same in-game
    // result as the accented cap: plate at the bottom when clicking the lower half
    @Override
    protected Half placedHalf(Direction clickedFace, double hitY)
    {
        boolean lowerHalf = clickedFace != Direction.DOWN && (clickedFace == Direction.UP || hitY <= 0.5);
        return lowerHalf ? Half.BOTTOM : Half.TOP;
    }
}
