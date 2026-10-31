package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import com.mowmaster.crystalblocks.Blocks.CBBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
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

// A vanilla chest (inventory, double chests, lid animation, sounds, hoppers) with three tint regions:
// 0 = the plank inside, 1 = the metal bands, 2 = the knob (defaults to light grey).
// The bottom is a normal block model; the lid is drawn by CBTintableChestRenderer so it can swing open.
// A diamond or gold ingot used on the knob locks the chest to that player (see CBTintableChestBlockEntity.Lock).
public class CBTintableChest extends ChestBlock implements IColorableBlock
{
    public static final int INNER_REGION = 0;
    public static final int BAND_REGION = 1;
    public static final int KNOB_REGION = 2;

    public static final int KNOB_DEFAULT = 0xDCDCDC;
    public static final int KNOB_GOLD = 0xF6D03D;
    public static final int KNOB_DIAMOND = 0x62EDE4;

    // ---- The model's pxes as modelled (front facing north), per tint region: lid and bottom together ----

    private static final List<List<CBPixelBox>> SINGLE_pxES = List.of(
            List.of(px(1, 13, 1, 15, 15, 15), px(1, 0, 1, 15, 11, 15)),
            List.of(px(0, 12, 0, 1, 16, 1), px(0, 12, 15, 1, 16, 16), px(15, 12, 15, 16, 16, 16), px(15, 12, 0, 16, 16, 1),
                    px(0, 12, 1, 1, 13, 15), px(0, 15, 1, 1, 16, 15), px(15, 15, 1, 16, 16, 15), px(15, 12, 1, 16, 13, 15),
                    px(1, 12, 0, 7, 13, 1), px(6, 13, 0, 10, 14, 1), px(9, 12, 0, 15, 13, 1), px(1, 15, 0, 15, 16, 1),
                    px(1, 15, 15, 15, 16, 16), px(1, 12, 15, 15, 13, 16),
                    px(0, 0, 0, 1, 12, 1), px(0, 0, 15, 1, 12, 16), px(15, 0, 15, 16, 12, 16), px(15, 0, 0, 16, 12, 1),
                    px(0, 0, 1, 1, 1, 15), px(0, 11, 1, 1, 12, 15), px(15, 11, 1, 16, 12, 15), px(15, 0, 1, 16, 1, 15),
                    px(1, 0, 0, 15, 1, 1), px(1, 11, 0, 15, 12, 1), px(1, 11, 15, 15, 12, 16), px(1, 0, 15, 15, 1, 16)),
            List.of(px(7, 9, -0.5, 9, 13, 1)));

    // The right half of a double chest (its partner is to the west, x = 0 is the joined side)
    private static final List<List<CBPixelBox>> RIGHT_pxES = List.of(
            List.of(px(0, 13, 1, 15, 15, 15), px(0, 0, 1, 15, 11, 15)),
            List.of(px(15, 12, 15, 16, 16, 16), px(15, 12, 0, 16, 16, 1), px(15, 15, 1, 16, 16, 15), px(15, 12, 1, 16, 13, 15),
                    px(0, 13, 0, 2, 14, 1), px(1, 12, 0, 15, 13, 1), px(0, 15, 0, 15, 16, 1), px(0, 15, 15, 15, 16, 16),
                    px(0, 12, 15, 15, 13, 16),
                    px(15, 0, 15, 16, 12, 16), px(15, 0, 0, 16, 12, 1), px(15, 11, 1, 16, 12, 15), px(15, 0, 1, 16, 1, 15),
                    px(1, 11, 0, 15, 12, 1), px(0, 0, 0, 15, 1, 1), px(0, 11, 15, 15, 12, 16), px(0, 0, 15, 15, 1, 16)),
            List.of(px(0, 9, -0.5, 1, 13, 1)));

    // The left half is the right half mirrored east-west
    private static final List<List<CBPixelBox>> LEFT_pxES = RIGHT_pxES.stream()
            .map(region -> region.stream().map(CBPixelBox::mirrorX).toList())
            .toList();

    // Returned instead of the chest's inventory when automation must not reach it (diamond lock)
    private static final DoubleBlockCombiner.NeighborCombineResult<ChestBlockEntity> NO_CONTAINER =
            new DoubleBlockCombiner.NeighborCombineResult<>()
            {
                @Override
                public <S> S apply(DoubleBlockCombiner.Combiner<? super ChestBlockEntity, S> combiner)
                {
                    return combiner.acceptNone();
                }
            };

    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public CBTintableChest(Properties properties)
    {
        super(() -> CBBlockEntityRegistry.TINTABLE_CHEST.get(), SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            shapes.put(state, CBPixelBox.union(pxes(state).stream().flatMap(List::stream).map(px -> place(px, state)).toList()));
        }
    }

    private static CBPixelBox px(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return new CBPixelBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static List<List<CBPixelBox>> pxes(BlockState state)
    {
        return switch (state.getValue(TYPE))
        {
            case SINGLE -> SINGLE_pxES;
            case RIGHT -> RIGHT_pxES;
            case LEFT -> LEFT_pxES;
        };
    }

    // The models face north and are turned for the facing in the blockstate
    private static CBPixelBox place(CBPixelBox px, BlockState state)
    {
        return px.rotateY(CBPixelBox.quarterTurns(state.getValue(FACING)));
    }

    // The bottom renders as a normal block model (vanilla chests are drawn entirely by their renderer)
    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return shapes.get(state);
    }

    // ---- Tinting ----

    @Override
    public int getTintRegionCount()
    {
        return 3;
    }

    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        List<List<CBPixelBox>> regions = pxes(state);
        for (int region = regions.size() - 1; region >= 0; region--)
        {
            for (CBPixelBox px : regions.get(region))
            {
                if (place(px, state).contains(pixel)) return region;
            }
        }
        return INNER_REGION;
    }

    @Override
    public int getDefaultColor(int region)
    {
        return region == KNOB_REGION ? KNOB_DEFAULT : CBTintBlockEntity.DEFAULT_TINT;
    }

    @Override
    public boolean usesTintBlockEntity()
    {
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CBTintableChestBlockEntity(pos, state);
    }

    // ---- The two halves ----

    // This chest's block entity and, for a double chest, its partner's
    private List<CBTintableChestBlockEntity> halves(BlockGetter level, BlockPos pos, BlockState state)
    {
        List<CBTintableChestBlockEntity> halves = new ArrayList<>();
        if (level.getBlockEntity(pos) instanceof CBTintableChestBlockEntity chest) halves.add(chest);
        if (state.getValue(TYPE) != ChestType.SINGLE)
        {
            BlockPos partnerPos = pos.relative(getConnectedDirection(state));
            if (level.getBlockState(partnerPos).is(this) && level.getBlockEntity(partnerPos) instanceof CBTintableChestBlockEntity partner)
            {
                halves.add(partner);
            }
        }
        return halves;
    }

    private boolean canUse(BlockGetter level, BlockPos pos, BlockState state, @Nullable Player player)
    {
        return halves(level, pos, state).stream().allMatch(chest -> chest.canUse(player));
    }

    private boolean hasLock(BlockGetter level, BlockPos pos, BlockState state, CBTintableChestBlockEntity.Lock lock)
    {
        return halves(level, pos, state).stream().anyMatch(chest -> chest.getLock() == lock);
    }

    private void lockedFeedback(Level level, BlockPos pos, Player player)
    {
        if (!level.isClientSide())
        {
            player.sendOverlayMessage(Component.translatable("container.isLocked", getName()));
            level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1f, 1f);
        }
    }

    // ---- Using it ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        CBTintableChestBlockEntity.Lock lock = stack.is(Items.DIAMOND) ? CBTintableChestBlockEntity.Lock.DIAMOND
                : stack.is(Items.GOLD_INGOT) ? CBTintableChestBlockEntity.Lock.GOLD
                : CBTintableChestBlockEntity.Lock.NONE;
        boolean onKnob = getTintRegion(state, hitResult) == KNOB_REGION;

        // Locking: a diamond or gold ingot on the knob
        if (lock != CBTintableChestBlockEntity.Lock.NONE && onKnob)
        {
            List<CBTintableChestBlockEntity> halves = halves(level, pos, state);
            if (!canUse(level, pos, state, player))
            {
                lockedFeedback(level, pos, player);
                return InteractionResult.SUCCESS;
            }
            // Already locked this way: just open it
            if (halves.stream().allMatch(chest -> chest.getLock() == lock)) return InteractionResult.TRY_WITH_EMPTY_HAND;

            if (!level.isClientSide())
            {
                int knobColor = lock == CBTintableChestBlockEntity.Lock.DIAMOND ? KNOB_DIAMOND : KNOB_GOLD;
                for (CBTintableChestBlockEntity chest : halves)
                {
                    chest.setLock(lock, player.getUUID());
                    chest.setColor(KNOB_REGION, knobColor);
                }
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // Only the owner may dye a locked chest
        if (IColorableBlock.dyeColorOf(stack) != null && !canUse(level, pos, state, player))
        {
            lockedFeedback(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        // Dyes colour the clicked part; anything else falls through to useWithoutItem (opening it)
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!canUse(level, pos, state, player))
        {
            lockedFeedback(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    // ---- Locks: automation, breaking and explosions ----

    // Hoppers (and our item capability) ask for the inventory with ignoreBlocked = true; players opening the chest
    // and comparators use false. A diamond lock hides the inventory from automation only.
    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState state, Level level, BlockPos pos, boolean ignoreBlocked)
    {
        if (ignoreBlocked && hasLock(level, pos, state, CBTintableChestBlockEntity.Lock.DIAMOND)) return NO_CONTAINER;
        return super.combine(state, level, pos, ignoreBlocked);
    }

    // Diamond: nobody but the owner can mine it in survival (creative players break blocks without mining)
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos)
    {
        if (level.getBlockEntity(pos) instanceof CBTintableChestBlockEntity chest
                && chest.getLock() == CBTintableChestBlockEntity.Lock.DIAMOND && !chest.canUse(player))
        {
            return 0f;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    // Gold and diamond: explosions can't break it (same resistance as bedrock)
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion)
    {
        if (level.getBlockEntity(pos) instanceof CBTintableChestBlockEntity chest && chest.getLock() != CBTintableChestBlockEntity.Lock.NONE)
        {
            return 3_600_000f;
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    // ---- Double chests and locks ----

    // A chest can't join onto someone else's locked chest: it's placed as a single chest instead
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || state.getValue(TYPE) == ChestType.SINGLE) return state;

        BlockPos partnerPos = context.getClickedPos().relative(getConnectedDirection(state));
        if (context.getLevel().getBlockEntity(partnerPos) instanceof CBTintableChestBlockEntity partner && !partner.canUse(context.getPlayer()))
        {
            return state.setValue(TYPE, ChestType.SINGLE);
        }
        return state;
    }

    // Joining the owner's own locked chest: the new half takes the same lock and knob colour
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (state.getValue(TYPE) == ChestType.SINGLE || !(level.getBlockEntity(pos) instanceof CBTintableChestBlockEntity placed)) return;

        BlockPos partnerPos = pos.relative(getConnectedDirection(state));
        if (level.getBlockEntity(partnerPos) instanceof CBTintableChestBlockEntity partner && partner.getLock() != CBTintableChestBlockEntity.Lock.NONE)
        {
            placed.setLock(partner.getLock(), partner.getOwner());
            placed.setColor(KNOB_REGION, partner.getColor(KNOB_REGION));
        }
    }
}
