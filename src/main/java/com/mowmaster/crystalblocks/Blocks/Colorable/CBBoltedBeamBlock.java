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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Base for beams built from a hand-made model that runs up the Y axis, as modelled.
// - Position: offset_x / offset_z place the main body anywhere in the block, like the column
// - Turn (non-square beams): a quarter turn around the beam's own length
// - Caps: a 6-way facing; the model's top always points at the surface it was placed against
// Datagen makes one copy of the hand-made model per position and turn, moved with a NeoForge root transform,
// and rotates it for the facing in the blockstate. Shapes, clicks and placement all use the same maths.
public abstract class CBBoltedBeamBlock extends Block implements IColorableBlock
{
    // Which way the model's top points (caps only)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    // Turned a quarter around the beam's length (turnable beams only)
    public static final BooleanProperty TURNED = BooleanProperty.create("turned");
    // Which way a plain beam runs (beams look the same from both ends, so 3 values are enough)
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    // Beams and caps of the same kind (same cross-section size) line up with each other
    public enum Kind { SQUARE, I_BEAM }

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    protected CBBoltedBeamBlock(Properties properties)
    {
        super(properties);

        BlockState defaults = stateDefinition.any()
                .setValue(offsetXProperty(), (int) footprint().minX())
                .setValue(offsetZProperty(), (int) footprint().minZ());
        defaults = isCap() ? defaults.setValue(FACING, Direction.UP) : defaults.setValue(AXIS, Direction.Axis.Y);
        if (isTurnable()) defaults = defaults.setValue(TURNED, false);
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

    public abstract boolean isTurnable();

    // The model's element boxes per tint region, as modelled
    protected abstract List<List<CBPixelBox>> regionBoxes();

    // The main body's cross-section as modelled; it decides how far the beam can move
    protected abstract CBPixelBox footprint();

    public abstract IntegerProperty offsetXProperty();

    public abstract IntegerProperty offsetZProperty();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(offsetXProperty(), offsetZProperty());
        if (isCap()) builder.add(FACING);
        else builder.add(AXIS);
        if (isTurnable()) builder.add(TURNED);
    }

    // ---- Reading a state ----

    // Which way the model's top points. Plain beams have an axis instead: up, north or east stand in for it.
    public Direction facing(BlockState state)
    {
        if (isCap()) return state.getValue(FACING);
        return switch (state.getValue(AXIS))
        {
            case X -> Direction.EAST;
            case Z -> Direction.NORTH;
            default -> Direction.UP;
        };
    }

    // Points the beam: caps keep the exact direction, plain beams only keep its axis
    private BlockState withFacing(BlockState state, Direction facing)
    {
        return isCap() ? state.setValue(FACING, facing) : state.setValue(AXIS, facing.getAxis());
    }

    public boolean isTurned(BlockState state)
    {
        return isTurnable() && state.getValue(TURNED);
    }

    public int offsetX(BlockState state)
    {
        return state.getValue(offsetXProperty());
    }

    public int offsetZ(BlockState state)
    {
        return state.getValue(offsetZProperty());
    }

    public int maxOffsetX()
    {
        return 16 - (int) (footprint().maxX() - footprint().minX());
    }

    public int maxOffsetZ()
    {
        return 16 - (int) (footprint().maxZ() - footprint().minZ());
    }

    // ---- Transforms, shared with datagen ----

    // How far the model moves from where it was modelled, in pixels, turned along with it if the beam is turned.
    // Datagen writes this into each model copy's root transform.
    public Vec3 modelTranslation(int offsetX, int offsetZ, boolean turned)
    {
        double dx = offsetX - footprint().minX();
        double dz = offsetZ - footprint().minZ();
        return turned ? new Vec3(-dz, 0, dx) : new Vec3(dx, 0, dz);
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

    // A model box moved to where it really is in this state: turn, then move, then the blockstate rotations
    private CBPixelBox place(CBPixelBox box, BlockState state)
    {
        boolean turned = isTurned(state);
        Vec3 translation = modelTranslation(offsetX(state), offsetZ(state), turned);
        return (turned ? box.rotateY(1) : box)
                .offset(translation.x, translation.y, translation.z)
                .rotateX(xQuarterTurns(state))
                .rotateY(yQuarterTurns(state));
    }

    // Where the main body actually is in the block
    public CBPixelBox worldFootprint(BlockState state)
    {
        return place(footprint(), state);
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
        // Next to a beam of the same kind along this one's length: line up with it exactly
        BlockPos neighborPos = alignedNeighbor(level, pos, state, clickedFace.getOpposite());
        if (neighborPos != null)
        {
            BlockState neighbor = level.getBlockState(neighborPos);
            CBBoltedBeamBlock other = (CBBoltedBeamBlock) neighbor.getBlock();
            return bestFit(state, other.worldFootprint(neighbor), true);
        }

        // Otherwise: upright beams turn to match the way the player is looking...
        if (isTurnable())
        {
            boolean lookingEastWest = context.getHorizontalDirection().getAxis() == Direction.Axis.X;
            state = state.setValue(TURNED, facing.getAxis().isVertical() && lookingEastWest);
        }
        // ...and centre on the clicked pixel (as close as the block allows)
        Vec3 click = context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)).scale(16);
        return bestFit(state, new CBPixelBox(click.x, click.y, click.z, click.x, click.y, click.z), false);
    }

    // A beam or cap of the same kind directly before or after this one along its length, or null.
    // preferredSide (if given and lined up) is checked first, so the block that was clicked wins.
    @Nullable
    private BlockPos alignedNeighbor(LevelReader level, BlockPos pos, BlockState state, @Nullable Direction preferredSide)
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

    // Tries every position (and turn, if allowed) and keeps the one whose body best matches the target
    // across the beam's length. Used for clicks, stacking, rotating and mirroring.
    private BlockState bestFit(BlockState state, CBPixelBox target, boolean tryBothTurns)
    {
        Direction.Axis lengthAxis = facing(state).getAxis();
        boolean[] turns = tryBothTurns && isTurnable() ? new boolean[] { false, true } : new boolean[] { isTurned(state) };

        BlockState best = state;
        double bestDifference = Double.MAX_VALUE;
        for (boolean turned : turns)
        {
            for (int x = 0; x <= maxOffsetX(); x++)
            {
                for (int z = 0; z <= maxOffsetZ(); z++)
                {
                    BlockState candidate = state.setValue(offsetXProperty(), x).setValue(offsetZProperty(), z);
                    if (isTurnable()) candidate = candidate.setValue(TURNED, turned);

                    double difference = crossSectionDifference(worldFootprint(candidate), target, lengthAxis);
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
    private static double crossSectionDifference(CBPixelBox a, CBPixelBox b, Direction.Axis lengthAxis)
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

    // Turn the facing, then find the position whose body sits where the old body ends up after turning
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
        return bestFit(rotated, worldFootprint(state).rotateY(quarterTurns), true);
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
        return bestFit(flipped, mirrored, true);
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}
