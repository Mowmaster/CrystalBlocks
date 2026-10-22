package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CBTintableHorizontalColumn extends Block implements IColorableBlock
{
    public static final int SIZE = CBTintableColumn.SIZE;
    public static final int MAX_OFFSET = CBTintableColumn.MAX_OFFSET;

    // Which way the beam runs: X = east-west, Z = north-south
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    // The beam's edge across its length, in pixels: its north edge when running along X, its west edge when running along Z
    public static final IntegerProperty SIDE = IntegerProperty.create("side", 0, MAX_OFFSET);
    // The beam's bottom edge, in pixels
    public static final IntegerProperty Y = IntegerProperty.create("y", 0, MAX_OFFSET);

    private static final VoxelShape[] SHAPES_ALONG_X = new VoxelShape[(MAX_OFFSET + 1) * (MAX_OFFSET + 1)];
    private static final VoxelShape[] SHAPES_ALONG_Z = new VoxelShape[(MAX_OFFSET + 1) * (MAX_OFFSET + 1)];

    static
    {
        for (int side = 0; side <= MAX_OFFSET; side++)
        {
            for (int y = 0; y <= MAX_OFFSET; y++)
            {
                SHAPES_ALONG_X[shapeIndex(side, y)] = Block.box(0, y, side, 16, y + SIZE, side + SIZE);
                SHAPES_ALONG_Z[shapeIndex(side, y)] = Block.box(side, y, 0, side + SIZE, y + SIZE, 16);
            }
        }
    }

    public CBTintableHorizontalColumn(Properties properties)
    {
        super(properties);
        // Default: running north-south, centred
        registerDefaultState(withPosition(stateDefinition.any(), Direction.Axis.Z, MAX_OFFSET / 2, MAX_OFFSET / 2));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AXIS, SIDE, Y);
    }

    // ---- Shape ----

    private static int shapeIndex(int side, int y)
    {
        return side * (MAX_OFFSET + 1) + y;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        VoxelShape[] shapes = state.getValue(AXIS) == Direction.Axis.X ? SHAPES_ALONG_X : SHAPES_ALONG_Z;
        return shapes[shapeIndex(state.getValue(SIDE), state.getValue(Y))];
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    // ---- Placement ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // Placed against the end of another beam: copy its axis and position so the beam extends in a straight line
        if (clickedFace.getAxis().isHorizontal() && !context.replacingClickedOnBlock())
        {
            BlockState against = context.getLevel().getBlockState(pos.relative(clickedFace.getOpposite()));
            if (against.getBlock() instanceof CBTintableHorizontalColumn && against.getValue(AXIS) == clickedFace.getAxis())
            {
                return withPosition(defaultBlockState(), against.getValue(AXIS), against.getValue(SIDE), against.getValue(Y));
            }
        }

        // Side of a block: stick straight out of it. Top or bottom: run in the direction the player is looking.
        Direction.Axis axis = clickedFace.getAxis().isHorizontal()
                ? clickedFace.getAxis()
                : context.getHorizontalDirection().getAxis();

        // Centre the beam's cross-section on the clicked pixel, kept inside the block
        Vec3 hit = context.getClickLocation();
        double across = axis == Direction.Axis.X ? hit.z - pos.getZ() : hit.x - pos.getX();
        return withPosition(defaultBlockState(), axis,
                CBTintableColumn.toOffset(across),
                CBTintableColumn.toOffset(hit.y - pos.getY()));
    }

    private static BlockState withPosition(BlockState state, Direction.Axis axis, int side, int y)
    {
        return state.setValue(AXIS, axis).setValue(SIDE, side).setValue(Y, y);
    }

    // Extending a beam: copy the colour of a matching beam at either end, unless the placed item already gave this one a colour
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity placed) || placed.isDyed(0)) return;

        Direction[] ends = state.getValue(AXIS) == Direction.Axis.X
                ? new Direction[] { Direction.WEST, Direction.EAST }
                : new Direction[] { Direction.NORTH, Direction.SOUTH };

        for (Direction direction : ends)
        {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() instanceof CBTintableHorizontalColumn
                    && neighbor.getValue(AXIS) == state.getValue(AXIS)
                    && neighbor.getValue(SIDE).equals(state.getValue(SIDE))
                    && neighbor.getValue(Y).equals(state.getValue(Y))
                    && level.getBlockEntity(neighborPos) instanceof CBTintBlockEntity source
                    && source.isDyed(0))
            {
                placed.setColor(0, source.getColor(0));
                return;
            }
        }
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    // Turning 90 degrees swaps the axis; whether the side offset flips depends on which way it turns
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        Direction.Axis axis = state.getValue(AXIS);
        int side = state.getValue(SIDE);
        int y = state.getValue(Y);
        boolean alongX = axis == Direction.Axis.X;
        return switch (rotation)
        {
            case NONE -> state;
            case CLOCKWISE_90 -> withPosition(state, alongX ? Direction.Axis.Z : Direction.Axis.X, alongX ? MAX_OFFSET - side : side, y);
            case CLOCKWISE_180 -> withPosition(state, axis, MAX_OFFSET - side, y);
            case COUNTERCLOCKWISE_90 -> withPosition(state, alongX ? Direction.Axis.Z : Direction.Axis.X, alongX ? side : MAX_OFFSET - side, y);
        };
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        Direction.Axis axis = state.getValue(AXIS);
        int flipped = MAX_OFFSET - state.getValue(SIDE);
        return switch (mirror)
        {
            case NONE -> state;
            // Flips north/south: only moves a beam running along X (its side offset is a Z position)
            case LEFT_RIGHT -> axis == Direction.Axis.X ? state.setValue(SIDE, flipped) : state;
            // Flips east/west: only moves a beam running along Z (its side offset is an X position)
            case FRONT_BACK -> axis == Direction.Axis.Z ? state.setValue(SIDE, flipped) : state;
        };
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}