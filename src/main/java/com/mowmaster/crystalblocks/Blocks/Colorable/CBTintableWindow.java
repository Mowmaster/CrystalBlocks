package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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

    // The shape of an L-shaped corner window, named by the two directions its arms go.
    // The models are made for NORTH_WEST and turned by yTurns for the others.
    // firstArm / secondArm are where the base model's west / north arms end up after turning.
    public enum BendType implements StringRepresentable
    {
        NONE("none", null, null, 0),
        NORTH_WEST("north_west", Direction.WEST, Direction.NORTH, 0),
        NORTH_EAST("north_east", Direction.NORTH, Direction.EAST, 1),
        SOUTH_EAST("south_east", Direction.EAST, Direction.SOUTH, 2),
        SOUTH_WEST("south_west", Direction.SOUTH, Direction.WEST, 3);

        private final String name;
        private final Direction firstArm;
        private final Direction secondArm;
        private final int yTurns;

        BendType(String name, Direction firstArm, Direction secondArm, int yTurns)
        {
            this.name = name;
            this.firstArm = firstArm;
            this.secondArm = secondArm;
            this.yTurns = yTurns;
        }

        // The name shown in F3 and written in the blockstate file
        @Override
        public @NotNull String getSerializedName()
        {
            return name;
        }

        public Direction firstArm() { return firstArm; }
        public Direction secondArm() { return secondArm; }
        public int yTurns() { return yTurns; }

        public boolean hasArm(Direction direction)
        {
            return direction == firstArm || direction == secondArm;
        }

        public static BendType withArms(Direction a, Direction b)
        {
            for (BendType bend : values())
            {
                if (bend != NONE && bend.hasArm(a) && bend.hasArm(b)) return bend;
            }
            return NONE;
        }
    }

    // The side the window is looked at from. South and west look identical, so only north and east exist.
    // Ignored while the window is bent.
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.NORTH, Direction.EAST);
    public static final EnumProperty<BendType> BEND = EnumProperty.create("bend", BendType.class);

    // Joined to another window on that edge. For straight windows, left/right are the model's north/south edges
    // (facing east -> left = north, right = south; facing north -> left = east, right = west).
    // For bends, left/right are the ends of the first/second arm and are always joined.
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

    // ---- Straight window boxes, in model space (the model as made = facing east). The frame is 4px thick (x 6-10). ----
    private static final CBPixelBox PANEL = new CBPixelBox(6, 0, 0, 10, 16, 16);
    private static final CBPixelBox FRAME_UP = new CBPixelBox(6, 14, 0, 10, 16, 16);
    private static final CBPixelBox FRAME_DOWN = new CBPixelBox(6, 0, 0, 10, 2, 16);
    private static final CBPixelBox FRAME_LEFT = new CBPixelBox(6, 0, 0, 10, 16, 2);
    private static final CBPixelBox FRAME_RIGHT = new CBPixelBox(6, 0, 14, 10, 16, 16);
    public static final CBPixelBox CORNER_UP_LEFT = new CBPixelBox(6, 14, 0, 10, 16, 2);
    public static final CBPixelBox CORNER_UP_RIGHT = new CBPixelBox(6, 14, 14, 10, 16, 16);
    public static final CBPixelBox CORNER_DOWN_LEFT = new CBPixelBox(6, 0, 0, 10, 2, 2);
    public static final CBPixelBox CORNER_DOWN_RIGHT = new CBPixelBox(6, 0, 14, 10, 2, 16);

    // ---- Bend boxes, in model space for NORTH_WEST: the west arm (z 6-10) and the north arm (x 6-10) ----
    // The west arm also covers the centre square so the two arms meet cleanly
    public static final CBPixelBox BEND_WEST_PANEL = new CBPixelBox(0, 0, 6, 10, 16, 10);
    public static final CBPixelBox BEND_NORTH_PANEL = new CBPixelBox(6, 0, 0, 10, 16, 6);
    public static final CBPixelBox BEND_TOP_WEST = new CBPixelBox(0, 14, 6, 10, 16, 10);
    public static final CBPixelBox BEND_TOP_NORTH = new CBPixelBox(6, 14, 0, 10, 16, 6);
    public static final CBPixelBox BEND_BOTTOM_WEST = new CBPixelBox(0, 0, 6, 10, 2, 10);
    public static final CBPixelBox BEND_BOTTOM_NORTH = new CBPixelBox(6, 0, 0, 10, 2, 6);
    // 2x2 pieces at the far end of each arm
    public static final CBPixelBox BEND_FIRST_UP = new CBPixelBox(0, 14, 6, 2, 16, 10);
    public static final CBPixelBox BEND_FIRST_DOWN = new CBPixelBox(0, 0, 6, 2, 2, 10);
    public static final CBPixelBox BEND_SECOND_UP = new CBPixelBox(6, 14, 0, 10, 16, 2);
    public static final CBPixelBox BEND_SECOND_DOWN = new CBPixelBox(6, 0, 0, 10, 2, 2);

    private static final VoxelShape SHAPE_EAST = PANEL.toShape();
    private static final VoxelShape SHAPE_NORTH = PANEL.rotateY(1).toShape();
    private static final VoxelShape[] BEND_SHAPES = new VoxelShape[4];

    static
    {
        for (int turns = 0; turns < 4; turns++)
        {
            BEND_SHAPES[turns] = CBPixelBox.union(List.of(BEND_WEST_PANEL.rotateY(turns), BEND_NORTH_PANEL.rotateY(turns)));
        }
    }

    public CBTintableWindow(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(BEND, BendType.NONE)
                .setValue(UP, false).setValue(DOWN, false).setValue(LEFT, false).setValue(RIGHT, false)
                .setValue(UP_LEFT, false).setValue(UP_RIGHT, false).setValue(DOWN_LEFT, false).setValue(DOWN_RIGHT, false)
                .setValue(TINTED_GLASS, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, BEND, UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT, TINTED_GLASS);
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

    // A straight window facing north runs east-west (along X); facing east it runs north-south (along Z)
    private static Direction.Axis runsAlong(Direction facing)
    {
        return facing == Direction.NORTH ? Direction.Axis.X : Direction.Axis.Z;
    }

    // ---- Shape ----

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        BendType bend = state.getValue(BEND);
        if (bend != BendType.NONE) return BEND_SHAPES[bend.yTurns()];
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

        // Clicked on a straight window: match it, so extending a window just works
        if (!context.replacingClickedOnBlock())
        {
            BlockState against = level.getBlockState(pos.relative(context.getClickedFace().getOpposite()));
            if (against.getBlock() instanceof CBTintableWindow && against.getValue(BEND) == BendType.NONE)
            {
                facing = against.getValue(FACING);
            }
        }
        return withConnections(defaultBlockState().setValue(FACING, facing), level, pos);
    }

    // A neighbour changed: re-check the shape, which edges are joined and which corners need filling
    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        return withConnections(state, level, pos);
    }

    private static BlockState withConnections(BlockState state, LevelReader level, BlockPos pos)
    {
        BendType bend = findBend(level, pos);
        if (bend != BendType.NONE)
        {
            return withBendConnections(state.setValue(BEND, bend), level, pos, bend);
        }

        state = state.setValue(BEND, BendType.NONE);
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

    // A bend's arm ends are always joined (that's what made it a bend); above/below join only to the same kind of bend
    private static BlockState withBendConnections(BlockState state, LevelReader level, BlockPos pos, BendType bend)
    {
        boolean up = isBend(level, pos.above(), bend);
        boolean down = isBend(level, pos.below(), bend);
        return state
                .setValue(UP, up)
                .setValue(DOWN, down)
                .setValue(LEFT, true)
                .setValue(RIGHT, true)
                .setValue(UP_LEFT, up && !offersArm(level, pos.above(), bend.firstArm()))
                .setValue(UP_RIGHT, up && !offersArm(level, pos.above(), bend.secondArm()))
                .setValue(DOWN_LEFT, down && !offersArm(level, pos.below(), bend.firstArm()))
                .setValue(DOWN_RIGHT, down && !offersArm(level, pos.below(), bend.secondArm()));
    }

    // Bend when there's exactly one in-line window on an east/west side and exactly one on a north/south side
    private static BendType findBend(LevelReader level, BlockPos pos)
    {
        Direction xSide = null;
        Direction zSide = null;
        int xCount = 0;
        int zCount = 0;
        for (Direction side : Direction.Plane.HORIZONTAL)
        {
            if (!offersArm(level, pos, side)) continue;
            if (side.getAxis() == Direction.Axis.X)
            {
                xSide = side;
                xCount++;
            }
            else
            {
                zSide = side;
                zCount++;
            }
        }
        return xCount == 1 && zCount == 1 ? BendType.withArms(xSide, zSide) : BendType.NONE;
    }

    // Does the window on that side of pos line up with pos, so that pos could join to it?
    private static boolean offersArm(LevelReader level, BlockPos pos, Direction side)
    {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        if (!(neighbor.getBlock() instanceof CBTintableWindow)) return false;

        BendType neighborBend = neighbor.getValue(BEND);
        if (neighborBend != BendType.NONE) return neighborBend.hasArm(side.getOpposite());
        return runsAlong(neighbor.getValue(FACING)) == side.getAxis();
    }

    private static boolean isBend(LevelReader level, BlockPos pos, BendType bend)
    {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof CBTintableWindow && state.getValue(BEND) == bend;
    }

    // Can a straight window with this facing join to the window on that side of pos?
    private static boolean joinsTo(Direction facing, LevelReader level, BlockPos pos, Direction side)
    {
        BlockState neighbor = level.getBlockState(pos.relative(side));
        if (!(neighbor.getBlock() instanceof CBTintableWindow)) return false;

        BendType neighborBend = neighbor.getValue(BEND);
        if (neighborBend != BendType.NONE)
        {
            // A straight window joins a bend only through the end of one of its arms
            return side.getAxis().isHorizontal() && neighborBend.hasArm(side.getOpposite());
        }
        return neighbor.getValue(FACING) == facing;
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
        BendType bend = state.getValue(BEND);
        int turns = bend != BendType.NONE ? bend.yTurns() : yTurns(state.getValue(FACING));
        for (CBPixelBox frame : frameBoxes(state))
        {
            if (frame.rotateY(turns).contains(pixel)) return FRAME_REGION;
        }
        return GLASS_REGION;
    }

    // All frame boxes present in this state, in model space
    private static List<CBPixelBox> frameBoxes(BlockState state)
    {
        List<CBPixelBox> boxes = new ArrayList<>();
        if (state.getValue(BEND) != BendType.NONE)
        {
            if (!state.getValue(UP)) { boxes.add(BEND_TOP_WEST); boxes.add(BEND_TOP_NORTH); }
            if (!state.getValue(DOWN)) { boxes.add(BEND_BOTTOM_WEST); boxes.add(BEND_BOTTOM_NORTH); }
            if (state.getValue(UP_LEFT)) boxes.add(BEND_FIRST_UP);
            if (state.getValue(UP_RIGHT)) boxes.add(BEND_SECOND_UP);
            if (state.getValue(DOWN_LEFT)) boxes.add(BEND_FIRST_DOWN);
            if (state.getValue(DOWN_RIGHT)) boxes.add(BEND_SECOND_DOWN);
            return boxes;
        }

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

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        // Turning a bend turns both arms; the first arm stays the first arm, so left/right keep their meaning
        BendType bend = state.getValue(BEND);
        if (bend != BendType.NONE)
        {
            return state.setValue(BEND, BendType.withArms(rotation.rotate(bend.firstArm()), rotation.rotate(bend.secondArm())));
        }

        // Straight: left/right are relative to the facing, so some turns swap which neighbour counts as left
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
        // Mirroring a bend swaps which arm comes first
        BendType bend = state.getValue(BEND);
        if (bend != BendType.NONE)
        {
            if (mirror == Mirror.NONE) return state;
            return swapSides(state.setValue(BEND, BendType.withArms(mirror.mirror(bend.firstArm()), mirror.mirror(bend.secondArm()))));
        }

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