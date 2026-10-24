package com.mowmaster.crystalblocks.Blocks;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

// Everything that changes between stone-like, wooden and metal block sets
public record CBMaterial(
        MapColor mapColor,
        SoundType sound,
        // Button/pressure plate sounds, and whether arrows or items can press them
        BlockSetType setType,
        // How long buttons stay pressed
        int buttonTicks,
        // Whether the block only drops when mined with the right tool
        boolean needsTool,
        TagKey<Block> mineableTag,
        TagKey<Block> buttonTag,
        TagKey<Block> pressurePlateTag)
{
    public static final CBMaterial STONE = new CBMaterial(MapColor.STONE, SoundType.STONE, BlockSetType.STONE, 20, true,
            BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.STONE_BUTTONS, BlockTags.STONE_PRESSURE_PLATES);

    public static final CBMaterial WOOD = new CBMaterial(MapColor.WOOD, SoundType.WOOD, BlockSetType.OAK, 30, false,
            BlockTags.MINEABLE_WITH_AXE, BlockTags.WOODEN_BUTTONS, BlockTags.WOODEN_PRESSURE_PLATES);

    public static final CBMaterial METAL = new CBMaterial(MapColor.METAL, SoundType.METAL, BlockSetType.IRON, 20, true,
            BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.BUTTONS, BlockTags.PRESSURE_PLATES);

    // The shared properties for full-sized shapes in this material
    public BlockBehaviour.Properties apply(BlockBehaviour.Properties properties)
    {
        properties.mapColor(mapColor).sound(sound).strength(2f);
        if (needsTool) properties.requiresCorrectToolForDrops();
        return properties;
    }
}