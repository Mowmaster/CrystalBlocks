package com.mowmaster.crystalblocks.blocks;

import com.mowmaster.crystalblocks.items.ColorApplicator;
import com.mowmaster.crystalblocks.items.ItemChisel;
import com.mowmaster.crystalblocks.reference.BlockReference;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.crystalblocks.CrystalBlocks.CRYSTAL_TAB;
import static com.mowmaster.crystalblocks.reference.References.MODID;

//https://stackoverflow.com/questions/4255973/calculation-of-a-mixed-color-in-rgb
//SOme sort of amethyst block that can grow into another color given differnt crystals nearby
// use this for crystals??? SeaPickleBlock
public class CrystalBlock extends Block {

    public static final IntegerProperty COLOR_RED = IntegerProperty.create("color_red", 0, 3);
    public static final IntegerProperty COLOR_GREEN = IntegerProperty.create("color_green", 0, 3);
    public static final IntegerProperty COLOR_BLUE = IntegerProperty.create("color_blue", 0, 3);

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public CrystalBlock(BlockBehaviour.Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_RED, Integer.valueOf(3)).setValue(COLOR_GREEN, Integer.valueOf(3)).setValue(COLOR_BLUE, Integer.valueOf(3)));
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
            if(p_60503_.getBlock() instanceof CrystalBlock)
            {
                p_60504_.setBlock(p_60505_, BlockReference.getBlock(p_60503_,false),3);
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
            if (p_56214_.getBlock() instanceof CrystalBlock) {
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

    private static final ResourceLocation RES_BLOCK_STONE = new ResourceLocation(MODID, "block_stone");
    public static final Block BLK_BLOCK_STONE = new CrystalBlock(Block.Properties.of(Material.STONE, MaterialColor.NONE).strength(1.5F, 6.0F).sound(SoundType.STONE)).setRegistryName(RES_BLOCK_STONE);
    public static final Item ITM_BLOCK_STONE = new BlockItem(BLK_BLOCK_STONE, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_BLOCK_STONE);

    private static final ResourceLocation RES_BLOCK_BRICK = new ResourceLocation(MODID, "block_brick");
    public static final Block BLK_BLOCK_BRICK = new CrystalBlock(Block.Properties.of(Material.STONE, MaterialColor.NONE).strength(1.5F, 6.0F).sound(SoundType.STONE)).setRegistryName(RES_BLOCK_BRICK);
    public static final Item ITM_BLOCK_BRICK = new BlockItem(BLK_BLOCK_BRICK, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_BLOCK_BRICK);

    private static final ResourceLocation RES_BLOCK_BRICKS = new ResourceLocation(MODID, "block_bricks");
    public static final Block BLK_BLOCK_BRICKS = new CrystalBlock(Block.Properties.of(Material.STONE, MaterialColor.NONE).strength(1.5F, 6.0F).sound(SoundType.STONE)).setRegistryName(RES_BLOCK_BRICKS);
    public static final Item ITM_BLOCK_BRICKS = new BlockItem(BLK_BLOCK_BRICKS, new Item.Properties().tab(CRYSTAL_TAB)){}.setRegistryName(RES_BLOCK_BRICKS);


    @SubscribeEvent
    public static void onItemRegistryReady(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(ITM_BLOCK_STONE);
        event.getRegistry().register(ITM_BLOCK_BRICK);
        event.getRegistry().register(ITM_BLOCK_BRICKS);
    }

    @SubscribeEvent
    public static void onBlockRegistryReady(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(BLK_BLOCK_STONE);
        event.getRegistry().register(BLK_BLOCK_BRICK);
        event.getRegistry().register(BLK_BLOCK_BRICKS);
    }

    public static void handleItemColors(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_BLOCK_STONE);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_BLOCK_BRICK);
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_BLOCK_BRICKS);

    }

    public static void handleBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_BLOCK_STONE);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_BLOCK_BRICK);
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_BLOCK_BRICKS);

    }
}
