package com.mowmaster.crystalblocks.Blocks.Colorable;

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
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

// A lantern that hangs from a chain of its size (chain beam, chain cap, a chained support beam's lower side, or a
// vanilla chain for the small size), lined up with it. With no chain above it stands on the floor instead
// (hanging = false), and switches back to hanging when a chain is placed on top.
// The cap versions have their own plate and hang from the underside of a full block instead.
// Treated as an always-upright chain cap of the chain's kind, so the beam maths (positions, line-up, chained
// support beam joins) all work unchanged.
// Lit: light 15 and a flame inside. Stays lit in water; wind charges and shears put it out; a torch or flint and
// steel relights it. If nothing holds it up it falls, and shatters on landing, starting a fire if it was lit.
// Region 0 = metal, region 1 = glass (clear until first dyed, like the windows).
public class CBChainLantern extends CBBoltedBeamBlock implements SimpleWaterloggedBlock, Fallable
{
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty TINTED_GLASS = CBTintableWindow.TINTED_GLASS;
    // true = hanging from above, false = standing on the floor. Cap lanterns always hang.
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    public static final int METAL_REGION = 0;
    public static final int GLASS_REGION = 1;

    // The middle of the block: what standing lanterns centre on, and where vanilla chains run
    private static final CBPixelBox BLOCK_CENTRE = new CBPixelBox(8, 0, 8, 8, 16, 8);

    // Standing lanterns only have the top loop, not the chain running up to the block above
    private final Map<BlockState, VoxelShape> standingShapes = new HashMap<>();

    public CBChainLantern(Properties properties)
    {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(LIT, true)
                .setValue(WATERLOGGED, false)
                .setValue(TINTED_GLASS, false)
                .setValue(HANGING, true));

        List<CBPixelBox> standing = standingBoxes();
        for (BlockState state : stateDefinition.getPossibleStates())
        {
            if (!state.getValue(HANGING))
            {
                standingShapes.put(state, CBPixelBox.union(standing.stream().map(box -> place(box, state)).toList()));
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, WATERLOGGED, TINTED_GLASS, HANGING);
    }

    // ---- Variants (must return constants: they're read while the block is being built) ----

    public CBChainSize size()
    {
        return CBChainSize.SMALL;
    }

    // true = has its own mounting plate and hangs from the underside of a full block; false = hangs from a chain
    public boolean hasPlate()
    {
        return false;
    }

    // ---- Beam setup ----

    @Override public Kind kind() { return size().kind(); }
    @Override public boolean isCap() { return true; }
    @Override public boolean alwaysUpright() { return true; }
    // Modelled hanging along Z with the chain's top at the north end, like the chain caps
    @Override public int modelXTurns() { return 3; }
    @Override protected CBPixelBox footprint() { return size().footprint(); }
    @Override public IntegerProperty offsetXProperty() { return size().offsetX(); }
    @Override public IntegerProperty offsetZProperty() { return size().offsetZ(); }

    // Boxes as modelled: the chain part (z 0-6, always first), the lantern body (z 6-16) and, for caps, the plate.
    // The medium size is modelled centred at y 10 like the medium chain, so the body is lifted to match.
    @Override
    protected List<List<CBPixelBox>> regionBoxes()
    {
        CBPixelBox chain = size().footprint();
        double dy = (chain.minY() + chain.maxY()) / 2 - 8;

        List<CBPixelBox> metal = new ArrayList<>();
        metal.add(new CBPixelBox(chain.minX(), chain.minY(), 0, chain.maxX(), chain.maxY(), 6));
        for (CBPixelBox box : List.of(
                new CBPixelBox(6, 6, 6, 10, 10, 7),       // top knob
                new CBPixelBox(5, 5, 7, 11, 11, 8),       // roof
                new CBPixelBox(4, 4, 8, 12, 12, 9),       // top plate
                new CBPixelBox(4, 4, 9, 5, 5, 15),        // corner posts
                new CBPixelBox(11, 4, 9, 12, 5, 15),
                new CBPixelBox(4, 11, 9, 5, 12, 15),
                new CBPixelBox(11, 11, 9, 12, 12, 15),
                new CBPixelBox(4, 4, 15, 12, 12, 16)))    // base
        {
            metal.add(box.offset(0, dy, 0));
        }
        if (hasPlate()) metal.add(new CBPixelBox(4, 4, 0, 12, 12, 0.25).offset(0, dy, 0));

        List<CBPixelBox> glass = List.of(new CBPixelBox(5, 5, 9, 11, 11, 15).offset(0, dy, 0));
        return List.of(metal, glass);
    }

    // The standing model's boxes: the same, but the chain part is just the top loop (from z 2.75)
    private List<CBPixelBox> standingBoxes()
    {
        List<CBPixelBox> boxes = new ArrayList<>();
        for (List<CBPixelBox> region : regionBoxes()) boxes.addAll(region);
        CBPixelBox chain = boxes.remove(0);
        boxes.add(new CBPixelBox(chain.minX(), chain.minY(), 2.75, chain.maxX(), chain.maxY(), 6));
        return boxes;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return state.getValue(HANGING) ? super.getShape(state, level, pos, context) : standingShapes.get(state);
    }

    // ---- Support ----

    // What a hanging lantern hangs from: the chain's box in the block above (in that block's pixels; only the
    // horizontal position matters), or null if nothing above can hold it
    @Nullable
    private CBPixelBox chainAbove(LevelReader level, BlockPos pos)
    {
        BlockState above = level.getBlockState(pos.above());

        // Vanilla chains run through the middle of their block and match the small size
        if (above.getBlock() instanceof ChainBlock)
        {
            return size() == CBChainSize.SMALL && above.getValue(ChainBlock.AXIS) == Direction.Axis.Y ? BLOCK_CENTRE : null;
        }
        if (!(above.getBlock() instanceof CBBoltedBeamBlock beam) || beam instanceof CBChainLantern) return null;

        // A vertical chain beam or cap of the same size (but not a cap with its plate on the bottom)
        if (beam.kind() == kind() && beam.facing(above).getAxis() == Direction.Axis.Y)
        {
            boolean plateUnderneath = beam.isCap() && beam.facing(above) == Direction.DOWN;
            return plateUnderneath ? null : beam.worldFootprint(above);
        }
        // Anything else with a chain spot on its lower side (a chained support beam lying sideways)
        return beam.connectionPoint(above, Direction.DOWN, this);
    }

    // A floor a standing lantern can sit on (the same check vanilla lanterns and torches use)
    private static boolean floorBelow(LevelReader level, BlockPos pos)
    {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    // What a chain lantern (not a cap) should be with its current surroundings: hanging, lined up with a chain
    // above; otherwise standing where it is, if there's a floor; otherwise null (nothing holds it, so it falls).
    // To make a hanging lantern always fall when its chain goes, return null instead of standing when
    // state.getValue(HANGING) is true.
    @Nullable
    private BlockState refit(BlockState state, LevelReader level, BlockPos pos)
    {
        CBPixelBox chain = chainAbove(level, pos);
        if (chain != null)
        {
            BlockState hanging = state.setValue(HANGING, true);
            return centred(worldFootprint(hanging), chain) ? hanging : bestFit(hanging, chain, true, 0);
        }
        return floorBelow(level, pos) ? state.setValue(HANGING, false) : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        if (hasPlate())
        {
            BlockPos abovePos = pos.above();
            return level.getBlockState(abovePos).isFaceSturdy(level, abovePos, Direction.DOWN, SupportType.FULL);
        }
        if (!state.getValue(HANGING)) return floorBelow(level, pos);
        CBPixelBox chain = chainAbove(level, pos);
        return chain != null && centred(worldFootprint(state), chain);
    }

    // Whether two boxes' centres are within half a pixel of each other horizontally
    private static boolean centred(CBPixelBox a, CBPixelBox b)
    {
        return Math.abs((a.minX() + a.maxX()) / 2 - (b.minX() + b.maxX()) / 2) <= 0.5
                && Math.abs((a.minZ() + a.maxZ()) / 2 - (b.minZ() + b.maxZ()) / 2) <= 0.5;
    }

    // ---- Placement ----

    // Caps sit where you clicked (the beam rules). Chain lanterns line up with a chain above if there is one,
    // otherwise stand centred on the floor, otherwise can't be placed.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState state;
        if (hasPlate())
        {
            state = super.getStateForPlacement(context);
        }
        else
        {
            CBPixelBox chain = chainAbove(level, pos);
            if (chain != null) state = bestFit(defaultBlockState(), chain, true, 0);
            else if (floorBelow(level, pos)) state = bestFit(defaultBlockState().setValue(HANGING, false), BLOCK_CENTRE, true, 0);
            else return null;
        }
        if (state == null) return null;
        return state.setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    // Hanging lanterns take the chain's colour for their metal, unless the item already gave them one
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        if (hasPlate())
        {
            super.setPlacedBy(level, pos, state, placer, stack);
            return;
        }
        if (!state.getValue(HANGING)) return;
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity placed) || placed.isDyed(METAL_REGION)) return;

        BlockPos abovePos = pos.above();
        // The chained support beam's chains are its region 1 (metal); chain beams only have region 0
        int chainRegion = level.getBlockState(abovePos).getBlock() instanceof CBChainedSupportBeam ? 1 : 0;
        if (level.getBlockEntity(abovePos) instanceof CBTintBlockEntity source && source.isDyed(chainRegion))
        {
            placed.setColor(METAL_REGION, source.getColor(chainRegion));
        }
    }

    // ---- Neighbours changing: switch between hanging and standing, or fall ----

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                              BlockPos pos, Direction direction, BlockPos neighborPos,
                                              BlockState neighborState, RandomSource random)
    {
        if (state.getValue(WATERLOGGED))
        {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction.getAxis() != Direction.Axis.Y) return state;

        BlockState fitted = hasPlate() ? (state.canSurvive(level, pos) ? state : null) : refit(state, level, pos);
        if (fitted == null)
        {
            // Nothing holds it: fall a tick later, like sand and gravel, after the change has finished
            scheduledTickAccess.scheduleTick(pos, this, 1);
            return state;
        }
        return fitted;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        boolean held = hasPlate() ? state.canSurvive(level, pos) : refit(state, level, pos) != null;
        if (!held)
        {
            // disableDrop: when it lands it never places or drops, it calls onBrokenAfterFall instead
            FallingBlockEntity.fall(level, pos, state).disableDrop();
        }
    }

    // Landed: shatter, and start a fire if it was lit and there's room for one
    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock)
    {
        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1f, 1f);
        if (!fallingBlock.getBlockState().getValue(LIT)) return;

        BlockState fire = BaseFireBlock.getState(level, pos);
        if (level.getBlockState(pos).canBeReplaced() && level.getFluidState(pos).isEmpty() && fire.canSurvive(level, pos))
        {
            level.setBlockAndUpdate(pos, fire);
        }
    }

    // ---- Right-click ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        boolean lit = state.getValue(LIT);

        // Shears put it out
        if (lit && stack.is(Tags.Items.TOOLS_SHEAR))
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
                stack.hurtAndBreak(1, player, hand);
                level.playSound(null, pos, SoundEvents.SHEARS_SNIP, SoundSource.BLOCKS, 1f, 1f);
                level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // A torch or flint and steel relights it (water doesn't matter)
        if (!lit && (stack.is(Items.TORCH) || stack.is(Items.FLINT_AND_STEEL)))
        {
            if (!level.isClientSide())
            {
                level.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
                if (stack.is(Items.FLINT_AND_STEEL)) stack.hurtAndBreak(1, player, hand);
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.SUCCESS;
        }

        // The first dye on the glass swaps the clear glass texture for the tintable one (same block, so the
        // block entity and the metal's colour are kept)
        if (!level.isClientSide() && IColorableBlock.dyeColorOf(stack) != null && !state.getValue(TINTED_GLASS)
                && getTintRegion(state, hitResult) == GLASS_REGION)
        {
            level.setBlock(pos, state.setValue(TINTED_GLASS, true), Block.UPDATE_ALL);
        }
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }

    // Wind charges trigger blocks rather than breaking them, the same way they blow out candles
    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer)
    {
        if (explosion.canTriggerBlocks() && state.getValue(LIT))
        {
            level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
        }
        super.onExplosionHit(state, level, pos, explosion, dropConsumer);
    }

    // ---- Water (it stays lit) ----

    @Override
    protected @NotNull FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ---- Flame ----

    // A torch flame in the middle of the glass, placed with the same maths as the model
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (!state.getValue(LIT)) return;

        CBPixelBox glass = regionBoxes().get(GLASS_REGION).get(0);
        double centreX = (glass.minX() + glass.maxX()) / 2;
        double centreY = (glass.minY() + glass.maxY()) / 2;
        double centreZ = (glass.minZ() + glass.maxZ()) / 2;
        CBPixelBox flame = place(new CBPixelBox(centreX, centreY, centreZ, centreX, centreY, centreZ), state);

        level.addParticle(ParticleTypes.FLAME,
                pos.getX() + flame.minX() / 16, pos.getY() + flame.minY() / 16, pos.getZ() + flame.minZ() / 16,
                0, 0, 0);
    }
}