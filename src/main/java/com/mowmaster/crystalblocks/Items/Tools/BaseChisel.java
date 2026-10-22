package com.mowmaster.crystalblocks.Items.Tools;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.Blocks.Colorable.IColorableBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class BaseChisel extends Item {
    public BaseChisel(Properties properties) {
        super(properties);
    }



    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Called when rightclicking on a block with this item
        Level level = context.getLevel();
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();
        ItemStack offhand = context.getPlayer().getOffhandItem();

        if(!level.isClientSide()) {
            if(clickedBlock instanceof IColorableBlock crystalStone)
            {

            }
        }

        return InteractionResult.SUCCESS;
    }
}
