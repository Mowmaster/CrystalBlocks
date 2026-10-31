package com.mowmaster.crystalblocks.Blocks.Storage;

import com.mowmaster.mowlib.storage.CBBulkStorageBlock;
import com.mowmaster.mowlib.util.CBPixelBox;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// A stack of logs (tinted_storage_log): stores up to 3456 (54 stacks) of one kind of log (minecraft:logs).
// Four crossed layers of two logs. Region 0 = the bark, region 1 = the end grain.
public class CBLogPile extends CBBulkStorageBlock
{
    public static final int CAPACITY = 54 * 64;
    public static final int BARK_REGION = 0;
    public static final int END_REGION = 1;


    // Each log's outline, and its end grain stretched along the whole log (so clicks on either end land in it)
    private static final List<CBPixelBox> LOGS = new ArrayList<>();
    private static final List<CBPixelBox> ENDS = new ArrayList<>();

    static
    {
        for (int layer = 0; layer < 4; layer++)
        {
            double y = layer * 4;
            for (double offset : new double[] { 2.5, 9.5 })
            {
                if (layer % 2 == 0)
                {
                    // Running east-west
                    LOGS.add(new CBPixelBox(0, y, offset, 16, y + 4, offset + 4));
                    ENDS.add(new CBPixelBox(0, y + 0.5, offset + 0.5, 16, y + 3.5, offset + 3.5));
                }
                else
                {
                    // Running north-south
                    LOGS.add(new CBPixelBox(offset, y, 0, offset + 4, y + 4, 16));
                    ENDS.add(new CBPixelBox(offset + 0.5, y + 0.5, 0, offset + 3.5, y + 3.5, 16));
                }
            }
        }
    }

    private static final VoxelShape SHAPE = CBPixelBox.union(LOGS);

    public CBLogPile(Properties properties)
    {
        super(properties, CAPACITY, ItemTags.LOGS);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected double topHeight()
    {
        return 1.0;
    }

    @Override
    public int getTintRegionCount()
    {
        return 2;
    }

    // The end grain sits half a pixel inside each log's bark, so a click on a log's end lands inside its ENDS box
    @Override
    public int getTintRegion(BlockState state, BlockHitResult hit)
    {
        Vec3 pixel = CBPixelBox.pixelInside(hit);
        for (CBPixelBox end : ENDS)
        {
            if (end.contains(pixel)) return END_REGION;
        }
        return BARK_REGION;
    }
}
