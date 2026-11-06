package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// A block that beams can be placed against and line up with: every CBBoltedBeamBlock, plus the corner beam.
// A beam placed by clicking one of these asks it where to sit (see CBBoltedBeamBlock#getStateForPlacement).
public interface CBBeamConnector
{
    // The spot on the given world side that the connecting beam should line up with, in this block's pixels,
    // or null if nothing attaches there
    @Nullable
    CBPixelBox connectionPoint(BlockState state, Direction side, CBBoltedBeamBlock connecting);
}
