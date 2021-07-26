package com.mowmaster.crystalblocks.blocks;

import com.mowmaster.crystalblocks.items.ColorApplicator;
import com.mowmaster.crystalblocks.reference.ColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.NbtComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;

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
        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);

        if(p_49850_ instanceof Player)
        {
            Player player = ((Player)p_49850_);
            if(player.getOffhandItem().getItem() instanceof ColorApplicator)
            {
                int[] colored = ColorReference.getRGBColorFromItemStack(player.getOffhandItem());
                BlockState newState = p_49849_.setValue(COLOR_RED,colored[0]).setValue(COLOR_GREEN,colored[1]).setValue(COLOR_BLUE,colored[2]);
                p_49847_.setBlock(p_49848_,newState,3);
            }
        }
        else
        {
            int[] colored = ColorReference.getRGBColorFromItemStack(p_49851_);
            BlockState newState = p_49849_.setValue(COLOR_RED,colored[0]).setValue(COLOR_GREEN,colored[1]).setValue(COLOR_BLUE,colored[2]);
            p_49847_.setBlock(p_49848_,newState,3);
        }

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

    private static final ResourceLocation RES_BLOCK_STONE = new ResourceLocation(MODID, "block_stone");
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
        event.getItemColors().register((itemstack, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColorFromItemStack(itemstack);} else {return -1;}},ITM_BLOCK_STONE);
    }

    public static void handleBlockColors(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((blockstate, blockReader, blockPos, tintIndex) -> {if (tintIndex == 1) {return ColorReference.getColor(Arrays.asList(blockstate.getValue(COLOR_RED),blockstate.getValue(COLOR_GREEN),blockstate.getValue(COLOR_BLUE)));} else {return -1;}},BLK_BLOCK_STONE);
    }
}
