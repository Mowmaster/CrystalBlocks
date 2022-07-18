package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.api.IColorableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils.getMowLibComponentLocalized;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BaseColoredSlabBlock extends SlabBlock implements IColorableBlock
{
    public BaseColoredSlabBlock(Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(MowLibColorReference.addColorToBlockState(this.defaultBlockState(),MowLibColorReference.DEFAULTCOLOR));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_152043_) {
        p_152043_.add(WATERLOGGED, TYPE, COLOR_RED, COLOR_GREEN, COLOR_BLUE);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_56361_) {
        BlockPos blockpos = p_56361_.getClickedPos();
        BlockState blockstate = p_56361_.getLevel().getBlockState(blockpos);
        int color = MowLibColorReference.DEFAULTCOLOR;
        if(p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof ColorApplicator)
        {
            color = MowLibColorReference.getColorFromItemStackInt(p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND));
        }
        else
        {
            color = MowLibColorReference.getColorFromItemStackInt(p_56361_.getPlayer().getItemInHand(p_56361_.getHand()));
        }

        if (blockstate.is(this) && MowLibColorReference.getColorFromStateInt(blockstate) == color) {
            return MowLibColorReference.addColorToBlockState(blockstate,color).setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false));
        }
        else if(blockstate.is(this)
                && MowLibColorReference.getColorFromStateInt(blockstate) != color)
        {
            FluidState fluidstate = p_56361_.getLevel().getFluidState(blockpos);
            Direction direction = p_56361_.getClickedFace();

            p_56361_.getLevel().setBlock((direction != Direction.DOWN)?(blockpos.above()):(blockpos.below()),MowLibColorReference.addColorToBlockState(this.defaultBlockState(),color).setValue(TYPE, (direction != Direction.DOWN)?(SlabType.BOTTOM):(SlabType.TOP)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER)),3);
            if(!p_56361_.getPlayer().isCreative())p_56361_.getPlayer().getItemInHand(InteractionHand.OFF_HAND).shrink(1);
            p_56361_.getLevel().playSound(p_56361_.getPlayer(),blockpos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS,1.0F, 1.0F);
            return this.defaultBlockState().setValue(TYPE, blockstate.getValue(TYPE)).setValue(WATERLOGGED, blockstate.getValue(WATERLOGGED))
                    .setValue(COLOR_RED, blockstate.getValue(COLOR_RED)).setValue(COLOR_GREEN, blockstate.getValue(COLOR_GREEN)).setValue(COLOR_BLUE, blockstate.getValue(COLOR_BLUE));
        }
        else {
            FluidState fluidstate = p_56361_.getLevel().getFluidState(blockpos);
            BlockState blockstate1 = MowLibColorReference.addColorToBlockState(this.defaultBlockState(),color).setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
            Direction direction = p_56361_.getClickedFace();
            return direction != Direction.DOWN && (direction == Direction.UP || !(p_56361_.getClickLocation().y - (double)blockpos.getY() > 0.5D)) ? blockstate1 : blockstate1.setValue(TYPE, SlabType.TOP);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack picked = state.getBlock().getCloneItemStack(world, pos, state);
        int getColor = MowLibColorReference.getColorFromStateInt(state);
        return MowLibColorReference.addColorToItemStack(picked,getColor);
    }

    @Override
    public void setPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack p_49851_) {
        if(p_49850_ instanceof Player)
        {
            Player player = ((Player)p_49850_);
            if(player.getOffhandItem().getItem() instanceof ColorApplicator)
            {
                int getColor = MowLibColorReference.getColorFromItemStackInt(player.getOffhandItem());
                BlockState newState = MowLibColorReference.addColorToBlockState(p_49849_,getColor);
                p_49847_.setBlock(p_49848_,newState,3);
            }
            else
            {
                int getColor = MowLibColorReference.getColorFromItemStackInt(p_49851_);
                BlockState newState = MowLibColorReference.addColorToBlockState(p_49849_,getColor);
                p_49847_.setBlock(p_49848_,newState,3);
            }
        }

        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        int getColor;
        int currentColor;
        Component sameColor;
        BlockState newState;
        List<Item> DYES = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes"))).stream().toList();

        ItemStack itemInHand = p_60506_.getMainHandItem();
        ItemStack itemInOffHand = p_60506_.getOffhandItem();

        if(p_60506_.getItemInHand(p_60507_).getItem() instanceof ColorApplicator)
        {
            getColor = MowLibColorReference.getColorFromItemStackInt(p_60506_.getItemInHand(p_60507_));
            currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
            if(currentColor != getColor)
            {
                newState = MowLibColorReference.addColorToBlockState(p_60503_,getColor);
                p_60504_.setBlock(p_60505_,newState,3);
                //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
                return InteractionResult.SUCCESS;
            }
            sameColor = getMowLibComponentLocalized(".recolor.message_sameColor", ChatFormatting.RED);
            if(p_60504_.isClientSide)p_60506_.displayClientMessage(sameColor, false);
            return InteractionResult.FAIL;

        }
        else if(DYES.contains(itemInOffHand.getItem()))
        {
            getColor = MowLibColorReference.getColorFromDyeInt(itemInOffHand);
            currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
            if (currentColor != getColor) {
                newState = MowLibColorReference.addColorToBlockState(p_60503_, getColor);
                p_60504_.setBlock(p_60505_, newState, 3);
                return InteractionResult.SUCCESS;
            } else {
                sameColor = getMowLibComponentLocalized(".recolor.message_sameColor", ChatFormatting.RED);
                if(p_60504_.isClientSide)p_60506_.displayClientMessage(sameColor, false);
                return InteractionResult.FAIL;
            }

        }

        return super.use(p_60503_, p_60504_, p_60505_, p_60506_, p_60507_, p_60508_);
    }

    //Should Fix Building Gadgets dropps issues
    //https://github.com/Direwolf20-MC/MiningGadgets/blob/1.19/src/main/java/com/direwolf20/mininggadgets/common/tiles/RenderBlockTileEntity.java#L444
    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootContext.Builder p_60538_) {
        if (p_60537_.getBlock() instanceof BaseColoredSlabBlock) {
            List<ItemStack> stacks = new ArrayList<>();
            ItemStack itemstack = new ItemStack(this);
            int getColor = MowLibColorReference.getColorFromStateInt(p_60537_);
            ItemStack newStack = MowLibColorReference.addColorToItemStack(itemstack,getColor);
            newStack.setCount(1);
            stacks.add(newStack);
            return stacks;
        }
        return super.getDrops(p_60537_, p_60538_);
    }

    /*@Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        if(!p_60516_.isClientSide() && p_60519_)
        {
            if (p_60518_.getBlock() instanceof BaseColoredSlabBlock) {
                if (!p_60516_.isClientSide) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = MowLibColorReference.getColorFromStateInt(p_60518_);
                    ItemStack newStack = MowLibColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(p_60516_, (double)p_60517_.getX() + 0.5D, (double)p_60517_.getY() + 0.5D, (double)p_60517_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_60516_.addFreshEntity(itementity);
                }
            }
        }
        super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
    }*/

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return true;
    }

    /*@Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if(!level.isClientSide())
        {
            if (state.getBlock() instanceof BaseColoredSlabBlock) {
                if (!level.isClientSide) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = MowLibColorReference.getColorFromStateInt(state);
                    ItemStack newStack = MowLibColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public void playerWillDestroy(Level p_56212_, BlockPos p_56213_, BlockState p_56214_, Player p_56215_) {

        if(!p_56212_.isClientSide())
        {
            if (p_56214_.getBlock() instanceof BaseColoredSlabBlock) {
                if (!p_56212_.isClientSide && !p_56215_.isCreative()) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = MowLibColorReference.getColorFromStateInt(p_56214_);
                    ItemStack newStack = MowLibColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount((p_56214_.getValue(TYPE).equals(SlabType.DOUBLE))?(2):(1));
                    ItemEntity itementity = new ItemEntity(p_56212_, (double)p_56213_.getX() + 0.5D, (double)p_56213_.getY() + 0.5D, (double)p_56213_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_56212_.addFreshEntity(itementity);
                }
            }
        }
        super.playerWillDestroy(p_56212_, p_56213_, p_56214_, p_56215_);
    }*/

    @Override
    public void appendHoverText(ItemStack p_49816_, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
        super.appendHoverText(p_49816_, p_49817_, p_49818_, p_49819_);

    }
}