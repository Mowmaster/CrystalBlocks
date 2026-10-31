package com.mowmaster.crystalblocks.Blocks.Colorable;

import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class CBTintableSlab extends SlabBlock implements IColorableBlock
{
    public static final int BOTTOM_REGION = 0;
    public static final int TOP_REGION = 1;

    public CBTintableSlab(Properties properties)
    {
        super(properties);
    }

    @Override
    public int getTintRegionCount()
    {
        return 2;
    }

    // Single slabs dye their own half; a double slab dyes whichever half was clicked
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        return switch (state.getValue(TYPE))
        {
            case BOTTOM -> BOTTOM_REGION;
            case TOP -> TOP_REGION;
            // Height of the click inside the block: 0.0 = bottom face, 1.0 = top face
            case DOUBLE -> hit.getLocation().y - hit.getBlockPos().getY() >= 0.5 ? TOP_REGION : BOTTOM_REGION;
        };
    }

    // The single top slab model only uses tintindex 0, but its colour is stored in the top region.
    // The double slab model uses tintindex 0 = bottom and 1 = top, which already match the regions.
    @Override
    public int getRegionForTintIndex(BlockState state, int tintIndex)
    {
        return state.getValue(TYPE) == SlabType.TOP ? TOP_REGION : tintIndex;
    }

    // Single slab -> double slab: the added half is the opposite of the one that was already there
    @Override
    public int getAddedRegion(BlockState oldState, BlockState newState)
    {
        if (!oldState.is(this) || newState.getValue(TYPE) != SlabType.DOUBLE) return NO_REGION;
        return switch (oldState.getValue(TYPE))
        {
            case BOTTOM -> TOP_REGION;
            case TOP -> BOTTOM_REGION;
            case DOUBLE -> NO_REGION;
        };
    }

    // Runs after the item's components have been applied. If the new half still has no colour
    // (the item wasn't dyed), it copies the existing half's colour. An item colour is never overwritten.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity tintEntity)) return;

        int added = tintEntity.takeAddedRegion();
        if (added == NO_REGION || tintEntity.isDyed(added)) return;

        int existing = added == TOP_REGION ? BOTTOM_REGION : TOP_REGION;
        if (tintEntity.isDyed(existing)) tintEntity.setColor(added, tintEntity.getColor(existing));
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}