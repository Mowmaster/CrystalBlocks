package com.mowmaster.crystalblocks.Items.Tools;

import com.mowmaster.crystalblocks.Blocks.BaseCrystalStone;
import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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

    private static final Map<Item, Block> DYE_MAPPER =
            Map.of(
                    Items.DYE.red(), CBBlockRegistry.CRYSTAL_STONE_RED.get(),
                    Items.DYE.green(), CBBlockRegistry.CRYSTAL_STONE_GREEN.get(),
                    Items.DYE.blue(), CBBlockRegistry.CRYSTAL_STONE_BLUE.get(),
                    Items.DYE.white(), CBBlockRegistry.CRYSTAL_STONE_WHITE.get(),
                    Items.DYE.black(), CBBlockRegistry.CRYSTAL_STONE_BLACK.get()
            );

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Called when rightclicking on a block with this item
        Level level = context.getLevel();
        Block clickedBlock = level.getBlockState(context.getClickedPos()).getBlock();
        ItemStack offhand = context.getPlayer().getOffhandItem();

        if(!level.isClientSide()) {
            if(clickedBlock instanceof BaseCrystalStone crystalStone)
            {
                System.out.println("Crystal Stone");

                System.out.println(offhand.getItemName() + "___offhand item");
                if(DYE_MAPPER.containsKey(offhand.getItem()))
                {
                    System.out.println("Dye Exists");
                    level.setBlockAndUpdate(context.getClickedPos(), DYE_MAPPER.get(offhand.getItem()).defaultBlockState());
                    level.playSound((Player) null,context.getClickedPos(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.BLOCKS, 2f, 1f);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }
}
