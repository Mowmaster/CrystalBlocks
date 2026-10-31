package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

// A wall sconce holding 1-3 torches. Light 15 while lit. Goes out in water or when hit by a wind charge;
// relit with a torch or flint and steel. Right-click with another sconce to add a torch, shears to take one off.
// The metal is dyeable (tint 0).
public class CBSconce extends Block implements IColorableBlock, SimpleWaterloggedBlock
{
    // The side the back plate is on (against the wall)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty TORCHES = IntegerProperty.create("torches", 1, 3);

    // Each torch's flame, in pixels as modelled (facing north), for 1, 2 and 3 torches
    private static final List<List<Vec3>> FLAMES = List.of(
            List.of(new Vec3(8, 12.5, 4)),
            List.of(new Vec3(7.5, 12.5, 4), new Vec3(10.1, 12, 5)),
            List.of(new Vec3(4.3, 12, 4), new Vec3(7.5, 12.5, 5.25), new Vec3(10.1, 12, 6.25)));

    // Outline: the back plate plus the area the torches take up, as modelled (facing north)
    private static final CBPixelBox PLATE = new CBPixelBox(5, 0, 3.5, 11, 16, 4);
    private static final List<CBPixelBox> TORCH_AREAS = List.of(
            new CBPixelBox(6, 0, 0, 10, 13, 6),
            new CBPixelBox(5.5, 0, 0, 11, 13, 7),
            new CBPixelBox(4, 0, 0, 11, 13, 8));

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public CBSconce(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, true)
                .setValue(WATERLOGGED, false)
                .setValue(TORCHES, 1));

        for (BlockState state : stateDefinition.getPossibleStates())
        {
            int turns = CBPixelBox.quarterTurns(state.getValue(FACING));
            CBPixelBox torches = TORCH_AREAS.get(state.getValue(TORCHES) - 1);
            shapes.put(state, CBPixelBox.union(List.of(PLATE.rotateY(turns), torches.rotateY(turns))));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, LIT, WATERLOGGED, TORCHES);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    // ---- Placement ----

    // On a wall: back plate against it. On a floor or ceiling: facing away from the player.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction clickedFace = context.getClickedFace();
        Direction facing = clickedFace.getAxis().isHorizontal() ? clickedFace.getOpposite() : context.getHorizontalDirection();
        boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, inWater)
                .setValue(LIT, !inWater);
    }

    // ---- Right-click ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        int torches = state.getValue(TORCHES);

        // Another sconce adds a torch
        if (stack.is(asItem()) && torches < 3)
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(TORCHES, torches + 1), Block.UPDATE_ALL);
                stack.consume(1, player);
                level.playSound(null, pos, SoundType.CHAIN.getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // Shears take one off and drop it
        if (stack.is(Tags.Items.TOOLS_SHEAR) && torches > 1)
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(TORCHES, torches - 1), Block.UPDATE_ALL);
                Block.popResource(level, pos, new ItemStack(asItem()));
                stack.hurtAndBreak(1, player, player.getUsedItemHand());
                level.playSound(null, pos, SoundType.CHAIN.getBreakSound(), SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // A torch or flint and steel relights it (not underwater)
        if (!state.getValue(LIT) && !state.getValue(WATERLOGGED) && (stack.is(Items.TORCH) || stack.is(Items.FLINT_AND_STEEL)))
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
                if (stack.is(Items.FLINT_AND_STEEL)) stack.hurtAndBreak(1, player, player.getUsedItemHand());
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // Anything else: dyes colour the metal
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    // ---- Going out ----

    private static void extinguish(LevelAccessor level, BlockPos pos, BlockState state)
    {
        level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
        level.playSound((Player) null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
    }

    // Wind charges trigger blocks rather than breaking them, the same way they blow out candles
    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer)
    {
        if (explosion.canTriggerBlocks() && state.getValue(LIT))
        {
            extinguish(level, pos, state);
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    // ---- Water ----

    // Water placed into it (bucket): waterlog and put it out
    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState)
    {
        if (state.getValue(WATERLOGGED) || fluidState.getType() != Fluids.WATER) return false;
        if (!level.isClientSide())
        {
            if (state.getValue(LIT)) extinguish(level, pos, state);
            level.setBlock(pos, level.getBlockState(pos).setValue(WATERLOGGED, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
        return true;
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

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

    // ---- Drops ----

    // The loot table drops one sconce (with the usual tool and explosion rules); this sets the count to the torches held
    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder params)
    {
        List<ItemStack> drops = super.getDrops(state, params);
        for (ItemStack drop : drops)
        {
            if (drop.is(asItem())) drop.setCount(state.getValue(TORCHES));
        }
        return drops;
    }

    // ---- Flames ----

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (!state.getValue(LIT)) return;

        int turns = CBPixelBox.quarterTurns(state.getValue(FACING));
        for (Vec3 flame : FLAMES.get(state.getValue(TORCHES) - 1))
        {
            Vec3 point = rotateY(flame, turns);
            double x = pos.getX() + point.x / 16;
            double y = pos.getY() + point.y / 16;
            double z = pos.getZ() + point.z / 16;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }

        // An occasional quiet crackle
        if (random.nextInt(10) == 0)
        {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, SoundEvents.CANDLE_AMBIENT,
                    SoundSource.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
    }

    // Turns a point in pixels clockwise (seen from above) by quarter turns, matching CBPixelBox.rotateY
    private static Vec3 rotateY(Vec3 point, int quarterTurns)
    {
        double x = point.x;
        double z = point.z;
        for (int i = 0; i < quarterTurns; i++)
        {
            double oldX = x;
            x = 16 - z;
            z = oldX;
        }
        return new Vec3(x, point.y, z);
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
}
