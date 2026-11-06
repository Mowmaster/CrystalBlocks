package com.mowmaster.crystalblocks.Items.Tools;

import com.mowmaster.mowlib.keys.IActivatable;
import com.mowmaster.mowlib.keys.IPageKeyTool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// Turns blocks that have a facing or an axis (logs, stairs, beams, chests, observers...) a quarter turn:
// - Right-click: around the axis of the clicked face (click the top to spin it like a turntable)
// - Next page key: around the X axis; previous page key: around the Z axis; activate key: around the Y axis
// Clockwise as seen looking at the clicked face (keys: as seen from the east, south or above); sneak to reverse.
// Around Y it uses the block's own rotate() (what structures use), so stairs, beams and anything modded turn properly.
// Around X or Z it turns every facing/axis property, and flips top/bottom halves on a half turn; if the block can't
// face the new way a quarter turn would give (stairs can't face up), it tries a half turn, and otherwise does nothing.
// Beds, doors, double chests and extended pistons are left alone: turning one half would break them.
public class CBWrenchItem extends Item implements IPageKeyTool, IActivatable
{
    public CBWrenchItem(Properties properties)
    {
        super(properties);
    }

    // ---- Right-click ----

    // NeoForge runs onItemUseFirst before the clicked block's own right-click, so chests and levers turn
    // instead of opening or flicking
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        // Clockwise seen from the clicked face: the positive faces (east, up, south) look back down the axis
        boolean clockwise = (face.getAxisDirection() == Direction.AxisDirection.POSITIVE) != player.isSecondaryUseActive();

        if (level.isClientSide())
        {
            // Predict it so the arm swings (and the block's own right-click is skipped); the server does the turning
            return rotated(level.getBlockState(pos), level, pos, face.getAxis(), clockwise) != null
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return rotate(level, pos, player, face.getAxis(), clockwise) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // ---- Keys (MowLib) ----

    // Take over the page keys whenever a block is targeted, so they don't turn pages while wrenching
    @Override
    public boolean usesPageKeys(ItemStack stack, Level level, BlockPos pos, Player player)
    {
        return !level.getBlockState(pos).isAir();
    }

    // Next (+1) = X axis, previous (-1) = Z axis
    @Override
    public void onPageKey(ItemStack stack, ServerPlayer player, BlockPos pos, int direction)
    {
        keyRotate(player, pos, direction > 0 ? Direction.Axis.X : Direction.Axis.Z);
    }

    @Override
    public boolean canActivate(ItemStack stack)
    {
        return true;
    }

    // Activate while not looking at a block: nothing to turn
    @Override
    public void activate(ItemStack stack, ServerPlayer player)
    {
    }

    @Override
    public boolean usesBlockActivation(ItemStack stack)
    {
        return true;
    }

    // Activate while looking at a block = Y axis
    @Override
    public boolean activateAtBlock(ItemStack stack, ServerPlayer player, BlockPos pos)
    {
        keyRotate(player, pos, Direction.Axis.Y);
        return true;
    }

    // MowLib has already checked the reach and that the player may use the block
    private static void keyRotate(ServerPlayer player, BlockPos pos, Direction.Axis axis)
    {
        if (!rotate(player.level(), pos, player, axis, !player.isSecondaryUseActive()))
        {
            player.sendOverlayMessage(Component.translatable("message.crystalblocks.wrench.cannot_rotate"));
        }
    }

    // ---- Turning ----

    // Turns the block in the world if it can. Returns whether it turned.
    public static boolean rotate(Level level, BlockPos pos, Player player, Direction.Axis axis, boolean clockwise)
    {
        if (!player.mayBuild() || !level.mayInteract(player, pos)) return false;

        BlockState state = level.getBlockState(pos);
        BlockState turned = rotated(state, level, pos, axis, clockwise);
        if (turned == null) return false;

        // Let shape properties (stair corners, fence connections...) catch up with the new facing
        turned = Block.updateFromNeighbourShapes(turned, level, pos);
        // e.g. a torch turned to face away from its wall
        if (!turned.canSurvive(level, pos)) return false;

        // Same block, so its block entity (contents, colours) stays
        level.setBlock(pos, turned, Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 1f, 1f);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }

    // The state turned a quarter turn around the axis (or a half turn when a quarter isn't possible),
    // or null if it can't be turned or nothing would change
    @Nullable
    public static BlockState rotated(BlockState state, Level level, BlockPos pos, Direction.Axis axis, boolean clockwise)
    {
        if (isPartOfMultiblock(state) || state.getDestroySpeed(level, pos) < 0) return null;

        if (axis == Direction.Axis.Y)
        {
            // NeoForge's level-aware version of rotate(); blocks override it when turning depends on the world
            BlockState turned = state.rotate(level, pos, clockwise ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90);
            return turned == state ? null : turned;
        }

        for (int quarterTurns = 1; quarterTurns <= 2; quarterTurns++)
        {
            BlockState turned = turnAround(state, axis, clockwise, quarterTurns);
            if (turned != null && turned != state) return turned;
        }
        return null;
    }

    // Turns every direction and axis property around a horizontal axis, and flips halves on a half turn.
    // Null if any property can't hold its turned value.
    @Nullable
    @SuppressWarnings("unchecked") // the casts are checked against getValueClass() first
    private static BlockState turnAround(BlockState state, Direction.Axis axis, boolean clockwise, int quarterTurns)
    {
        BlockState result = state;
        for (Property<?> property : state.getProperties())
        {
            Class<?> type = property.getValueClass();
            if (type == Direction.class)
            {
                Property<Direction> directions = (Property<Direction>) property;
                Direction turned = state.getValue(directions);
                for (int i = 0; i < quarterTurns; i++)
                {
                    // getClockWise(axis): clockwise seen from the axis' positive end
                    turned = clockwise ? turned.getClockWise(axis) : turned.getCounterClockWise(axis);
                }
                if (!directions.getPossibleValues().contains(turned)) return null;
                result = result.setValue(directions, turned);
            }
            else if (type == Direction.Axis.class)
            {
                Property<Direction.Axis> axes = (Property<Direction.Axis>) property;
                Direction.Axis current = state.getValue(axes);
                // A quarter turn swaps the other two axes; a half turn changes nothing
                Direction.Axis turned = quarterTurns % 2 == 0 || current == axis ? current : otherAxis(axis, current);
                if (!axes.getPossibleValues().contains(turned)) return null;
                result = result.setValue(axes, turned);
            }
            else if (property == BlockStateProperties.HALF)
            {
                // Top/bottom can't point sideways
                if (quarterTurns == 1) return null;
                result = result.setValue(BlockStateProperties.HALF, state.getValue(BlockStateProperties.HALF) == Half.TOP ? Half.BOTTOM : Half.TOP);
            }
            else if (property == BlockStateProperties.SLAB_TYPE)
            {
                SlabType slab = state.getValue(BlockStateProperties.SLAB_TYPE);
                if (slab == SlabType.DOUBLE) continue;
                if (quarterTurns == 1) return null;
                result = result.setValue(BlockStateProperties.SLAB_TYPE, slab == SlabType.TOP ? SlabType.BOTTOM : SlabType.TOP);
            }
        }
        return result;
    }

    // The axis that is neither of these two
    private static Direction.Axis otherAxis(Direction.Axis first, Direction.Axis second)
    {
        for (Direction.Axis axis : Direction.Axis.values())
        {
            if (axis != first && axis != second) return axis;
        }
        return first;
    }

    private static boolean isPartOfMultiblock(BlockState state)
    {
        return state.hasProperty(BlockStateProperties.BED_PART)
                || state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                || (state.hasProperty(BlockStateProperties.CHEST_TYPE) && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE)
                || (state.hasProperty(BlockStateProperties.EXTENDED) && state.getValue(BlockStateProperties.EXTENDED))
                || state.is(Blocks.PISTON_HEAD) || state.is(Blocks.MOVING_PISTON);
    }

    // ---- Tooltip ----

    // Component.keybind shows whatever key each MowLib key is bound to
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.crystalblocks.wrench.click").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.crystalblocks.wrench.keys",
                Component.keybind("key.mowlib.page_next"), Component.keybind("key.mowlib.page_previous"),
                Component.keybind("key.mowlib.activate")).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.crystalblocks.wrench.sneak").withStyle(ChatFormatting.DARK_GRAY));
    }
}
