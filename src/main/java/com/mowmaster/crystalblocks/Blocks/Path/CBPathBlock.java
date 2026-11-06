package com.mowmaster.crystalblocks.Blocks.Path;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

// A path made from another block (gravel, sand, red sand, clay, mud), working like vanilla's dirt path:
// a pixel lower than a full block, turns back into its source block when something solid is put on top,
// and drops the source block (see CBBlockLootProvider). Made with the tamper.
public class CBPathBlock extends Block
{
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 15, 16);

    private final Supplier<Block> source;

    public CBPathBlock(Properties properties, Supplier<Block> source)
    {
        super(properties);
        this.source = source;
    }

    // The block this path was made from, and turns back into
    public Block source()
    {
        return source.get();
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    // Mobs don't treat paths as somewhere to walk around, same as vanilla's dirt path
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type)
    {
        return false;
    }

    // Placed under something solid: it's the source block instead
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : source().defaultBlockState();
    }

    // Like the dirt path: anything solid above (except a fence gate) squashes it back into its source block
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockState above = level.getBlockState(pos.above());
        return !above.isSolid() || above.getBlock() instanceof FenceGateBlock;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        if (direction == Direction.UP && !state.canSurvive(level, pos))
        {
            scheduledTickAccess.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        // Entities standing in the pixel gap get lifted onto the full block
        level.setBlockAndUpdate(pos, pushEntitiesUp(state, source().defaultBlockState(), level, pos));
    }
}
