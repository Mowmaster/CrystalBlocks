package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// The raised version of the tech block (tintable_block_tech_3d): the glowing lines stand 1 pixel proud of a
// stone core, inside a stone edge frame. Region 0 = stone, region 1 = the glowing lines.
public class CBTintableTech3DBlock extends CBTintableTechBlock
{
    public CBTintableTech3DBlock(Properties properties)
    {
        super(properties);
    }

    // The outer 1-pixel layer holds only the edge frame and the glow lines. A clicked pixel in that layer on exactly
    // one axis is a line (front or side); on two axes it's the edge frame; on none it's the core.
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        int outerAxes = (inOuterLayer(pixel.x) ? 1 : 0) + (inOuterLayer(pixel.y) ? 1 : 0) + (inOuterLayer(pixel.z) ? 1 : 0);
        return outerAxes == 1 ? GLOW_REGION : BASE_REGION;
    }

    private static boolean inOuterLayer(double pixel)
    {
        return pixel < 1 || pixel > 15;
    }
}