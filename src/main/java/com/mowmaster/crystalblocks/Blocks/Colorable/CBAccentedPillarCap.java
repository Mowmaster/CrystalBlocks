package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CBAccentedPillarCap extends CBAccentedBlock
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    // BOTTOM = the model as made; TOP = turned upside down
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    // From models/block/accented_pillar_cap.json, facing north
    private static final List<CBPixelBox> BODY = List.of(
            new CBPixelBox(2, 0, 0, 14, 16, 2),    // outer_back
            new CBPixelBox(2, 14, 2, 14, 16, 16),  // outer_top
            new CBPixelBox(3, 0, 2, 13, 14, 4),    // inner_back
            new CBPixelBox(3, 12, 4, 13, 14, 16)); // inner_top
    private static final List<CBPixelBox> ACCENT = List.of(
            new CBPixelBox(4, 4, 4, 12, 6, 12),    // square_bottom
            new CBPixelBox(4, 10, 4, 12, 12, 12),  // square_top
            new CBPixelBox(4, 6, 4, 12, 10, 6),    // square_back
            new CBPixelBox(4, 6, 10, 12, 10, 12),  // square_front
            new CBPixelBox(5, 10, 12, 11, 12, 16), // pillar_top
            new CBPixelBox(5, 0, 4, 11, 4, 6));    // pillar_back

    public CBAccentedPillarCap(Properties properties)
    {
        this(properties, List.of(BODY, ACCENT));
    }

    // For caps with the same placement and rotation but a different model (e.g. the stepped cap)
    protected CBAccentedPillarCap(Properties properties, List<List<CBPixelBox>> regionBoxes)
    {
        super(properties, regionBoxes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HALF);
    }

    // ---- Rotation of the model, shared with datagen ----

    // Upside down = x 180, which also flips the back to the other side...
    public static int xTurnsFor(Half half)
    {
        return half == Half.TOP ? 2 : 0;
    }

    // ...so an upside-down cap gets an extra half turn of y to put its back where it belongs
    public static int yTurnsFor(Direction facing, Half half)
    {
        return CBPixelBox.quarterTurns(facing) + (half == Half.TOP ? 2 : 0);
    }

    @Override
    public int xQuarterTurns(BlockState state)
    {
        return xTurnsFor(state.getValue(HALF));
    }

    @Override
    public int yQuarterTurns(BlockState state)
    {
        return yTurnsFor(state.getValue(FACING), state.getValue(HALF)) + extraYTurns();
    }

    // For cap models made facing a different way (e.g. with their back on the south side)
    protected int extraYTurns()
    {
        return 0;
    }

    // ---- Placement ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        double hitY = context.getClickLocation().y - context.getClickedPos().getY();
        Half half = placedHalf(context.getClickedFace(), hitY);
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HALF, half);
    }

    // Like vanilla stairs but flipped: this model has its overhang at the top, so the upright cap goes on the upper half
    protected Half placedHalf(Direction clickedFace, double hitY)
    {
        boolean lowerHalf = clickedFace != Direction.DOWN && (clickedFace == Direction.UP || hitY <= 0.5);
        return lowerHalf ? Half.TOP : Half.BOTTOM;
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}