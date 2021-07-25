package com.mowmaster.crystalblocks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

//https://stackoverflow.com/questions/4255973/calculation-of-a-mixed-color-in-rgb
//SOme sort of amethyst block that can grow into another color given differnt crystals nearby
// use this for crystals??? SeaPickleBlock
public class CrystalBlock extends Block {

    public static final IntegerProperty COLOR_RED = IntegerProperty.create("color_red", 0, 4);
    public static final IntegerProperty COLOR_GREEN = IntegerProperty.create("color_green", 0, 4);
    public static final IntegerProperty COLOR_BLUE = IntegerProperty.create("color_blue", 0, 4);

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public CrystalBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_RED, Integer.valueOf(4)).setValue(COLOR_GREEN, Integer.valueOf(4)).setValue(COLOR_BLUE, Integer.valueOf(4)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56120_) {
        p_56120_.add(COLOR_RED).add(COLOR_GREEN).add(COLOR_BLUE);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_56089_) {
        BlockState blockstate = p_56089_.getLevel().getBlockState(p_56089_.getClickedPos());
        if (blockstate.is(this))
        {
            return blockstate.setValue(COLOR_RED, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_RED)))).setValue(COLOR_GREEN, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_GREEN)))).setValue(COLOR_BLUE, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_BLUE))));
        }
        else return super.getStateForPlacement(p_56089_);
    }

    @Override
    public void onPlace(BlockState p_60566_, Level p_60567_, BlockPos p_60568_, BlockState p_60569_, boolean p_60570_) {
        super.onPlace(p_60566_, p_60567_, p_60568_, p_60569_, p_60570_);
    }



    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        if(p_60506_.getItemInHand(p_60507_).getItem().equals(Items.RED_DYE))
        {
            int current = p_60503_.getValue(COLOR_RED);
            int next = current+=1;
            if (current>4)next = 0;
            BlockState newState = p_60503_.setValue(COLOR_RED,next).setValue(COLOR_GREEN,p_60503_.getValue(COLOR_GREEN)).setValue(COLOR_BLUE,p_60503_.getValue(COLOR_BLUE));
            System.out.println("COLOR MATH: "+getColor(newState));
            p_60504_.setBlock(p_60505_,newState,3);
            //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
            return InteractionResult.SUCCESS;
        }
        else if(p_60506_.getItemInHand(p_60507_).getItem().equals(Items.GREEN_DYE))
        {
            int current = p_60503_.getValue(COLOR_GREEN);
            int next = current+=1;
            if (current>4)next = 0;
            BlockState newState = p_60503_.setValue(COLOR_RED,p_60503_.getValue(COLOR_RED)).setValue(COLOR_GREEN,next).setValue(COLOR_BLUE,p_60503_.getValue(COLOR_BLUE));
            System.out.println("COLOR MATH: "+getColor(newState));
            p_60504_.setBlock(p_60505_,newState,3);
            //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
            return InteractionResult.SUCCESS;
        }
        else if(p_60506_.getItemInHand(p_60507_).getItem().equals(Items.BLUE_DYE))
        {
            int current = p_60503_.getValue(COLOR_BLUE);
            int next = current+=1;
            if (current>4)next = 0;
            BlockState newState = p_60503_.setValue(COLOR_RED,p_60503_.getValue(COLOR_RED)).setValue(COLOR_GREEN,p_60503_.getValue(COLOR_GREEN)).setValue(COLOR_BLUE,next);
            System.out.println("COLOR MATH: "+getColor(newState));
            p_60504_.setBlock(p_60505_,newState,3);
            //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
            return InteractionResult.SUCCESS;
        }

        return super.use(p_60503_, p_60504_, p_60505_, p_60506_, p_60507_, p_60508_);
    }

    public static int getColor(BlockState state){
        if(state.getBlock() instanceof CrystalBlock)
        {
            double r = state.getValue(COLOR_RED)/4;
            double g = state.getValue(COLOR_GREEN)/4;
            double b = state.getValue(COLOR_BLUE)/4;

            int red = (int)Math.floor(255*r);
            int green = (int)Math.floor(255*g);
            int blue = (int)Math.floor(255*b);

            return (red*65536)+(green*256)+(blue);
        }
        else return 16777216;
    }

    private static final ResourceLocation RES_BLOCK_STONE = new ResourceLocation("crystalblocks", "block_stone");
    public static final Block BLK_BLOCK_STONE = new CrystalBlock(Block.Properties.of(Material.STONE, MaterialColor.NONE).strength(2.0F, 10.0F).sound(SoundType.STONE)).setRegistryName(RES_BLOCK_STONE);
    public static final Item ITM_BLOCK_STONE = new BlockItem(BLK_BLOCK_STONE, new Item.Properties()){}.setRegistryName(RES_BLOCK_STONE);


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_BLOCK_STONE);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLK_BLOCK_STONE);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return 16777216;} else {return -1;}},ITM_BLOCK_STONE);
    }

    public static void handleBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return getColor(blockstate);} else {return -1;}},BLK_BLOCK_STONE);
    }
}
