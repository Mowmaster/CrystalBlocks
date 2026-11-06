package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.block.HorizontalFacing;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.tint.ITintHolder;
import com.mowmaster.mowlib.util.CBPixelBox;

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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Where an upright 8x8 beam meets two level ones (models/block/corner_beam.json), one per block set.
// As modelled: a full-height post in the south-west quarter, with arms along the bottom running north and east.
// - FACING: the first arm's direction; the second arm is clockwise from it (north -> arms north and east)
// - HALF: bottom = arms along the bottom (as modelled), top = flipped upside down (blockstate x 180)
// Placing it picks the orientation that lines up with the most neighbouring square beams and columns; beams placed
// against its ends line up with it (CBBeamConnector). Colours pass both ways when placed against each other.
public class CBCornerBeam extends Block implements IColorableBlock, CBBeamConnector
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    // The model's boxes, as modelled
    private static final CBPixelBox POST = new CBPixelBox(0, 0, 8, 8, 16, 16);
    private static final CBPixelBox NORTH_ARM = new CBPixelBox(0, 0, 0, 8, 8, 8);
    private static final CBPixelBox EAST_ARM = new CBPixelBox(8, 0, 8, 16, 8, 16);
    private static final List<CBPixelBox> BOXES = List.of(POST, NORTH_ARM, EAST_ARM);

    // Which box reaches each open side, as modelled. Map.of keeps them in a fixed, read-only map.
    private static final Map<Direction, CBPixelBox> ENDS = Map.of(
            Direction.UP, POST,
            Direction.DOWN, POST,
            Direction.NORTH, NORTH_ARM,
            Direction.EAST, EAST_ARM);

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public CBCornerBeam(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            shapes.put(state, CBPixelBox.union(BOXES.stream().map(box -> place(box, state)).toList()));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HALF);
    }

    // ---- Rotations, shared with datagen ----

    // Blockstate x: upside down for the top half
    public static int xQuarterTurns(BlockState state)
    {
        return state.getValue(HALF) == Half.TOP ? 2 : 0;
    }

    // Blockstate y. Flipping upside down moves the north arm to the south, leaving arms south and east;
    // east is then the first arm, so one quarter turn less brings it round to the facing.
    public static int yQuarterTurns(BlockState state)
    {
        int turns = HorizontalFacing.quarterTurns(state.getValue(FACING));
        return state.getValue(HALF) == Half.TOP ? Math.floorMod(turns - 1, 4) : turns;
    }

    // A model box moved to where it is in this state (same order as the blockstate: x first, then y)
    private static CBPixelBox place(CBPixelBox box, BlockState state)
    {
        return box.rotateX(xQuarterTurns(state)).rotateY(yQuarterTurns(state));
    }

    // Which world side a side of the model faces in this state
    private static Direction worldSide(Direction modelSide, BlockState state)
    {
        Direction side = modelSide;
        // One rotateX turn: north -> down -> south -> up
        for (int i = 0; i < xQuarterTurns(state); i++)
        {
            side = switch (side)
            {
                case NORTH -> Direction.DOWN;
                case DOWN -> Direction.SOUTH;
                case SOUTH -> Direction.UP;
                case UP -> Direction.NORTH;
                default -> side;
            };
        }
        for (int i = 0; i < yQuarterTurns(state); i++)
        {
            if (side.getAxis().isHorizontal()) side = side.getClockWise();
        }
        return side;
    }

    // The end of the corner on a world side (in this block's pixels), or null if that side has no end
    @Nullable
    public CBPixelBox endBox(BlockState state, Direction side)
    {
        for (Map.Entry<Direction, CBPixelBox> end : ENDS.entrySet())
        {
            if (worldSide(end.getKey(), state) == side) return place(end.getValue(), state);
        }
        return null;
    }

    // Only the plain 8x8 beams (columns, support beams and their caps) attach to the ends
    @Override
    public @Nullable CBPixelBox connectionPoint(BlockState state, Direction side, CBBoltedBeamBlock connecting)
    {
        return connecting.kind() == CBBoltedBeamBlock.Kind.SQUARE ? endBox(state, side) : null;
    }

    // ---- Shape ----

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    // ---- Placement ----

    // Every orientation scores 1 per end that lines up with a neighbouring square beam (or another corner's end).
    // The orientation the player is aiming for gets a small bonus, so with nothing around (or a tie) it wins:
    // the first arm points the way the player faces, and it's upside down when placed on a block's underside or
    // the upper half of a side.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        boolean top = clickedFace == Direction.DOWN
                || (clickedFace != Direction.UP && context.getClickLocation().y - pos.getY() > 0.5);
        BlockState aimed = defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(HALF, top ? Half.TOP : Half.BOTTOM);

        BlockState best = aimed;
        double bestScore = -1;
        for (BlockState candidate : stateDefinition.getPossibleStates())
        {
            double score = connections(level, pos, candidate) + (candidate == aimed ? 0.5 : 0);
            if (score > bestScore)
            {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    // How many of this state's ends line up with a neighbour
    private int connections(BlockGetter level, BlockPos pos, BlockState state)
    {
        int count = 0;
        for (Direction side : Direction.values())
        {
            if (connectedNeighbor(level, pos, state, side) != null) count++;
        }
        return count;
    }

    // The neighbour on this side if this state has an end there and the neighbour lines up with it exactly:
    // a square beam running along that side's axis, or another corner beam's end facing back. Otherwise null.
    @Nullable
    private BlockPos connectedNeighbor(BlockGetter level, BlockPos pos, BlockState state, Direction side)
    {
        CBPixelBox end = endBox(state, side);
        if (end == null) return null;

        BlockPos neighborPos = pos.relative(side);
        BlockState neighbor = level.getBlockState(neighborPos);
        CBPixelBox other = null;
        if (neighbor.getBlock() instanceof CBBoltedBeamBlock beam && beam.kind() == CBBoltedBeamBlock.Kind.SQUARE
                && beam.facing(neighbor).getAxis() == side.getAxis())
        {
            other = beam.worldFootprint(neighbor);
        }
        else if (neighbor.getBlock() instanceof CBCornerBeam corner)
        {
            other = corner.endBox(neighbor, side.getOpposite());
        }
        // crossSectionDifference ignores the length axis, so the two blocks' own pixel spaces compare directly
        boolean linedUp = other != null && CBBoltedBeamBlock.crossSectionDifference(end, other, side.getAxis()) < 0.01;
        return linedUp ? neighborPos : null;
    }

    // Placed against a coloured beam or corner it lines up with: take that colour, unless the item already gave one
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof ITintHolder placed) || placed.isDyed(0)) return;

        for (Direction side : Direction.values())
        {
            BlockPos neighborPos = connectedNeighbor(level, pos, state, side);
            if (neighborPos != null && level.getBlockEntity(neighborPos) instanceof ITintHolder source && source.isDyed(0))
            {
                placed.setColor(0, source.getColor(0));
                return;
            }
        }
    }

    // ---- Rotation and mirroring (structures, the wrench's Y axis) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // Mirrored, the second arm ends up counter-clockwise of the first, so the mirrored second arm becomes the facing
    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        if (mirror == Mirror.NONE) return state;
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING).getClockWise()));
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}
