package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CBTintableWindow extends Block implements IColorableBlock
{
    public static final int FRAME_REGION = 0; // tintindex 0, texture_one
    public static final int GLASS_REGION = 1; // tintindex 1, texture_two

    // The side the window is looked at from. South and west look identical, so only north and east exist.
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.NORTH, Direction.EAST);

    // Joined to another window (same facing) on that edge. Left/right are the model's north/south edges:
    // facing east -> left = north, right = south. Facing north -> left = east, right = west.
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    // Draw a small corner frame piece: both edges next to that corner are joined, but the diagonal window is missing
    public static final BooleanProperty UP_LEFT = BooleanProperty.create("up_left");
    public static final BooleanProperty UP_RIGHT = BooleanProperty.create("up_right");
    public static final BooleanProperty DOWN_LEFT = BooleanProperty.create("down_left");
    public static final BooleanProperty DOWN_RIGHT = BooleanProperty.create("down_right");

    // false = clear cb_glass texture, true = tintable_glass texture (switched on the first time the glass is dyed)
    public static final BooleanProperty TINTED_GLASS = BooleanProperty.create("tinted_glass");

    // In model space (the model as made = facing east). The frame is 4px thick (x 6-10).
    private static final CBPixelBox PANEL = new CBPixelBox(6, 0, 0, 10, 16, 16);
    private static final CBPixelBox FRAME_UP = new CBPixelBox(6, 14, 0, 10, 16, 16);
    private static final CBPixelBox FRAME_DOWN = new CBPixelBox(6, 0, 0, 10, 2, 16);
    private static final CBPixelBox FRAME_LEFT = new CBPixelBox(6, 0, 0, 10, 16, 2);
    private static final CBPixelBox FRAME_RIGHT = new CBPixelBox(6, 0, 14, 10, 16, 16);

    // The 2x2 corner pieces; datagen builds the corner models from these same boxes
    public static final CBPixelBox CORNER_UP_LEFT = new CBPixelBox(6, 14, 0, 10, 16, 2);
    public static final CBPixelBox CORNER_UP_RIGHT = new CBPixelBox(6, 14, 14, 10, 16, 16);
    public static final CBPixelBox CORNER_DOWN_LEFT = new CBPixelBox(6, 0, 0, 10, 2, 2);
    public static final CBPixelBox CORNER_DOWN_RIGHT = new CBPixelBox(6, 0, 14, 10, 2, 16);

    private static final VoxelShape SHAPE_EAST = PANEL.toShape();
    private static final VoxelShape SHAPE_NORTH = PANEL.rotateY(1).toShape();

    public CBTintableWindow(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UP, false).setValue(DOWN, false).setValue(LEFT, false).setValue(RIGHT, false)
                .setValue(UP_LEFT, false).setValue(UP_RIGHT, false).setValue(DOWN_LEFT, false).setValue(DOWN_RIGHT, false)
                .setValue(TINTED_GLASS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT, TINTED_GLASS);
    }

    // ---- Directions ----

    // Facing north is the model turned a quarter (y 90); facing east is the model as made
    public static int yTurns(Direction facing)
    {
        return facing == Direction.NORTH ? 1 : 0;
    }

    public static Direction leftOf(Direction facing)
    {
        return facing == Direction.EAST ? Direction.NORTH : Direction.EAST;
    }

    public static Direction rightOf(Direction facing)
    {
        return leftOf(facing).getOpposite();
    }

    // ---- Shape ----

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return state.getValue(FACING) == Direction.NORTH ? SHAPE_NORTH : SHAPE_EAST;
    }

    // ---- Placement and joining ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Face the player; looking north or south both give a north-facing window
        Direction facing = context.getHorizontalDirection().getAxis() == Direction.Axis.X ? Direction.EAST : Direction.NORTH;

        // Clicked on another window: match it, so extending a window just works
        if (!context.replacingClickedOnBlock())
        {
            BlockState against = level.getBlockState(pos.relative(context.getClickedFace().getOpposite()));
            if (against.getBlock() instanceof CBTintableWindow)
            {
                facing = against.getValue(FACING);
            }
        }
        return withConnections(defaultBlockState().setValue(FACING, facing), level, pos);
    }

    // A neighbour changed: re-check which edges are joined and which corners need filling
    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        return withConnections(state, level, pos);
    }

    private static BlockState withConnections(BlockState state, LevelReader level, BlockPos pos)
    {
        Direction facing = state.getValue(FACING);
        Direction left = leftOf(facing);
        Direction right = rightOf(facing);

        boolean up = joinsTo(facing, level, pos, Direction.UP);
        boolean down = joinsTo(facing, level, pos, Direction.DOWN);
        boolean joinedLeft = joinsTo(facing, level, pos, left);
        boolean joinedRight = joinsTo(facing, level, pos, right);

        return state
                .setValue(UP, up)
                .setValue(DOWN, down)
                .setValue(LEFT, joinedLeft)
                .setValue(RIGHT, joinedRight)
                // A corner needs filling when both edges beside it are joined but the window diagonally between them isn't there
                .setValue(UP_LEFT, up && joinedLeft && !joinsTo(facing, level, pos.above(), left))
                .setValue(UP_RIGHT, up && joinedRight && !joinsTo(facing, level, pos.above(), right))
                .setValue(DOWN_LEFT, down && joinedLeft && !joinsTo(facing, level, pos.below(), left))
                .setValue(DOWN_RIGHT, down && joinedRight && !joinsTo(facing, level, pos.below(), right));
    }

    // Is there a window with the same facing on that side of pos?
    private static boolean joinsTo(Direction facing, LevelReader level, BlockPos pos, Direction side)
    {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        return neighbor.getBlock() instanceof CBTintableWindow && neighbor.getValue(FACING) == facing;
    }

    // ---- Tint regions ----

    @Override
    public int getTintRegionCount()
    {
        return 2;
    }

    // Clicking any frame strip or corner piece dyes the frame; anywhere else dyes the glass
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        int turns = yTurns(state.getValue(FACING));
        for (CBPixelBox frame : frameBoxes(state))
        {
            if (frame.rotateY(turns).contains(pixel)) return FRAME_REGION;
        }
        return GLASS_REGION;
    }

    private static List<CBPixelBox> frameBoxes(BlockState state)
    {
        List<CBPixelBox> boxes = new ArrayList<>();
        if (!state.getValue(UP)) boxes.add(FRAME_UP);
        if (!state.getValue(DOWN)) boxes.add(FRAME_DOWN);
        if (!state.getValue(LEFT)) boxes.add(FRAME_LEFT);
        if (!state.getValue(RIGHT)) boxes.add(FRAME_RIGHT);
        if (state.getValue(UP_LEFT)) boxes.add(CORNER_UP_LEFT);
        if (state.getValue(UP_RIGHT)) boxes.add(CORNER_UP_RIGHT);
        if (state.getValue(DOWN_LEFT)) boxes.add(CORNER_DOWN_LEFT);
        if (state.getValue(DOWN_RIGHT)) boxes.add(CORNER_DOWN_RIGHT);
        return boxes;
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        // The first dye on the glass swaps the clear glass texture for the tintable one.
        // Same block, so the block entity (and the frame's colour) is kept.
        if (!level.isClientSide() && DyeColor.getColor(stack) != null && !state.getValue(TINTED_GLASS)
                && getTintRegion(state, hitResult) == GLASS_REGION)
        {
            level.setBlock(pos, state.setValue(TINTED_GLASS, true), Block.UPDATE_ALL);
        }
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    // Left/right are relative to the facing, so some turns swap which neighbour counts as left
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        boolean facingEast = state.getValue(FACING) == Direction.EAST;
        return switch (rotation)
        {
            case NONE -> state;
            case CLOCKWISE_90 -> facingEast
                    ? state.setValue(FACING, Direction.NORTH)
                    : swapSides(state.setValue(FACING, Direction.EAST));
            case COUNTERCLOCKWISE_90 -> facingEast
                    ? swapSides(state.setValue(FACING, Direction.NORTH))
                    : state.setValue(FACING, Direction.EAST);
            case CLOCKWISE_180 -> swapSides(state);
        };
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        boolean facingEast = state.getValue(FACING) == Direction.EAST;
        return switch (mirror)
        {
            case NONE -> state;
            case LEFT_RIGHT -> facingEast ? swapSides(state) : state; // flips north/south
            case FRONT_BACK -> facingEast ? state : swapSides(state); // flips east/west
        };
    }

    // Swaps everything on the left with everything on the right, corners included
    private static BlockState swapSides(BlockState state)
    {
        return state
                .setValue(LEFT, state.getValue(RIGHT)).setValue(RIGHT, state.getValue(LEFT))
                .setValue(UP_LEFT, state.getValue(UP_RIGHT)).setValue(UP_RIGHT, state.getValue(UP_LEFT))
                .setValue(DOWN_LEFT, state.getValue(DOWN_RIGHT)).setValue(DOWN_RIGHT, state.getValue(DOWN_LEFT));
    }
}