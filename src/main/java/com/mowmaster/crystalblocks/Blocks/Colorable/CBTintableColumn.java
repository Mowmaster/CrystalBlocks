package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CBTintableColumn extends Block implements IColorableBlock
{
    // Column width and depth in pixels
    public static final int SIZE = 8;
    // Furthest the column's corner can sit from the west/north edge: 8 = flush with the east/south edge
    public static final int MAX_OFFSET = 16 - SIZE;

    // The column's west (x) and north (z) edge, in pixels from the block's corner
    public static final IntegerProperty X = IntegerProperty.create("x", 0, MAX_OFFSET);
    public static final IntegerProperty Z = IntegerProperty.create("z", 0, MAX_OFFSET);

    private static final VoxelShape[] SHAPES = new VoxelShape[(MAX_OFFSET + 1) * (MAX_OFFSET + 1)];

    static
    {
        for (int x = 0; x <= MAX_OFFSET; x++)
        {
            for (int z = 0; z <= MAX_OFFSET; z++)
            {
                SHAPES[shapeIndex(x, z)] = Block.box(x, 0, z, x + SIZE, 16, z + SIZE);
            }
        }
    }

    public CBTintableColumn(Properties properties)
    {
        super(properties);
        // Default to centred
        registerDefaultState(stateDefinition.any().setValue(X, MAX_OFFSET / 2).setValue(Z, MAX_OFFSET / 2));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(X, Z);
    }

    // ---- Shape ----

    private static int shapeIndex(int x, int z)
    {
        return x * (MAX_OFFSET + 1) + z;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[shapeIndex(state.getValue(X), state.getValue(Z))];
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

        // Placed on top of / underneath another column: copy its position so the stack lines up
        if (clickedFace.getAxis() == Direction.Axis.Y && !context.replacingClickedOnBlock())
        {
            BlockState against = context.getLevel().getBlockState(pos.relative(clickedFace.getOpposite()));
            if (against.getBlock() instanceof CBTintableColumn)
            {
                return withOffset(defaultBlockState(), against.getValue(X), against.getValue(Z));
            }
        }

        // Otherwise centre the column on the clicked pixel, kept inside the block.
        // Clicking the side of a block puts the click on that edge, so the column ends up flush against it.
        Vec3 hit = context.getClickLocation();
        return withOffset(defaultBlockState(), toOffset(hit.x - pos.getX()), toOffset(hit.z - pos.getZ()));
    }

    // Converts a click position inside the block (0.0 - 1.0) into the column's edge in whole pixels
    static int toOffset(double positionInBlock)
    {
        int centredEdge = (int) Math.round(positionInBlock * 16 - SIZE / 2.0);
        return Mth.clamp(centredEdge, 0, MAX_OFFSET);
    }

    private static BlockState withOffset(BlockState state, int x, int z)
    {
        return state.setValue(X, x).setValue(Z, z);
    }

    // Stacked onto a column above or below: copy its colour, unless the placed item already gave this one a colour
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity placed) || placed.isDyed(0)) return;

        for (Direction direction : new Direction[] { Direction.DOWN, Direction.UP })
        {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() instanceof CBTintableColumn
                    && neighbor.getValue(X).equals(state.getValue(X))
                    && neighbor.getValue(Z).equals(state.getValue(Z))
                    && level.getBlockEntity(neighborPos) instanceof CBTintBlockEntity source
                    && source.isDyed(0))
            {
                placed.setColor(0, source.getColor(0));
                return;
            }
        }
    }

    // ---- Rotation (structures, other mods' rotation tools) ----

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation)
    {
        int x = state.getValue(X);
        int z = state.getValue(Z);
        return switch (rotation)
        {
            case NONE -> state;
            case CLOCKWISE_90 -> withOffset(state, MAX_OFFSET - z, x);
            case CLOCKWISE_180 -> withOffset(state, MAX_OFFSET - x, MAX_OFFSET - z);
            case COUNTERCLOCKWISE_90 -> withOffset(state, z, MAX_OFFSET - x);
        };
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror)
    {
        return switch (mirror)
        {
            case NONE -> state;
            case LEFT_RIGHT -> state.setValue(Z, MAX_OFFSET - state.getValue(Z)); // flips north/south
            case FRONT_BACK -> state.setValue(X, MAX_OFFSET - state.getValue(X)); // flips east/west
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
