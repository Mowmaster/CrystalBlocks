package com.mowmaster.crystalblocks.Items.Tools;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

// Right-click the top of a block to tamp it into a path: dirt-like blocks into vanilla's dirt path, and gravel, sand,
// red sand, clay and mud into Crystal Blocks' paths. Needs open air above, like a shovel. Plays the block's
// falling sound and wears the tamper by 1.
public class CBTamperItem extends Item
{
    public CBTamperItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block path = pathFor(state.getBlock());
        if (path == null || context.getClickedFace() == Direction.DOWN || !level.getBlockState(pos.above()).isAir())
        {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide())
        {
            Player player = context.getPlayer();
            BlockState pathState = path.defaultBlockState();
            // The sound of the block being landed on (dirt's is the gravel fall sound)
            SoundType sound = state.getSoundType(level, pos, player);
            level.playSound(null, pos, sound.getFallSound(), SoundSource.BLOCKS, 1f, 1f);
            level.setBlock(pos, pathState, Block.UPDATE_ALL_IMMEDIATE);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, pathState));
            if (player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
        }
        return InteractionResult.SUCCESS;
    }

    // What a block becomes when tamped, or null if it can't be
    @Nullable
    public static Block pathFor(Block block)
    {
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL
                || block == Blocks.MYCELIUM || block == Blocks.ROOTED_DIRT)
        {
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.GRAVEL) return CBBlockRegistry.GRAVEL_PATH.get();
        if (block == Blocks.SAND) return CBBlockRegistry.SAND_PATH.get();
        if (block == Blocks.RED_SAND) return CBBlockRegistry.RED_SAND_PATH.get();
        if (block == Blocks.CLAY) return CBBlockRegistry.CLAY_PATH.get();
        if (block == Blocks.MUD) return CBBlockRegistry.MUD_PATH.get();
        return null;
    }
}
