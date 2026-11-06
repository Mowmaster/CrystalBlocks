package com.mowmaster.crystalblocks.Blocks.Storage;

import com.mowmaster.mowlib.datamaps.CBBlockColor;
import com.mowmaster.mowlib.datamaps.CBDataMaps;
import com.mowmaster.mowlib.storage.CBBulkStorageBlock;
import com.mowmaster.mowlib.storage.CBBulkStorageBlockEntity;
import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// A heap of raw ore (models/block/ore_pile.json): stores up to 3456 (54 stacks) of one kind of raw ore (c:raw_materials).
// One tint region, drawn over a greyscale raw ore texture.
// - AUTO_TINT on (the default): the heap takes the colour of the stored ore's raw block from MowLib's block_colors data
//   map (raw_iron -> raw_iron_block), as soon as a player puts ore in, or within a minute or so for hoppers and pipes
//   (random ticks, since MowLib's bulk storage has no "contents changed" hook yet)
// - Dyeing it turns AUTO_TINT off and keeps the player's colour; sneak + right-click with an empty hand turns it back on
public class CBOrePile extends CBBulkStorageBlock
{
    public static final int CAPACITY = 54 * 64;
    public static final BooleanProperty AUTO_TINT = BooleanProperty.create("auto_tint");

    // The heap's footprint (up to y 4), the middle mound (up to y 8) and the lumps on top (up to y 11)
    private static final VoxelShape SHAPE = CBPixelBox.union(List.of(
            new CBPixelBox(0, 0, 0, 16, 4, 16),
            new CBPixelBox(4, 4, 4, 12, 8, 12),
            new CBPixelBox(5, 8, 6, 9, 11, 11)));

    public CBOrePile(Properties properties)
    {
        super(properties, CAPACITY, Tags.Items.RAW_MATERIALS);
        registerDefaultState(stateDefinition.any().setValue(AUTO_TINT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AUTO_TINT);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected double topHeight()
    {
        return 11 / 16.0;
    }

    // ---- Clicks ----

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        boolean dye = IColorableBlock.dyeColorOf(stack) != null;
        // An auto colour isn't the player's: start the dye from scratch instead of mixing it with the ore's colour
        if (dye && state.getValue(AUTO_TINT) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage)
        {
            storage.setColor(0, CBTintBlockEntity.UNDYED);
        }

        InteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (level.isClientSide() || !result.consumesAction()) return result;

        if (dye)
        {
            level.setBlock(pos, state.setValue(AUTO_TINT, false), Block.UPDATE_ALL);
        }
        else
        {
            // Ore may just have gone in
            updateAutoTint(level, pos, state);
        }
        return result;
    }

    // Sneak + right-click with an empty hand: back to the ore's own colour
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!player.isSecondaryUseActive() || state.getValue(AUTO_TINT)) return InteractionResult.PASS;
        if (!level.isClientSide())
        {
            BlockState auto = state.setValue(AUTO_TINT, true);
            level.setBlock(pos, auto, Block.UPDATE_ALL);
            updateAutoTint(level, pos, auto);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1f, 0.5f);
        }
        return InteractionResult.SUCCESS;
    }

    // Taking ore out may empty it
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player)
    {
        super.attack(state, level, pos, player);
        updateAutoTint(level, pos, state);
    }

    // ---- Auto tint ----

    // Random ticks catch ore put in by hoppers and pipes
    @Override
    protected boolean isRandomlyTicking(BlockState state)
    {
        return state.getValue(AUTO_TINT);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        updateAutoTint(level, pos, state);
    }

    // Sets the colour from the stored ore, if auto tint is on and the colour isn't already right.
    // An empty heap, or ore with no known raw block colour, goes back to undyed (the plain grey texture).
    public static void updateAutoTint(Level level, BlockPos pos, BlockState state)
    {
        if (level.isClientSide() || !state.hasProperty(AUTO_TINT) || !state.getValue(AUTO_TINT)) return;
        if (!(level.getBlockEntity(pos) instanceof CBBulkStorageBlockEntity storage)) return;

        // An ItemResource is an item plus its data components, without a count; slot 0 is the only slot
        ItemResource stored = storage.getItems().getResource(0);
        Integer color = stored.isEmpty() ? null : rawBlockColor(stored.getItem());
        int wanted = color == null ? CBTintBlockEntity.UNDYED : color;
        if (storage.getColor(0) != wanted) storage.setColor(0, wanted);
    }

    // The block_colors colour of the raw block for a raw ore item: <namespace>:raw_iron -> <namespace>:raw_iron_block.
    // Null if there's no such block or it has no colour.
    @Nullable
    public static Integer rawBlockColor(Item rawOre)
    {
        Identifier id = BuiltInRegistries.ITEM.getKey(rawOre);
        Identifier blockId = id.withSuffix("_block");
        // getOptional: empty instead of air when the block doesn't exist
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .map(block -> block.builtInRegistryHolder().getData(CBDataMaps.BLOCK_COLORS))
                .map(CBBlockColor::rgb)
                .orElse(null);
    }
}
