package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// A full metal block with glowing lines on every face (tintable_block_tech).
// Region 0 = the stone base (texture_one), region 1 = the glowing lines (texture_two, fullbright in the model).
// Clicking a glowing line dyes the lines; clicking anywhere else dyes the base.
public class CBTintableTechBlock extends CBTintableBlock
{
    public static final int BASE_REGION = 0;
    public static final int GLOW_REGION = 1;

    // The glow pattern on the north face, seen from outside the block, '#' = glowing pixel.
    // Top row is y 15; columns are x 0-15. Every other face uses the same pattern, turned or mirrored.
    private static final String[] PATTERN = {
            "....#......#....",  // y 15
            "....#......#....",  // y 14
            "....####.###....",  // y 13
            ".......#.#......",  // y 12
            "####.#.#.#.#####",  // y 11
            "...#.###.#.#....",  // y 10
            "...#.......#.#..",  // y 9
            "...#####.###.#..",  // y 8
            ".......#.....#..",  // y 7
            "..####.###.###..",  // y 6
            "..#..#.....#....",  // y 5
            "###.##.#.#.#####",  // y 4
            ".......#.#......",  // y 3
            "....####.###....",  // y 2
            "....#......#....",  // y 1
            "....#......#....",  // y 0
    };

    public CBTintableTechBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public int getTintRegionCount()
    {
        return 2;
    }

    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        return isGlowPixel(hit.getDirection(), CBPixelBox.pixelInside(hit)) ? GLOW_REGION : BASE_REGION;
    }

    // Turns the clicked pixel on any face into the matching pixel of the north pattern
    private static boolean isGlowPixel(Direction face, Vec3 pixel)
    {
        int x = cell(pixel.x);
        int y = cell(pixel.y);
        int z = cell(pixel.z);
        // {column, row height} in the north pattern
        int[] north = switch (face)
        {
            case NORTH -> new int[] { x, y };
            case SOUTH -> new int[] { 15 - x, y };
            case WEST -> new int[] { 15 - z, y };
            case EAST -> new int[] { z, y };
            case UP -> new int[] { 15 - z, x };
            case DOWN -> new int[] { 15 - z, 15 - x };
        };
        return PATTERN[15 - north[1]].charAt(north[0]) == '#';
    }

    // The whole pixel a coordinate falls in, kept inside 0-15 (a click right on the far edge reads as 16)
    private static int cell(double pixel)
    {
        return Math.clamp((int) Math.floor(pixel), 0, 15);
    }
}