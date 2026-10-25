package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// A support beam that chains can join from any side of its band. Renders the plain support beam, plus half a
// chain from each joined side's block edge into the middle of the block, lined up with the band.
// Region 0 = log beam, region 1 = metal (straps and chains).
public class CBChainedSupportBeam extends CBBoltedBeamBlock
{
    // A chain joins that side of the band. Sides are "as the beam stands upright after its turn".
    public static final BooleanProperty CHAIN_NORTH = BooleanProperty.create("chain_north");
    public static final BooleanProperty CHAIN_EAST = BooleanProperty.create("chain_east");
    public static final BooleanProperty CHAIN_SOUTH = BooleanProperty.create("chain_south");
    public static final BooleanProperty CHAIN_WEST = BooleanProperty.create("chain_west");

    public static final Direction[] CHAIN_SIDES = { Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST };

    // A chain-sized box from the middle of the block to its south edge, at the band's height
    private static final CBPixelBox SOUTH_CHAIN = new CBPixelBox(6.75, 6.75, 8, 9.25, 9.25, 16);

    // The plain support beam's boxes (bolts left out)
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 4, 12, 16, 12)),
            List.of(new CBPixelBox(3.5, 6, 3.5, 4, 10, 12.5),
                    new CBPixelBox(12, 6, 3.5, 12.5, 10, 12.5),
                    new CBPixelBox(4, 6, 3.5, 12, 10, 4),
                    new CBPixelBox(4, 6, 12, 12, 10, 12.5)));

    public CBChainedSupportBeam(Properties properties)
    {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(CHAIN_NORTH, false).setValue(CHAIN_EAST, false)
                .setValue(CHAIN_SOUTH, false).setValue(CHAIN_WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(CHAIN_NORTH, CHAIN_EAST, CHAIN_SOUTH, CHAIN_WEST);
    }

    @Override public Kind kind() { return Kind.SQUARE; }
    @Override public boolean isCap() { return false; }
    @Override public int turnCount() { return 2; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBBoltedSupportBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBBoltedSupportBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBBoltedSupportBeam.OFFSET_Z; }

    public static BooleanProperty chainProperty(Direction side)
    {
        return switch (side)
        {
            case NORTH -> CHAIN_NORTH;
            case EAST -> CHAIN_EAST;
            case WEST -> CHAIN_WEST;
            default -> CHAIN_SOUTH;
        };
    }

    // Quarter turns (clockwise from above) that take the south half-chain round to that side
    public static int chainTurns(Direction side)
    {
        return switch (side)
        {
            case WEST -> 1;
            case NORTH -> 2;
            case EAST -> 3;
            default -> 0;
        };
    }

    // ---- Where each half-chain goes (shared with datagen) ----

    // The half-chain follows the beam across its side, but never along its own length,
    // so it always starts exactly at the block's edge
    public Vec3 chainTranslation(BlockState state, Direction side)
    {
        Vec3 beam = modelTranslation(offsetX(state), offsetZ(state), turn(state));
        double dx = side.getAxis() == Direction.Axis.X ? 0 : beam.x;
        double dz = side.getAxis() == Direction.Axis.Z ? 0 : beam.z;
        return new Vec3(dx, 0, dz);
    }

    // Where the half-chain on that side is, in this block's pixels
    public CBPixelBox chainBox(BlockState state, Direction side)
    {
        Vec3 translation = chainTranslation(state, side);
        return SOUTH_CHAIN.rotateY(chainTurns(side))
                .offset(translation.x, translation.y, translation.z)
                .rotateX(xQuarterTurns(state))
                .rotateY(yQuarterTurns(state));
    }

    // The side of the band facing that world direction, or null if it points along the beam
    @Nullable
    private Direction chainSideFacing(BlockState state, Direction worldSide)
    {
        for (Direction side : CHAIN_SIDES)
        {
            if (standardToWorld(side, state) == worldSide) return side;
        }
        return null;
    }

    // ---- Joining chains ----

    // Chains snap to the half-chain's position when placed against any side of the band
    @Override
    public @Nullable CBPixelBox connectionPoint(BlockState state, Direction worldSide, CBBoltedBeamBlock connecting)
    {
        if (connecting.kind() != Kind.CHAIN) return null;
        Direction side = chainSideFacing(state, worldSide);
        return side == null ? null : chainBox(state, side);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return withChains(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    // A neighbour changed: re-check every side
    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        return withChains(state, level, pos);
    }

    private BlockState withChains(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockState result = state;
        for (Direction side : CHAIN_SIDES)
        {
            result = result.setValue(chainProperty(side), chainAt(state, level, pos, side));
        }
        return result;
    }

    // A chain on that side that lines up with the half-chain: a vanilla chain (always through the middle of its
    // block) or a chain beam, running towards this block
    private boolean chainAt(BlockState state, LevelReader level, BlockPos pos, Direction side)
    {
        Direction worldSide = standardToWorld(side, state);
        CBPixelBox chain = chainBox(state, side);

        BlockState neighbor = level.getBlockState(pos.relative(worldSide));
        if (neighbor.getBlock() instanceof ChainBlock)
        {
            return neighbor.getValue(ChainBlock.AXIS) == worldSide.getAxis()
                    && centresMeet(chain, new CBPixelBox(8, 8, 8, 8, 8, 8), worldSide.getAxis());
        }
        if (neighbor.getBlock() instanceof CBBoltedBeamBlock beam && beam.kind() == Kind.CHAIN)
        {
            return beam.facing(neighbor).getAxis() == worldSide.getAxis()
                    && centresMeet(chain, beam.worldFootprint(neighbor), worldSide.getAxis());
        }
        return false;
    }

    // Whether two boxes' centres are within half a pixel of each other across the chain's length
    private static boolean centresMeet(CBPixelBox a, CBPixelBox b, Direction.Axis lengthAxis)
    {
        for (Direction.Axis axis : Direction.Axis.values())
        {
            if (axis == lengthAxis) continue;
            double centreA = (a.min(axis) + a.max(axis)) / 2;
            double centreB = (b.min(axis) + b.max(axis)) / 2;
            if (Math.abs(centreA - centreB) > 0.5) return false;
        }
        return true;
    }
}