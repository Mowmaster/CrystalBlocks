package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.mowlib.Blocks.BaseColoredBlock;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class ColoredStoneBlock extends BaseColoredBlock
{
    public ColoredStoneBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(ColorReference.addColorToBlockState(this.defaultBlockState(),ColorReference.DEFAULTCOLOR));
    }
}
