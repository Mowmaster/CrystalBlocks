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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// The support beam with two strap loops. A chain (vanilla chain or chain beam) running towards a loop
// attaches to it, which swaps the model to show the chain link. Region 0 = log beam, region 1 = metal.
public class CBChainedSupportBeam extends CBBoltedBeamBlock
{
    // A chain is attached to the loop on the model's north / south side (as modelled)
    public static final BooleanProperty CHAIN_NORTH = BooleanProperty.create("chain_north");
    public static final BooleanProperty CHAIN_SOUTH = BooleanProperty.create("chain_south");

    // From models/block/chained_support_beam.json (bolts left out)
    private static final List<List<CBPixelBox>> REGIONS = List.of(
            List.of(new CBPixelBox(4, 0, 4, 12, 16, 12)),
            List.of(new CBPixelBox(3.5, 6, 3.5, 4, 10, 12.5),
                    new CBPixelBox(12, 6, 3.5, 12.5, 10, 12.5),
                    new CBPixelBox(4, 6, 3.5, 12, 10, 4),
                    new CBPixelBox(4, 6, 12, 12, 10, 12.5),
                    new CBPixelBox(8, 6.75, 12, 8.5, 9.25, 14.5),  // south loop
                    new CBPixelBox(8, 6.75, 1.5, 8.5, 9.25, 4)));  // north loop

    // Chain-sized boxes (2.5 x 2.5, like the chain beam) centred on each loop's chain link, as modelled
    private static final CBPixelBox SOUTH_LINK = new CBPixelBox(7, 6.75, 12, 9.5, 9.25, 16);
    private static final CBPixelBox NORTH_LINK = new CBPixelBox(7, 6.75, 0, 9.5, 9.25, 4);

    public CBChainedSupportBeam(Properties properties)
    {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CHAIN_NORTH, false).setValue(CHAIN_SOUTH, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(CHAIN_NORTH, CHAIN_SOUTH);
    }

    @Override public Kind kind() { return Kind.SQUARE; }
    @Override public boolean isCap() { return false; }
    @Override public int turnCount() { return 2; }
    @Override protected List<List<CBPixelBox>> regionBoxes() { return REGIONS; }
    @Override protected CBPixelBox footprint() { return CBBoltedSupportBeam.FOOTPRINT; }
    @Override public IntegerProperty offsetXProperty() { return CBBoltedSupportBeam.OFFSET_X; }
    @Override public IntegerProperty offsetZProperty() { return CBBoltedSupportBeam.OFFSET_Z; }

    // ---- Chains ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return withChains(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    // A neighbour changed: re-check both loops
    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        return withChains(state, level, pos);
    }

    private BlockState withChains(BlockState state, LevelReader level, BlockPos pos)
    {
        return state
                .setValue(CHAIN_NORTH, chainAt(state, level, pos, worldDirection(Direction.NORTH, state)))
                .setValue(CHAIN_SOUTH, chainAt(state, level, pos, worldDirection(Direction.SOUTH, state)));
    }

    // Chains snap to a loop's link when placed against it
    @Override
    public @Nullable CBPixelBox connectionPoint(BlockState state, Direction side, CBBoltedBeamBlock connecting)
    {
        return connecting.kind() == Kind.CHAIN ? linkBox(state, side) : null;
    }

    // Where the chain link on the loop facing that world side is, in this block's pixels; null if no loop faces it
    @Nullable
    private CBPixelBox linkBox(BlockState state, Direction worldSide)
    {
        if (worldDirection(Direction.SOUTH, state) == worldSide) return place(SOUTH_LINK, state);
        if (worldDirection(Direction.NORTH, state) == worldSide) return place(NORTH_LINK, state);
        return null;
    }

    // A chain on that side that actually runs through the loop's link: a vanilla chain (always in the middle of
    // its block) or a chain beam, running towards this block and lined up with the link
    private boolean chainAt(BlockState state, LevelReader level, BlockPos pos, Direction side)
    {
        CBPixelBox link = linkBox(state, side);
        if (link == null) return false;

        BlockState neighbor = level.getBlockState(pos.relative(side));
        if (neighbor.getBlock() instanceof ChainBlock)
        {
            return neighbor.getValue(ChainBlock.AXIS) == side.getAxis()
                    && centresMeet(link, new CBPixelBox(8, 8, 8, 8, 8, 8), side.getAxis());
        }
        if (neighbor.getBlock() instanceof CBBoltedBeamBlock beam && beam.kind() == Kind.CHAIN)
        {
            return beam.facing(neighbor).getAxis() == side.getAxis()
                    && centresMeet(link, beam.worldFootprint(neighbor), side.getAxis());
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