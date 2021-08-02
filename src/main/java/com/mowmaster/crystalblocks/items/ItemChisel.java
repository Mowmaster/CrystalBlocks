package com.mowmaster.crystalblocks.items;

import com.mowmaster.crystalblocks.blocks.CrystalBlock;
import com.mowmaster.crystalblocks.blocks.CrystalSlab;
import com.mowmaster.crystalblocks.blocks.CrystalStair;
import com.mowmaster.crystalblocks.reference.BlockReference;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

import static com.mowmaster.crystalblocks.CrystalBlocks.CRYSTAL_TAB;
import static com.mowmaster.crystalblocks.blocks.CrystalBlock.*;
import static com.mowmaster.crystalblocks.reference.References.MODID;

public class ItemChisel extends Item {

    public ItemChisel(Properties p_41383_) {
        super(new Properties().stacksTo(1).tab(CRYSTAL_TAB));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        Level world = p_41432_;
        Player player = p_41433_;
        InteractionHand hand = p_41434_;
        ItemStack stackInHand = player.getItemInHand(hand);
        //Build Color List from NBT
        if(stackInHand.getItem() instanceof ItemChisel)
        {
            HitResult result = player.pick(5,0,false);
            if(result.getType().equals(HitResult.Type.BLOCK))
            {

                double rayLength = 5.0d;
                Vec3 playerRot = player.getViewVector(0);
                Vec3 rayPath = playerRot.scale(rayLength);

                Vec3 playerEyes = player.getEyePosition(0);
                Vec3 blockEyes = playerEyes.add(rayPath);

                ClipContext clipContext = new ClipContext(playerEyes,blockEyes, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY,null);
                BlockHitResult blockResult = world.clip(clipContext);
                BlockState state = world.getBlockState(blockResult.getBlockPos());

                //When flying, crouch doesnt seem to be recognised...
                if(player.isCrouching() || player.getAbilities().flying)
                {
                    if(state.getBlock() instanceof CrystalBlock)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getBlock(state,true),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                    else if(state.getBlock() instanceof CrystalStair)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getStair(state,true),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                    else if(state.getBlock() instanceof CrystalSlab)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getSlab(state,true),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                }
                /*else
                {
                    if(state.getBlock() instanceof CrystalBlock)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getBlock(state,false),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                    else if(state.getBlock() instanceof CrystalStair)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getStair(state,false),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                    else if(state.getBlock() instanceof CrystalSlab)
                    {
                        world.setBlock(blockResult.getBlockPos(), BlockReference.getSlab(state,false),3);
                        return super.use(p_41432_, p_41433_, p_41434_);
                    }
                }*/
            }
        }
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    public static final Item ITM_CHISEL = new ItemChisel(new Properties()){}.setRegistryName(new ResourceLocation(MODID, "chisel"));

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_CHISEL);
    }

}
