package com.mowmaster.crystalblocks.blocks;

import com.mowmaster.crystalblocks.items.ColorApplicator;
import com.mowmaster.crystalblocks.items.ItemChisel;
import com.mowmaster.crystalblocks.reference.BlockReference;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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

public class CrystalSlab extends SlabBlock {

    public static final IntegerProperty COLOR_RED = IntegerProperty.create("color_red", 0, 3);
    public static final IntegerProperty COLOR_GREEN = IntegerProperty.create("color_green", 0, 3);
    public static final IntegerProperty COLOR_BLUE = IntegerProperty.create("color_blue", 0, 3);

    public CrystalSlab(Properties p_56359_) {
        super(p_56359_);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_RED, Integer.valueOf(3)).setValue(COLOR_GREEN, Integer.valueOf(3)).setValue(COLOR_BLUE, Integer.valueOf(3)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56932_) {
        super.createBlockStateDefinition(p_56932_);
        p_56932_.add(COLOR_RED).add(COLOR_GREEN).add(COLOR_BLUE);

    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_56361_) {
        BlockPos blockpos = p_56361_.getClickedPos();
        BlockState blockstate = p_56361_.getLevel().getBlockState(blockpos);
        int red = 0;
        int green = 0;
        int blue = 0;
        if(p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ColorApplicator)
        {
            int[] color = ColorReference.getRGBColorFromItemStack(p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND));
            red = color[0];
            green = color[1];
            blue = color[2];
        }
        else
        {
            int[] color = ColorReference.getRGBColorFromItemStack(p_56361_.getPlayer().getItemInHand(p_56361_.getHand()));
            red = color[0];
            green = color[1];
            blue = color[2];
        }

        if (blockstate.is(this)
                && blockstate.getValue(COLOR_RED).equals(red)
                && blockstate.getValue(COLOR_GREEN).equals(green)
                && blockstate.getValue(COLOR_BLUE).equals(blue)) {
            return blockstate.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false))
                    .setValue(COLOR_RED, red).setValue(COLOR_GREEN, green).setValue(COLOR_BLUE, blue);
        }
        else if(blockstate.is(this)
                && (
                    !blockstate.getValue(COLOR_RED).equals(red)
                || !blockstate.getValue(COLOR_GREEN).equals(green)
                || !blockstate.getValue(COLOR_BLUE).equals(blue)
                ))
        {
            FluidState fluidstate = p_56361_.getLevel().getFluidState(blockpos);
            Direction direction = p_56361_.getClickedFace();

            p_56361_.getLevel().setBlock((direction != Direction.DOWN)?(blockpos.above()):(blockpos.below()),this.defaultBlockState().setValue(TYPE, (direction != Direction.DOWN)?(SlabType.BOTTOM):(SlabType.TOP)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER)).setValue(COLOR_RED, red).setValue(COLOR_GREEN, green).setValue(COLOR_BLUE, blue),3);
            if(!p_56361_.getPlayer().isCreative())p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND).shrink(1);
            p_56361_.getLevel().playSound(p_56361_.getPlayer(),blockpos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS,1.0F, 1.0F);
            return this.defaultBlockState().setValue(TYPE, blockstate.getValue(TYPE)).setValue(WATERLOGGED, blockstate.getValue(WATERLOGGED))
                    .setValue(COLOR_RED, blockstate.getValue(COLOR_RED)).setValue(COLOR_GREEN, blockstate.getValue(COLOR_GREEN)).setValue(COLOR_BLUE, blockstate.getValue(COLOR_BLUE));
        }
        else {
            FluidState fluidstate = p_56361_.getLevel().getFluidState(blockpos);
            BlockState blockstate1 = this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER)).setValue(COLOR_RED, red).setValue(COLOR_GREEN, green).setValue(COLOR_BLUE, blue);
            Direction direction = p_56361_.getClickedFace();
            return direction != Direction.DOWN && (direction == Direction.UP || !(p_56361_.getClickLocation().y - (double)blockpos.getY() > 0.5D)) ? blockstate1 : blockstate1.setValue(TYPE, SlabType.TOP);
        }
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

        if(p_60506_.getItemInHand(p_60507_).getItem() instanceof ItemChisel)
        {
            if(p_60503_.getBlock() instanceof CrystalSlab)
            {
                p_60504_.setBlock(p_60505_, BlockReference.getSlab(p_60503_,false),3);
                return InteractionResult.SUCCESS;
            }
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
            if (p_56214_.getBlock() instanceof CrystalSlab) {
                if (!p_56212_.isClientSide && !p_56215_.isCreative()) {
                    ItemStack itemstack = new ItemStack(this);
                    List<Integer> getRGB = ColorReference.getRGBFromState(p_56214_);
                    ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getRGB.get(0),getRGB.get(1),getRGB.get(2));
                    newStack.setCount((p_56214_.getValue(TYPE).equals(SlabType.DOUBLE))?(2):(1));
                    ItemEntity itementity = new ItemEntity(p_56212_, (double)p_56213_.getX() + 0.5D, (double)p_56213_.getY() + 0.5D, (double)p_56213_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_56212_.addFreshEntity(itementity);
                }
            }
        }
        super.playerWillDestroy(p_56212_, p_56213_, p_56214_, p_56215_);
    }

    private static final ResourceLocation RES_SLAB_STONE = new ResourceLocation(MODID, "slab_stone");
    public static final Block BLK_SLAB_STONE = new CrystalSlab( BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_STONE)).setRegistryName(RES_SLAB_STONE);
    public static final Item ITM_SLAB_STONE = new BlockItem(BLK_SLAB_STONE, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_SLAB_STONE);

    private static final ResourceLocation RES_SLAB_BRICK = new ResourceLocation(MODID, "slab_brick");
    public static final Block BLK_SLAB_BRICK = new CrystalSlab( BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_BRICK)).setRegistryName(RES_SLAB_BRICK);
    public static final Item ITM_SLAB_BRICK = new BlockItem(BLK_SLAB_BRICK, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_SLAB_BRICK);

    private static final ResourceLocation RES_SLAB_BRICKS = new ResourceLocation(MODID, "slab_bricks");
    public static final Block BLK_SLAB_BRICKS = new CrystalSlab( BlockBehaviour.Properties.copy(CrystalBlock.BLK_BLOCK_BRICKS)).setRegistryName(RES_SLAB_BRICKS);
    public static final Item ITM_SLAB_BRICKS = new BlockItem(BLK_SLAB_BRICKS, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_SLAB_BRICKS);

    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_SLAB_STONE);
        event.getRegistry().register(ITM_SLAB_BRICK);
        event.getRegistry().register(ITM_SLAB_BRICKS);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLK_SLAB_STONE);
        event.getRegistry().register(BLK_SLAB_BRICK);
        event.getRegistry().register(BLK_SLAB_BRICKS);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_SLAB_STONE);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_SLAB_BRICK);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_SLAB_BRICKS);

    }

    public static void handleBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_SLAB_STONE);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_SLAB_BRICK);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_SLAB_BRICKS);

    }
}
