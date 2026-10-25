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
import net.minecraft.world.level.LevelReader;
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
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Base for beams built from a hand-made model.
// - Model base rotation: models that aren't made running up Y (e.g. the chain) are turned onto Y first
// - Position: offset_x / offset_z place the main body anywhere in the block, in whole-pixel steps
// - Turn: quarter turns around the beam's own length (2 or 4 allowed, per block)
// - Plain beams have an axis (placed out of the clicked face like logs); caps have a 6-way facing whose top
//   always points at the surface they were placed against
// Datagen makes one copy of the hand-made model per position and turn, moved with a NeoForge root transform,
// and rotates it for the facing in the blockstate. Shapes, clicks and placement all use the same maths.
public abstract class CBBoltedBeamBlock extends Block implements IColorableBlock
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    // Quarter turns around the beam's length, for blocks that allow 2 or 4 of them
    public static final IntegerProperty TURN_2 = IntegerProperty.create("turn", 0, 1);
    public static final IntegerProperty TURN_4 = IntegerProperty.create("turn", 0, 3);

    // Beams and caps of the same kind (same cross-section) line up with each other
    public enum Kind { SQUARE, I_BEAM, CHAIN, CHAIN_MEDIUM, CHAIN_LARGE }

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    protected CBBoltedBeamBlock(Properties properties)
    {
        super(properties);

        CBPixelBox body = standardFootprint();
        BlockState defaults = stateDefinition.any()
                .setValue(offsetXProperty(), (int) Math.floor(body.minX()))
                .setValue(offsetZProperty(), (int) Math.floor(body.minZ()));
        if (!alwaysUpright()) defaults = isCap() ? defaults.setValue(FACING, Direction.UP) : defaults.setValue(AXIS, Direction.Axis.Y);
        if (turnProperty() != null) defaults = defaults.setValue(turnProperty(), 0);
        registerDefaultState(defaults);

        List<CBPixelBox> allBoxes = regionBoxes().stream().flatMap(List::stream).toList();
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            shapes.put(state, CBPixelBox.union(allBoxes.stream().map(box -> place(box, state)).toList()));
        }
    }

    // ---- What each beam provides (always constants, so they're safe to use while the block is being built) ----

    public abstract Kind kind();

    public abstract boolean isCap();

    // The model's element boxes per tint region, as modelled
    protected abstract List<List<CBPixelBox>> regionBoxes();

    // The main body's cross-section as modelled; it decides where the beam can sit
    protected abstract CBPixelBox footprint();

    public abstract IntegerProperty offsetXProperty();

    public abstract IntegerProperty offsetZProperty();

    // How many quarter turns around its length the beam allows: 1 (none), 2 or 4
    public int turnCount()
    {
        return 1;
    }

    // Quarter turns around X that bring the model onto the Y axis with its top up (CBPixelBox.rotateX direction).
    // 0 for models made running up Y; 3 for a model made running north-south with its top at the north end.
    public int modelXTurns()
    {
        return 0;
    }

    // Blocks that only ever stand upright (e.g. lanterns) have no facing or axis property at all.
    // facing() then always reports UP.
    public boolean alwaysUpright()
    {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(offsetXProperty(), offsetZProperty());
        if (!alwaysUpright())
        {
            if (isCap()) builder.add(FACING);
            else builder.add(AXIS);
        }
        if (turnProperty() != null) builder.add(turnProperty());
    }

    // ---- Reading a state ----

    // Which way the model's top points. Plain beams have an axis instead: up, north or east stand in for it.
    public Direction facing(BlockState state)
    {
        if (alwaysUpright()) return Direction.UP;
        if (isCap()) return state.getValue(FACING);
        return switch (state.getValue(AXIS))
        {
            case X -> Direction.EAST;
            case Z -> Direction.NORTH;
            default -> Direction.UP;
        };
    }

    // Points the beam: caps keep the exact direction, plain beams only keep its axis
    protected BlockState withFacing(BlockState state, Direction facing)
    {
        if (alwaysUpright()) return state;
        return isCap() ? state.setValue(FACING, facing) : state.setValue(AXIS, facing.getAxis());
    }

    @Nullable
    public IntegerProperty turnProperty()
    {
        return switch (turnCount())
        {
            case 2 -> TURN_2;
            case 4 -> TURN_4;
            default -> null;
        };
    }

    public int turn(BlockState state)
    {
        IntegerProperty property = turnProperty();
        return property == null ? 0 : state.getValue(property);
    }

    public int offsetX(BlockState state)
    {
        return state.getValue(offsetXProperty());
    }

    public int offsetZ(BlockState state)
    {
        return state.getValue(offsetZProperty());
    }

    // ---- Positions ----

    // The footprint once the model's base rotation has put it upright
    public CBPixelBox standardFootprint()
    {
        return footprint().rotateX(modelXTurns());
    }

    // Offsets move the body in whole pixels from where it was modelled, so a body whose edge sits on a fraction
    // of a pixel (like the chain at 6.75) keeps that fraction at every position
    public int maxOffsetX()
    {
        CBPixelBox body = standardFootprint();
        return (int) Math.floor(16 - (body.maxX() - body.minX()) - (body.minX() - Math.floor(body.minX())));
    }

    public int maxOffsetZ()
    {
        CBPixelBox body = standardFootprint();
        return (int) Math.floor(16 - (body.maxZ() - body.minZ()) - (body.minZ() - Math.floor(body.minZ())));
    }

    // The lowest offsets a beam may use. Normally 0; a beam that must stay centred along one axis raises it.
    public int minOffsetX()
    {
        return 0;
    }

    public int minOffsetZ()
    {
        return 0;
    }

    // ---- Transforms, shared with datagen ----

    // How far the model moves from where it was modelled, in pixels, turned along with it.
    // Datagen writes this into each model copy's root transform.
    public Vec3 modelTranslation(int offsetX, int offsetZ, int turn)
    {
        CBPixelBox body = standardFootprint();
        double dx = offsetX - Math.floor(body.minX());
        double dz = offsetZ - Math.floor(body.minZ());
        for (int i = 0; i < Math.floorMod(turn, 4); i++)
        {
            double oldX = dx;
            dx = -dz;
            dz = oldX;
        }
        return new Vec3(dx, 0, dz);
    }

    // The root transform rotation for a model copy: the base rotation first, then the turns around Y.
    // JOML's -90 degrees matches one CBPixelBox.rotateX / rotateY quarter turn.
    public Quaternionf rootRotation(int turns)
    {
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-90.0 * turns))
                .rotateX((float) Math.toRadians(-90.0 * modelXTurns()));
    }

    // Blockstate rotations that point the model's top at the facing: up as modelled, down = x 180,
    // sideways = x 90 (top to north) then y to the right side
    public int xQuarterTurns(BlockState state)
    {
        return switch (facing(state))
        {
            case UP -> 0;
            case DOWN -> 2;
            default -> 1;
        };
    }

    public int yQuarterTurns(BlockState state)
    {
        Direction facing = facing(state);
        return facing.getAxis().isVertical() ? 0 : CBPixelBox.quarterTurns(facing);
    }

    // A model box moved to where it really is in this state: base rotation, turn, move, then the blockstate rotations
    protected CBPixelBox place(CBPixelBox box, BlockState state)
    {
        int turn = turn(state);
        Vec3 translation = modelTranslation(offsetX(state), offsetZ(state), turn);
        return box.rotateX(modelXTurns())
                .rotateY(turn)
                .offset(translation.x, translation.y, translation.z)
                .rotateX(xQuarterTurns(state))
                .rotateY(yQuarterTurns(state));
    }

    // Where the main body actually is in the block
    public CBPixelBox worldFootprint(BlockState state)
    {
        return place(footprint(), state);
    }

    // Which world direction a side of the model (as modelled) faces in this state
    public Direction worldDirection(Direction modelSide, BlockState state)
    {
        Direction direction = modelSide;
        for (int i = 0; i < modelXTurns(); i++) direction = turnX(direction);
        for (int i = 0; i < turn(state); i++) direction = turnY(direction);
        for (int i = 0; i < xQuarterTurns(state); i++) direction = turnX(direction);
        for (int i = 0; i < yQuarterTurns(state); i++) direction = turnY(direction);
        return direction;
    }

    // Which world direction a side of the beam faces once the facing is applied. Sides are "as the beam stands
    // upright after its turn" (the turn and base rotation are not applied here).
    public Direction standardToWorld(Direction side, BlockState state)
    {
        Direction direction = side;
        for (int i = 0; i < xQuarterTurns(state); i++) direction = turnX(direction);
        for (int i = 0; i < yQuarterTurns(state); i++) direction = turnY(direction);
        return direction;
    }

    // A spot on this beam that another kind of beam can attach to on the given world side (e.g. a chained
    // support beam's loop, for chains). Returns that spot's box in this block's pixels, or null if there isn't one.
    @Nullable
    public CBPixelBox connectionPoint(BlockState state, Direction side, CBBoltedBeamBlock connecting)
    {
        return null;
    }

    // One CBPixelBox.rotateX quarter turn: north -> down -> south -> up -> north
    private static Direction turnX(Direction direction)
    {
        return switch (direction)
        {
            case NORTH -> Direction.DOWN;
            case DOWN -> Direction.SOUTH;
            case SOUTH -> Direction.UP;
            case UP -> Direction.NORTH;
            default -> direction;
        };
    }

    // One CBPixelBox.rotateY quarter turn: north -> east -> south -> west
    private static Direction turnY(Direction direction)
    {
        return direction.getAxis().isVertical() ? direction : direction.getClockWise();
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

    // ---- Tint regions ----

    @Override
    public int getTintRegionCount()
    {
        return regionBoxes().size();
    }

    // Dye whichever part was clicked; higher regions (the smaller details) are checked first
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        List<List<CBPixelBox>> regions = regionBoxes();
        for (int region = regions.size() - 1; region >= 0; region--)
        {
            for (CBPixelBox box : regions.get(region))
            {
                if (place(box, state).contains(pixel)) return region;
            }
        }
        return 0;
    }

    // ---- Placement ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Caps point their top at the surface they were placed against; beams run out of the clicked face, like logs
        Direction clickedFace = context.getClickedFace();
        Direction facing = isCap() ? clickedFace.getOpposite() : clickedFace;
        BlockState state = withFacing(defaultBlockState(), facing);
        // Placed against another beam's connection point (e.g. a chain against a chained support's loop):
        // line up with that point
        if (!context.replacingClickedOnBlock() && facing.getAxis() == clickedFace.getAxis())
        {
            BlockState clicked = level.getBlockState(pos.relative(clickedFace.getOpposite()));
            if (clicked.getBlock() instanceof CBBoltedBeamBlock other)
            {
                CBPixelBox point = other.connectionPoint(clicked, clickedFace, this);
                if (point != null) return bestFit(state, point, true, turn(state));
            }
        }

        // Next to a beam of the same kind along this one's length: line up with it exactly, keeping its turn
        BlockPos neighborPos = alignedNeighbor(level, pos, state, clickedFace.getOpposite());
        if (neighborPos != null)
        {
            BlockState neighbor = level.getBlockState(neighborPos);
            CBBoltedBeamBlock other = (CBBoltedBeamBlock) neighbor.getBlock();
            return bestFit(state, other.worldFootprint(neighbor), true, other.turn(neighbor));
        }

        // Otherwise: upright beams turn to match the way the player is looking...
        if (turnProperty() != null && facing.getAxis().isVertical())
        {
            state = state.setValue(turnProperty(), CBPixelBox.quarterTurns(context.getHorizontalDirection()) % turnCount());
        }
        // ...and centre on the clicked pixel (as close as the block allows)
        Vec3 click = context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)).scale(16);
        return bestFit(state, new CBPixelBox(click.x, click.y, click.z, click.x, click.y, click.z), false, turn(state));
    }

    // A beam or cap of the same kind directly before or after this one along its length, or null.
    // preferredSide (if given and lined up) is checked first, so the block that was clicked wins.
    @Nullable
    protected BlockPos alignedNeighbor(LevelReader level, BlockPos pos, BlockState state, @Nullable Direction preferredSide)
    {
        Direction.Axis axis = facing(state).getAxis();
        List<Direction> sides = new ArrayList<>();
        if (preferredSide != null && preferredSide.getAxis() == axis) sides.add(preferredSide);
        sides.add(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE));
        sides.add(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));

        for (Direction side : sides)
        {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() instanceof CBBoltedBeamBlock other && other.kind() == kind()
                    && other.facing(neighbor).getAxis() == axis)
            {
                return neighborPos;
            }
        }
        return null;
    }

    // Tries every position (and every turn, if allowed) and keeps the one whose body best matches the target
    // across the beam's length. Ties go to preferredTurn, so square beams keep the turn they should have.
    // Used for clicks, stacking, rotating and mirroring.
    BlockState bestFit(BlockState state, CBPixelBox target, boolean tryAllTurns, int preferredTurn)
    {
        Direction.Axis lengthAxis = facing(state).getAxis();
        int preferred = Math.floorMod(preferredTurn, turnCount());
        int firstTurn = tryAllTurns ? 0 : turn(state);
        int lastTurn = tryAllTurns ? turnCount() - 1 : turn(state);

        BlockState best = state;
        double bestDifference = Double.MAX_VALUE;
        for (int turn = firstTurn; turn <= lastTurn; turn++)
        {
            for (int x = minOffsetX(); x <= maxOffsetX(); x++)
            {
                for (int z = minOffsetZ(); z <= maxOffsetZ(); z++)
                {
                    BlockState candidate = state.setValue(offsetXProperty(), x).setValue(offsetZProperty(), z);
                    if (turnProperty() != null) candidate = candidate.setValue(turnProperty(), turn);

                    double difference = crossSectionDifference(worldFootprint(candidate), target, lengthAxis)
                            + (turn == preferred ? 0 : 0.001);
                    if (difference < bestDifference)
                    {
                        bestDifference = difference;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    // How different two boxes are across the beam: centre distance plus size difference, ignoring the length axis
    protected static double crossSectionDifference(CBPixelBox a, CBPixelBox b, Direction.Axis lengthAxis)
    {
        double difference = 0;
        for (Direction.Axis axis : Direction.Axis.values())
        {
            if (axis == lengthAxis) continue;
            double centre = (a.min(axis) + a.max(axis)) / 2 - (b.min(axis) + b.max(axis)) / 2;
            double size = (a.max(axis) - a.min(axis)) - (b.max(axis) - b.min(axis));
            difference += centre * centre + size * size;
        }
        return difference;
    }

    // Stacked onto a matching beam: copy its colours, unless the placed item already gave this one a colour
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity placed)) return;
        for (int region = 0; region < getTintRegionCount(); region++)
        {
            if (placed.isDyed(region)) return;
        }

        // Copy from whichever lined-up neighbour this block matches exactly (the one it was placed against)
        Direction.Axis axis = facing(state).getAxis();
        for (Direction side : new Direction[] {
                Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE),
                Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE) })
        {
            BlockPos neighborPos = alignedNeighbor(level, pos, state, side);
            if (neighborPos == null || !neighborPos.equals(pos.relative(side))) continue;

            BlockState neighbor = level.getBlockState(neighborPos);
            CBBoltedBeamBlock other = (CBBoltedBeamBlock) neighbor.getBlock();
            boolean linedUp = crossSectionDifference(worldFootprint(state), other.worldFootprint(neighbor), axis) < 0.01;

            if (linedUp && level.getBlockEntity(neighborPos) instanceof CBTintBlockEntity source)
            {
                // Different beams can have different numbers of regions; copy the ones both have
                int regions = Math.min(getTintRegionCount(), other.getTintRegionCount());
                for (int region = 0; region < regions; region++)
                {
                    if (source.isDyed(region)) placed.setColor(region, source.getColor(region));
                }
                return;
            }
        }
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    // Turn the facing, then find the position whose body sits where the old body ends up after turning.
    // An upright beam also turns around its own length by the same amount.
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        if (rotation == Rotation.NONE) return state;
        int quarterTurns = switch (rotation)
        {
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
            default -> 0;
        };
        BlockState rotated = withFacing(state, rotation.rotate(facing(state)));
        int preferredTurn = facing(state).getAxis().isVertical() ? turn(state) + quarterTurns : turn(state);
        return bestFit(rotated, worldFootprint(state).rotateY(quarterTurns), true, preferredTurn);
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        if (mirror == Mirror.NONE) return state;
        CBPixelBox body = worldFootprint(state);
        CBPixelBox mirrored = mirror == Mirror.LEFT_RIGHT
                // flips north/south
                ? new CBPixelBox(body.minX(), body.minY(), 16 - body.maxZ(), body.maxX(), body.maxY(), 16 - body.minZ())
                // flips east/west
                : new CBPixelBox(16 - body.maxX(), body.minY(), body.minZ(), 16 - body.minX(), body.maxY(), body.maxZ());
        BlockState flipped = withFacing(state, mirror.mirror(facing(state)));
        return bestFit(flipped, mirrored, true, turn(state));
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}