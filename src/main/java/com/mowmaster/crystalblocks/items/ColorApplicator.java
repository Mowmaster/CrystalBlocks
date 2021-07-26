package com.mowmaster.crystalblocks.items;

import com.mowmaster.crystalblocks.blocks.CrystalBlock;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.crystalblocks.blocks.CrystalBlock.*;
import static com.mowmaster.crystalblocks.reference.References.MODID;

public class ColorApplicator extends Item {
    private List<Integer> storedColors = new ArrayList<Integer>();

    public ColorApplicator(Properties p_41383_) {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        Level world = p_41427_.getLevel();
        Player player = p_41427_.getPlayer();
        InteractionHand hand = p_41427_.getHand();
        BlockState state = world.getBlockState(p_41427_.getClickedPos());
        if(state.getBlock() instanceof CrystalBlock)
        {
            int red = state.getValue(COLOR_RED);
            int green = state.getValue(COLOR_GREEN);
            int blue = state.getValue(COLOR_BLUE);

            getColorList(player.getItemInHand(hand));
            addSavedColor(ColorReference.getColor(Arrays.asList(red,green,blue)));
            ItemStack newStack = ColorReference.addColorToItemStack(player.getItemInHand(hand).copy(),red,green,blue);
            saveColorList(newStack);
            player.setItemInHand(hand,newStack);
        }

        if(player.isCrouching() && !(state.getBlock() instanceof CrystalBlock))
        {
            getColorList(player.getItemInHand(hand));
            int currentColor = ColorReference.getColorFromItemStack(player.getItemInHand(hand));
            int currentListPos = storedColors.indexOf(currentColor);
            int setColorPos = currentListPos+1;

            if(setColorPos>=8 || setColorPos>=storedColors.size())
            {
                setColorPos = 0;
            }
            ItemStack newStack = ColorReference.addColorToItemStackInt(player.getItemInHand(hand).copy(),storedColors.indexOf(setColorPos));
            player.setItemInHand(hand,newStack);
        }
        return super.useOn(p_41427_);
    }

    private void addSavedColor(int colorValue)
    {
        if(storedColors.size()<=8)
        {
            storedColors.add(colorValue);
        }
        else if(!storedColors.contains(colorValue))
        {
            storedColors.remove(0);
            storedColors.add(colorValue);
        }
    }

    private void saveColorList(ItemStack stack)
    {
        List<Integer> colorList= storedColors;
        CompoundTag blockColors = stack.getOrCreateTag();
        blockColors.putIntArray(MODID+"_colorlist",colorList);
        stack.setTag(blockColors);
    }

    private void getColorList(ItemStack stack)
    {
        if(stack.hasTag())
        {
            CompoundTag nbt = stack.getTag();
            if(nbt.contains(MODID+"_colorlist"))
            {
                List<Integer> listy = new ArrayList<>();
                int[] list = nbt.getIntArray(MODID+"_colorlist");
                for(int i=0;i<list.length;i++)
                {
                    listy.add(list[i]);
                }
                storedColors = listy;
            }
        }
    }





    public static final Item ITM_APPLICATOR = new ColorApplicator(new Item.Properties()){}.setRegistryName(new ResourceLocation(MODID, "applicator"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_APPLICATOR);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_APPLICATOR);
    }

}
