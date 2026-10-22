package com.mowmaster.crystalblocks.Blocks.Colorable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Implement this on any block (full block, stairs, slab, wall...) that can be dyed.
// Extending EntityBlock means implementing this interface is all a block needs to get a CBTintBlockEntity.
// EntityBlock#getTicker defaults to null, so the block entity never ticks.
public interface IColorableBlock extends EntityBlock
{
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CBTintBlockEntity(pos, state);
    }

    // Call this from the block's useItemOn override.
    default InteractionResult tryDyeBlock(ItemStack stack, Level level, BlockPos pos, Player player)
    {
        // NeoForge addition: handles vanilla dyes and modded items tagged as dyes. Returns null if not a dye.
        DyeColor dye = DyeColor.getColor(stack);
        if (dye == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof CBTintBlockEntity tintEntity)) return InteractionResult.PASS;

        if (!level.isClientSide())
        {
            tintEntity.setColor(mixDye(tintEntity.getColor(), dye));
            //stack.consume(1, player); // does nothing in creative
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1f, 1f);
        }
        // SUCCESS on both sides so the client swings the arm
        return InteractionResult.SUCCESS;
    }

    // Same mixing maths as leather armour: average the channels, then restore the average brightness
    // so mixing doesn't keep getting darker. An undyed block takes the dye colour directly.
    static int mixDye(int currentColor, DyeColor dye)
    {
        int dyeColor = dye.getTextureDiffuseColor() & 0xFFFFFF; // strip alpha, keep RGB
        if (currentColor == CBTintBlockEntity.UNDYED) return dyeColor;

        int red = (red(currentColor) + red(dyeColor)) / 2;
        int green = (green(currentColor) + green(dyeColor)) / 2;
        int blue = (blue(currentColor) + blue(dyeColor)) / 2;

        float averageBrightness = (brightest(currentColor) + brightest(dyeColor)) / 2f;
        int mixedBrightness = Math.max(red, Math.max(green, blue));
        if (mixedBrightness == 0) return 0;

        red = (int) (red * averageBrightness / mixedBrightness);
        green = (int) (green * averageBrightness / mixedBrightness);
        blue = (int) (blue * averageBrightness / mixedBrightness);
        return (red << 16) | (green << 8) | blue;
    }

    private static int red(int rgb) { return (rgb >> 16) & 0xFF; }
    private static int green(int rgb) { return (rgb >> 8) & 0xFF; }
    private static int blue(int rgb) { return rgb & 0xFF; }
    private static int brightest(int rgb) { return Math.max(red(rgb), Math.max(green(rgb), blue(rgb))); }
}