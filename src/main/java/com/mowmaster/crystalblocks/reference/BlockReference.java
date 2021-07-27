package com.mowmaster.crystalblocks.reference;

import com.mowmaster.crystalblocks.blocks.CrystalBlock;
import com.mowmaster.crystalblocks.blocks.CrystalSlab;
import com.mowmaster.crystalblocks.blocks.CrystalStair;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

import java.util.*;

import static com.mowmaster.crystalblocks.blocks.CrystalBlock.*;

public class BlockReference
{

    public static BlockState getBlock(BlockState state, boolean crouching)
    {
        int red = state.getValue(COLOR_RED);
        int green = state.getValue(COLOR_GREEN);
        int blue = state.getValue(COLOR_BLUE);

        List<Block> getBlockList = new ArrayList<Block>(
                Arrays.asList(
                        CrystalBlock.BLK_BLOCK_STONE,
                        CrystalBlock.BLK_BLOCK_BRICK,
                        CrystalBlock.BLK_BLOCK_BRICKS
                ));

        int currentBlock = getBlockList.indexOf(state.getBlock());
        int nextBlock = 0;
        if(!crouching)
        {
            nextBlock = currentBlock+1;
            if(nextBlock>=getBlockList.size())nextBlock = 0;
        }
        else
        {
            nextBlock = currentBlock-1;
            if(nextBlock<0)nextBlock = getBlockList.size()-1;
        }
        BlockState newState = getBlockList.get(nextBlock).defaultBlockState().setValue(COLOR_RED,red).setValue(COLOR_GREEN,green).setValue(COLOR_BLUE,blue);

        return newState;
    }

    public static BlockState getStair(BlockState state, boolean crouching)
    {
        int red = state.getValue(COLOR_RED);
        int green = state.getValue(COLOR_GREEN);
        int blue = state.getValue(COLOR_BLUE);
        Direction FACING = state.getValue(HorizontalDirectionalBlock.FACING);
        Half HALF = state.getValue(BlockStateProperties.HALF);
        StairsShape SHAPE = state.getValue(BlockStateProperties.STAIRS_SHAPE);
        Boolean WATER = state.getValue(BlockStateProperties.WATERLOGGED);

        List<Block> getBlockList = new ArrayList<Block>(
                Arrays.asList(
                        CrystalStair.BLK_STAIR_STONE,
                        CrystalStair.BLK_STAIR_BRICK,
                        CrystalStair.BLK_STAIR_BRICKS
                ));

        int currentBlock = getBlockList.indexOf(state.getBlock());
        int nextBlock = 0;
        if(!crouching)
        {
            nextBlock = currentBlock+1;
            if(nextBlock>=getBlockList.size())nextBlock = 0;
        }
        else
        {
            nextBlock = currentBlock-1;
            if(nextBlock<0)nextBlock = getBlockList.size()-1;
        }
        BlockState newState = getBlockList.get(nextBlock).defaultBlockState().setValue(COLOR_RED,red).setValue(COLOR_GREEN,green)
                .setValue(COLOR_BLUE,blue).setValue(HorizontalDirectionalBlock.FACING,FACING).setValue(BlockStateProperties.HALF,HALF).setValue(BlockStateProperties.STAIRS_SHAPE,SHAPE)
                .setValue(BlockStateProperties.WATERLOGGED,WATER);

        return newState;
    }

    public static BlockState getSlab(BlockState state, boolean crouching)
    {
        int red = state.getValue(COLOR_RED);
        int green = state.getValue(COLOR_GREEN);
        int blue = state.getValue(COLOR_BLUE);
        Boolean WATER = state.getValue(BlockStateProperties.WATERLOGGED);
        SlabType SLABBY = state.getValue(BlockStateProperties.SLAB_TYPE);

        List<Block> getBlockList = new ArrayList<Block>(
                Arrays.asList(
                        CrystalSlab.BLK_SLAB_STONE,
                        CrystalSlab.BLK_SLAB_BRICK,
                        CrystalSlab.BLK_SLAB_BRICKS
                ));

        int currentBlock = getBlockList.indexOf(state.getBlock());
        int nextBlock = 0;
        if(!crouching)
        {
            nextBlock = currentBlock+1;
            if(nextBlock>=getBlockList.size())nextBlock = 0;
        }
        else
        {
            nextBlock = currentBlock-1;
            if(nextBlock<0)nextBlock = getBlockList.size()-1;
        }
        BlockState newState = getBlockList.get(nextBlock).defaultBlockState().setValue(COLOR_RED,red).setValue(COLOR_GREEN,green)
                .setValue(COLOR_BLUE,blue).setValue(BlockStateProperties.SLAB_TYPE,SLABBY).setValue(BlockStateProperties.WATERLOGGED,WATER);

        return newState;
    }

}
