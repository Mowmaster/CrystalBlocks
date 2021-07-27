package com.mowmaster.crystalblocks.blocks;

import com.mowmaster.crystalblocks.items.ColorApplicator;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import static com.mowmaster.crystalblocks.CrystalBlocks.CRYSTAL_TAB;
import static com.mowmaster.crystalblocks.reference.References.MODID;

public class CrystalStair extends StairBlock {

    public static final IntegerProperty COLOR_RED = IntegerProperty.create("color_red", 0, 3);
    public static final IntegerProperty COLOR_GREEN = IntegerProperty.create("color_green", 0, 3);
    public static final IntegerProperty COLOR_BLUE = IntegerProperty.create("color_blue", 0, 3);

    public CrystalStair(BlockState state, Properties properties) {
        super(state, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_RED, Integer.valueOf(3)).setValue(COLOR_GREEN, Integer.valueOf(3)).setValue(COLOR_BLUE, Integer.valueOf(3)));

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56932_) {
        super.createBlockStateDefinition(p_56932_);
        p_56932_.add(COLOR_RED).add(COLOR_GREEN).add(COLOR_BLUE);

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_56089_) {
        BlockState blockstate = p_56089_.getLevel().getBlockState(p_56089_.getClickedPos());
        if (blockstate.is(this))
        {
            return super.getStateForPlacement(p_56089_).setValue(COLOR_RED, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_RED)))).setValue(COLOR_GREEN, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_GREEN)))).setValue(COLOR_BLUE, Integer.valueOf(Math.min(3, blockstate.getValue(COLOR_BLUE))));
        }
        else return super.getStateForPlacement(p_56089_);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack picked = state.getBlock().getCloneItemStack(world, pos, state);
        return ColorReference.addColorToItemStack(picked,state.getValue(COLOR_RED),state.getValue(COLOR_GREEN),state.getValue(COLOR_BLUE));
    }

    @Override
    public void setPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack p_49851_) {
        if(p_49850_ instanceof Player)
        {
            Player player = ((Player)p_49850_);
            if(player.getOffhandItem().getItem() instanceof ColorApplicator)
            {
                int[] colored = ColorReference.getRGBColorFromItemStack(player.getOffhandItem());
                BlockState newState = p_49849_.setValue(COLOR_RED,colored[0]).setValue(COLOR_GREEN,colored[1]).setValue(COLOR_BLUE,colored[2]);
                p_49847_.setBlock(p_49848_,newState,3);
            }
            else
            {
                int[] colored = ColorReference.getRGBColorFromItemStack(p_49851_);
                BlockState newState = p_49849_.setValue(COLOR_RED,colored[0]).setValue(COLOR_GREEN,colored[1]).setValue(COLOR_BLUE,colored[2]);
                p_49847_.setBlock(p_49848_,newState,3);
            }
        }
        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        if(p_60506_.getItemInHand(p_60507_).getItem() instanceof ColorApplicator)
        {
            int[] COLORS = ColorReference.getRGBColorFromItemStack(p_60506_.getItemInHand(p_60507_));
            BlockState newState = p_60503_.setValue(COLOR_RED,COLORS[0]).setValue(COLOR_GREEN,COLORS[1]).setValue(COLOR_BLUE,COLORS[2]);
            p_60504_.setBlock(p_60505_,newState,3);
            //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
            return InteractionResult.SUCCESS;
        }

        return super.use(p_60503_, p_60504_, p_60505_, p_60506_, p_60507_, p_60508_);
    }

    /*@Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if(!world.isClientSide())playerWillDestroy(world,pos,state,player);
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }*/

    @Override
    public void playerWillDestroy(Level p_56212_, BlockPos p_56213_, BlockState p_56214_, Player p_56215_) {

        if(!p_56212_.isClientSide())
        {
            if (p_56214_.getBlock() instanceof CrystalStair) {
                if (!p_56212_.isClientSide && !p_56215_.isCreative()) {
                    ItemStack itemstack = new ItemStack(this);
                    List<Integer> getRGB = ColorReference.getRGBFromState(p_56214_);
                    ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getRGB.get(0),getRGB.get(1),getRGB.get(2));
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(p_56212_, (double)p_56213_.getX() + 0.5D, (double)p_56213_.getY() + 0.5D, (double)p_56213_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_56212_.addFreshEntity(itementity);
                }
            }
        }
        super.playerWillDestroy(p_56212_, p_56213_, p_56214_, p_56215_);
    }

    private static final ResourceLocation RES_STAIR_STONE = new ResourceLocation(MODID, "stair_stone");
    public static final Block BLK_STAIR_STONE = new CrystalStair(CrystalBlock.BLK_BLOCK_STONE.defaultBlockState(), BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_STONE)).setRegistryName(RES_STAIR_STONE);
    public static final Item ITM_STAIR_STONE = new BlockItem(BLK_STAIR_STONE, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_STAIR_STONE);

    private static final ResourceLocation RES_STAIR_BRICK = new ResourceLocation(MODID, "stair_brick");
    public static final Block BLK_STAIR_BRICK = new CrystalStair(CrystalBlock.BLK_BLOCK_BRICK.defaultBlockState(), BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_BRICK)).setRegistryName(RES_STAIR_BRICK);
    public static final Item ITM_STAIR_BRICK = new BlockItem(BLK_STAIR_BRICK, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_STAIR_BRICK);

    private static final ResourceLocation RES_STAIR_BRICKS = new ResourceLocation(MODID, "stair_bricks");
    public static final Block BLK_STAIR_BRICKS = new CrystalStair(CrystalBlock.BLK_BLOCK_BRICKS.defaultBlockState(), BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_BRICKS)).setRegistryName(RES_STAIR_BRICKS);
    public static final Item ITM_STAIR_BRICKS = new BlockItem(BLK_STAIR_BRICKS, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_STAIR_BRICKS);

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_STAIR_STONE);
        event.getRegistry().register(ITM_STAIR_BRICK);
        event.getRegistry().register(ITM_STAIR_BRICKS);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLK_STAIR_STONE);
        event.getRegistry().register(BLK_STAIR_BRICK);
        event.getRegistry().register(BLK_STAIR_BRICKS);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_STAIR_STONE);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_STAIR_BRICK);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_STAIR_BRICKS);

    }

    public static void handleBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_STAIR_STONE);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_STAIR_BRICK);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_STAIR_BRICKS);

    }
}
