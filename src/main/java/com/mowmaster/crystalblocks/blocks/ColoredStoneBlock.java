package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.mowlib.Blocks.BaseBlocks.BaseColoredBlock;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class ColoredStoneBlock extends BaseColoredBlock
{
    public ColoredStoneBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(MowLibColorReference.addColorToBlockState(this.defaultBlockState(),MowLibColorReference.DEFAULTCOLOR));
    }
}
