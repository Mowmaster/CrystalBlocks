package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CBAccentedPillar extends CBAccentedBlock
{
    // The side the pillar's back is against: north/east/south/west = on a wall, down = lying on the floor, up = on the ceiling
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    // Which way the pillar runs while lying on the floor or ceiling (ignored when on a wall)
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    // From models/block/accented_pillar.json, facing north
    private static final List<CBPixelBox> BODY = List.of(
            new CBPixelBox(2, 0, 0, 14, 16, 2),  // outer_back
            new CBPixelBox(3, 0, 2, 13, 16, 4)); // inner_back
    private static final List<CBPixelBox> ACCENT = List.of(
            new CBPixelBox(5, 0, 4, 11, 16, 6)); // pillar_back

    public CBAccentedPillar(Properties properties)
    {
        this(properties, List.of(BODY, ACCENT));
    }

    // For pillars with the same placement and rotation but a different model (e.g. the stepped pillar)
    protected CBAccentedPillar(Properties properties, List<List<CBPixelBox>> regionBoxes)
    {
        super(properties, regionBoxes);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, AXIS);
    }

    // ---- Rotation of the model, shared with datagen ----

    // Floor: x 90 turns the north-facing back to face down. Ceiling: x 270 turns it to face up.
    public static int xTurnsFor(Direction facing)
    {
        return switch (facing)
        {
            case DOWN -> 1;
            case UP -> 3;
            default -> 0;
        };
    }

    // On a wall: turn to face the wall. Lying flat: the model runs north-south, so a quarter turn makes it run east-west.
    public static int yTurnsFor(Direction facing, Direction.Axis axis)
    {
        if (facing.getAxis().isVertical())
        {
            return axis == Direction.Axis.X ? 1 : 0;
        }
        return CBPixelBox.quarterTurns(facing);
    }

    @Override
    public int xQuarterTurns(BlockState state)
    {
        return xTurnsFor(state.getValue(FACING));
    }

    @Override
    public int yQuarterTurns(BlockState state)
    {
        return yTurnsFor(state.getValue(FACING), state.getValue(AXIS));
    }

    // ---- Placement ----

    // The back always goes against the clicked block. When lying flat it runs the way the player is looking.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction clickedFace = context.getClickedFace();
        Direction.Axis axis = clickedFace.getAxis().isVertical()
                ? context.getHorizontalDirection().getAxis()
                : Direction.Axis.Z;
        return defaultBlockState().setValue(FACING, clickedFace.getOpposite()).setValue(AXIS, axis);
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        Direction facing = state.getValue(FACING);
        // rotation.rotate leaves up and down unchanged
        BlockState rotated = state.setValue(FACING, rotation.rotate(facing));

        // A pillar lying flat swaps between north-south and east-west on a quarter turn
        boolean quarterTurn = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        if (facing.getAxis().isVertical() && quarterTurn)
        {
            Direction.Axis swapped = state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            rotated = rotated.setValue(AXIS, swapped);
        }
        return rotated;
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        // Mirroring never changes up/down, and the pillar is symmetric along its length
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}