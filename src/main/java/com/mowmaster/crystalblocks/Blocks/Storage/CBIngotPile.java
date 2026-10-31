package com.mowmaster.crystalblocks.Blocks.Storage;

import com.mowmaster.mowlib.storage.CBBulkStorageBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// A pallet of ingots (tinted_storage_ingot): stores up to 3456 (54 stacks) of one kind of ingot (c:ingots).
// Region 0 = the wooden pallet, region 1 = the ingots.
public class CBIngotPile extends CBBulkStorageBlock
{
    public static final int CAPACITY = 54 * 64;
    public static final int PALLET_REGION = 0;
    public static final int INGOT_REGION = 1;

    // The pallet (y 0-2) and the stack of ingots on it (up to y 14)
    private static final VoxelShape SHAPE = CBPixelBox.union(List.of(
            new CBPixelBox(0, 0, 0, 16, 2, 16),
            new CBPixelBox(0.5, 2, 0.5, 15.5, 14, 15.5)));

    public CBIngotPile(Properties properties)
    {
        super(properties, CAPACITY, Tags.Items.INGOTS);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected double topHeight()
    {
        return 14 / 16.0;
    }

    @Override
    public int getTintRegionCount()
    {
        return 2;
    }

    // Anything below the top of the pallet is the pallet
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        return CBPixelBox.pixelInside(hit).y < 2 ? PALLET_REGION : INGOT_REGION;
    }
}
