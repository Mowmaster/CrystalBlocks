package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CBTintableVerticalSlab extends Block implements IColorableBlock, SimpleWaterloggedBlock
{
    // FACING = the half of the block space the slab fills, e.g. NORTH fills the north half
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Block.box takes pixel coordinates (0-16) and converts them to a 0-1 VoxelShape
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 0, 8, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(8, 0, 0, 16, 16, 16);

    public CBTintableVerticalSlab(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, WATERLOGGED);
    }

    // ---- Shape ----

    // Used for the outline and, because the block has collision, for collision too
    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    // Light passes through the empty half, like vanilla slabs
    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    // ---- Placement ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(FACING, getPlacementFacing(context)).setValue(WATERLOGGED, inWater);
    }

    private Direction getPlacementFacing(BlockPlaceContext context)
    {
        Direction clickedFace = context.getClickedFace();

        // Sneaking against another vertical slab: copy its rotation
        if (context.isSecondaryUseActive() && !context.replacingClickedOnBlock())
        {
            // getClickedPos() is where the new block goes; step back through the clicked face to the block that was clicked
            BlockPos againstPos = context.getClickedPos().relative(clickedFace.getOpposite());
            BlockState against = context.getLevel().getBlockState(againstPos);
            if (against.getBlock() instanceof CBTintableVerticalSlab)
            {
                return against.getValue(FACING);
            }
        }

        // Clicked the side of a block: sit flat against that block
        if (clickedFace.getAxis().isHorizontal())
        {
            return clickedFace.getOpposite();
        }

        // Clicked a top or bottom: fill whichever half of the face was clicked, relative to the player
        Direction lookDirection = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        Vec3 hit = context.getClickLocation();

        // Distance from the centre of the block towards the direction the player faces:
        // positive = far half, negative = near half
        double alongLook = (hit.x - pos.getX() - 0.5) * lookDirection.getStepX()
                + (hit.z - pos.getZ() - 0.5) * lookDirection.getStepZ();

        return alongLook >= 0 ? lookDirection : lookDirection.getOpposite();
    }

    // ---- Waterlogging ----

    @Override
    protected @NotNull FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // Keeps the water inside the block flowing correctly when neighbours change
    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        if (state.getValue(WATERLOGGED))
        {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ---- Dyeing ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}