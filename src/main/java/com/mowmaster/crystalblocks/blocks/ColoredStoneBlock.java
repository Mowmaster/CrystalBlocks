package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.mowlib.Blocks.BaseBlocks.BaseColoredBlock;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mowmaster.crystalblocks.Util.References.MODID;

public class ColoredStoneBlock extends BaseColoredBlock
{
    public ColoredStoneBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(MowLibColorReference.addColorToBlockState(this.defaultBlockState(),MowLibColorReference.DEFAULTCOLOR));
    }

    @Override
    public void appendHoverText(ItemStack p_49816_, @Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
        super.appendHoverText(p_49816_, p_49817_, p_49818_, p_49819_);

        p_49818_.add(Component.translatable(MODID + ".colortext").withStyle(ChatFormatting.GOLD).append(Component.translatable(MowLibReferences.MODID + "." + MowLibColorReference.getColorName(MowLibColorReference.getColorFromItemStackInt(p_49816_))).withStyle(ChatFormatting.WHITE)));
    }
}
