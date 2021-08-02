package com.mowmaster.crystalblocks.items;

import com.mowmaster.crystalblocks.blocks.CrystalBlock;
import com.mowmaster.crystalblocks.blocks.CrystalSlab;
import com.mowmaster.crystalblocks.blocks.CrystalStair;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.crystalblocks.CrystalBlocks.CRYSTAL_TAB;
import static com.mowmaster.crystalblocks.blocks.CrystalBlock.*;
import static com.mowmaster.crystalblocks.reference.References.MODID;

public class ColorApplicator extends Item {

    public ColorApplicator(Properties p_41383_) {
        super(new Properties().stacksTo(1).tab(CRYSTAL_TAB));
    }

    /*@Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        Level world = p_41427_.getLevel();
        Player player = p_41427_.getPlayer();
        InteractionHand hand = p_41427_.getHand();
        BlockState state = world.getBlockState(p_41427_.getClickedPos());
        ItemStack stackInHand = player.getItemInHand(hand);
        //Build Color List from NBT
        List<Integer> list = getColorList(stackInHand);

        if(player.isCrouching())
        {
            if(state.getBlock() instanceof CrystalBlock)
            {
                int red = state.getValue(COLOR_RED);
                int green = state.getValue(COLOR_GREEN);
                int blue = state.getValue(COLOR_BLUE);

                ItemStack newStack = ColorReference.addColorToItemStack(player.getItemInHand(hand).copy(),red,green,blue);
                saveColorList(newStack,addSavedColor(stackInHand,ColorReference.getColor(Arrays.asList(red,green,blue))));
                player.setItemInHand(hand,newStack);
                return InteractionResult.PASS;
            }
            else
            {
                int currentColor = ColorReference.getColorFromItemStack(stackInHand);
            int currentListPos = list.indexOf(currentColor);
            //System.out.println("CurrentPos: "+ currentListPos);
            int setColorPos = currentListPos+1;

            //System.out.println("CurrentPos: "+ (setColorPos>=8 || setColorPos>=list.size()));
            if(setColorPos>=8 || setColorPos>=list.size())
            {
                setColorPos = 0;
            }

            //System.out.println("NewPos: "+ setColorPos);

            if(list.size()>0)
            {
                ItemStack newStack = ColorReference.addColorToItemStackInt(player.getItemInHand(hand).copy(),list.get(setColorPos));
                player.setItemInHand(hand,newStack);
            }
            }
        }

        return super.useOn(p_41427_);
    }*/

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);
        //Build Color List from NBT
        if(stackInHand.getItem() instanceof ColorApplicator)
        {
            List<Integer> list = getColorList(stackInHand);
            if(player.isCrouching() || player.getAbilities().flying)
            {
                HitResult result = player.pick(5,0,false);
                //System.out.println(result.getType());

                if(result.getType().equals(HitResult.Type.MISS))
                {
                    int currentColor = ColorReference.getColorFromItemStack(stackInHand);
                    int currentListPos = list.indexOf(currentColor);
                    //System.out.println("CurrentPos: "+ currentListPos);
                    int setColorPos = currentListPos+1;

                    //System.out.println("CurrentPos: "+ (setColorPos>=8 || setColorPos>=list.size()));
                    if(setColorPos>=8 || setColorPos>=list.size())
                    {
                        setColorPos = 0;
                    }

                    //System.out.println("NewPos: "+ setColorPos);

                    if(list.size()>0)
                    {
                        ItemStack newStack = ColorReference.addColorToItemStackInt(player.getItemInHand(hand).copy(),list.get(setColorPos));
                        player.setItemInHand(hand,newStack);
                    }
                }
                else if(result.getType().equals(HitResult.Type.BLOCK))
                {

                    double rayLength = 5.0d;
                    Vec3 playerRot = player.getViewVector(0);
                    Vec3 rayPath = playerRot.scale(rayLength);

                    Vec3 playerEyes = player.getEyePosition(0);
                    Vec3 blockEyes = playerEyes.add(rayPath);

                    ClipContext clipContext = new ClipContext(playerEyes,blockEyes, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY,null);
                    BlockHitResult blockResult = world.clip(clipContext);
                    BlockState state = world.getBlockState(blockResult.getBlockPos());

                    if(state.getBlock() instanceof CrystalBlock || state.getBlock() instanceof CrystalStair || state.getBlock() instanceof CrystalSlab)
                    {
                        int red = state.getValue(COLOR_RED);
                        int green = state.getValue(COLOR_GREEN);
                        int blue = state.getValue(COLOR_BLUE);

                        ItemStack newStack = ColorReference.addColorToItemStack(player.getItemInHand(hand).copy(),red,green,blue);
                        saveColorList(newStack,addSavedColor(stackInHand,ColorReference.getColor(Arrays.asList(red,green,blue))));
                        player.setItemInHand(hand,newStack);
                    }
                    else
                    {
                        int currentColor = ColorReference.getColorFromItemStack(stackInHand);
                        int currentListPos = list.indexOf(currentColor);
                        //System.out.println("CurrentPos: "+ currentListPos);
                        int setColorPos = currentListPos+1;

                        //System.out.println("CurrentPos: "+ (setColorPos>=8 || setColorPos>=list.size()));
                        if(setColorPos>=8 || setColorPos>=list.size())
                        {
                            setColorPos = 0;
                        }

                        //System.out.println("NewPos: "+ setColorPos);

                        if(list.size()>0)
                        {
                            ItemStack newStack = ColorReference.addColorToItemStackInt(player.getItemInHand(hand).copy(),list.get(setColorPos));
                            player.setItemInHand(hand,newStack);
                        }
                    }
                }
            }
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public static List<Integer> addSavedColor(ItemStack stack, int colorValue)
    {
        List<Integer> list = getColorList(stack);
        if(list.size()<8 && !list.contains(colorValue))
        {
            list.add(colorValue);
            return list;
        }
        else if(!list.contains(colorValue))
        {
            list.remove(0);
            list.add(colorValue);
            return list;
        }
        return list;
    }

    public static void saveColorList(ItemStack stack, List<Integer> list)
    {
        CompoundTag blockColors = stack.getOrCreateTag();
        blockColors.putIntArray(MODID+"_colorlist",list);
        stack.setTag(blockColors);
    }

    public static List<Integer> getColorList(ItemStack stack)
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
                return listy;
            }
        }
        return new ArrayList<Integer>();
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        List<Integer> list = getColorList(p_41421_);
        super.getTooltipImage(p_41421_);
        p_41423_.add((new TranslatableComponent("item.minecraft.bundle.fullness", list.size(), 8)).withStyle(ChatFormatting.GRAY));
        for(int i=0;i<list.size();i++)
        {
            TranslatableComponent minNeeded = new TranslatableComponent( list.get(i).toString());
            minNeeded.withStyle(ChatFormatting.WHITE);
            p_41423_.add(minNeeded);
        }
        //
    }

    /*public Optional<TooltipComponent> getTooltipImage(ItemStack p_150775_) {
        getColorList(p_150775_);
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        for(int i=0; i<this.storedColors.size();i++)
        {
            nonnulllist.add(i,ColorReference.addColorToItemStackInt(new ItemStack(ITM_APPLICATOR),this.storedColors.get(i)));
        }
        return Optional.of(new BundleTooltip(nonnulllist, 8));
    }*/

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
