package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A barrel like vanilla's (27 slots, 6-way facing, open model while someone has it open), with three tint
// regions: 0 = the log sides, 1 = the metal straps, 2 = the plank inside. A redstone signal also opens it
// (open sound, open model) for as long as it stays powered.
public class CBTintableBarrel extends Block implements IColorableBlock
{
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    // Someone has the barrel open
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    // A redstone signal holds the lid open
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final int SIDE_REGION = 0;
    public static final int STRAP_REGION = 1;
    public static final int INNER_REGION = 2;

    // The model's boxes, as modelled (upright, opening at the top)
    private static final List<CBPixelBox> SIDES = List.of(
            new CBPixelBox(2, 0, 2, 14, 16, 3),
            new CBPixelBox(2, 0, 13, 14, 16, 14),
            new CBPixelBox(2, 0, 3, 3, 16, 13),
            new CBPixelBox(13, 0, 3, 14, 16, 13));
    private static final List<CBPixelBox> STRAPS = straps();
    private static final List<CBPixelBox> INNER_CLOSED = List.of(new CBPixelBox(3, 1, 3, 13, 15, 13));
    private static final List<CBPixelBox> INNER_OPEN = List.of(new CBPixelBox(3, 1, 3, 13, 2, 13));

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public CBTintableBarrel(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(OPEN, false)
                .setValue(POWERED, false));

        for (BlockState state : stateDefinition.getPossibleStates())
        {
            shapes.put(state, CBPixelBox.union(regions(state).stream().flatMap(List::stream).map(box -> place(box, state)).toList()));
        }
    }

    // Three bands of four straps (at y 2, 7 and 13), each 1 pixel tall and half a pixel thick
    private static List<CBPixelBox> straps()
    {
        List<CBPixelBox> straps = new java.util.ArrayList<>();
        for (int y : new int[] { 2, 7, 13 })
        {
            straps.add(new CBPixelBox(1.5, y, 1.5, 14.5, y + 1, 2));
            straps.add(new CBPixelBox(1.5, y, 14, 14.5, y + 1, 14.5));
            straps.add(new CBPixelBox(1.5, y, 2, 2, y + 1, 14));
            straps.add(new CBPixelBox(14, y, 2, 14.5, y + 1, 14));
        }
        return List.copyOf(straps);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, OPEN, POWERED);
    }

    // Shows the open model: opened by a player, or held open by redstone
    public static boolean looksOpen(BlockState state)
    {
        return state.getValue(OPEN) || state.getValue(POWERED);
    }

    // ---- Rotations, shared with datagen: up as modelled, down = x 180, sideways = x 90 then y ----

    public static int xQuarterTurns(Direction facing)
    {
        return switch (facing)
        {
            case UP -> 0;
            case DOWN -> 2;
            default -> 1;
        };
    }

    public static int yQuarterTurns(Direction facing)
    {
        return facing.getAxis().isVertical() ? 0 : CBPixelBox.quarterTurns(facing);
    }

    private static CBPixelBox place(CBPixelBox box, BlockState state)
    {
        Direction facing = state.getValue(FACING);
        return box.rotateX(xQuarterTurns(facing)).rotateY(yQuarterTurns(facing));
    }

    private static List<List<CBPixelBox>> regions(BlockState state)
    {
        return List.of(SIDES, STRAPS, looksOpen(state) ? INNER_OPEN : INNER_CLOSED);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    // ---- Tint regions ----

    @Override
    public int getTintRegionCount()
    {
        return 3;
    }

    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        List<List<CBPixelBox>> regions = regions(state);
        for (int region = regions.size() - 1; region >= 0; region--)
        {
            for (CBPixelBox box : regions.get(region))
            {
                if (place(box, state).contains(pixel)) return region;
            }
        }
        return SIDE_REGION;
    }

    // Has its own block entity (with an inventory) instead of the plain tint one
    @Override
    public boolean usesTintBlockEntity()
    {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CBTintableBarrelBlockEntity(pos, state);
    }

    // ---- Placement: the opening faces the player, like vanilla ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    // ---- Using it ----

    // Dyes colour the clicked part; anything else falls through to useWithoutItem (opening it)
    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CBTintableBarrelBlockEntity barrel)
        {
            player.openMenu(barrel);
            player.awardStat(Stats.OPEN_BARREL);
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Redstone ----

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston)
    {
        updatePower(state, level, pos);
    }

    private static void updatePower(BlockState state, Level level, BlockPos pos)
    {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) return;

        // Only make a sound when the barrel visibly opens or closes (not while a player already has it open)
        if (!state.getValue(OPEN)) playSound(level, pos, state, powered ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE);
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
    }

    // Played at the opening, like vanilla
    public static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound)
    {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() / 2.0;
        double y = pos.getY() + 0.5 + facing.getStepY() / 2.0;
        double z = pos.getZ() + 0.5 + facing.getStepZ() / 2.0;
        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5f, level.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    // ---- Comparators ----

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction)
    {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    // The contents are dropped by the block entity (vanilla does it for every Container); this updates comparators
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston)
    {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
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
