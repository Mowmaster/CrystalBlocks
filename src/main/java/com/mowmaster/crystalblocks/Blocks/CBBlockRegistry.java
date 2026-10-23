package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.Blocks.Colorable.*;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class CBBlockRegistry
{
    public static final DeferredRegister.Blocks BCBLOCKS = DeferredRegister.createBlocks(CBReferences.MODID);

    public static final DeferredBlock<Block> TINTABLE_STONE = registerBlock("tintable_stone",
            properties -> new CBTintableBlock(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_STAIRS = registerBlock("tintable_stone_stairs",
            properties -> new CBTintableStairs(CBBlockRegistry.TINTABLE_STONE.get().defaultBlockState(), properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_SLAB = registerBlock("tintable_stone_slab",
            properties -> new CBTintableSlab(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_VERTICAL_SLAB = registerBlock("tintable_stone_vertical_slab",
            properties -> new CBTintableVerticalSlab(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_WALL = registerBlock("tintable_stone_wall",
            properties -> new CBTintableWall(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops().forceSolidOn()));
    public static final DeferredBlock<Block> TINTABLE_STONE_BUTTON = registerBlock("tintable_stone_button",
            properties -> new CBTintableButton(BlockSetType.STONE, 20, properties
                    .sound(SoundType.STONE).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED)));
    public static final DeferredBlock<Block> TINTABLE_STONE_PRESSUREPLATE = registerBlock("tintable_stone_pressureplate",
            properties -> new CBTintablePressurePlate(BlockSetType.STONE, properties
                    .sound(SoundType.STONE).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED)));
    public static final DeferredBlock<Block> TINTABLE_STONE_COLUMN = registerBlock("tintable_stone_column",
            properties -> new CBTintableColumn(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_HORIZONTAL_COLUMN = registerBlock("tintable_stone_horizontal_column",
            properties -> new CBTintableHorizontalColumn(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_ACCENTED_PILLAR = registerBlock("tintable_stone_accented_pillar",
            properties -> new CBAccentedPillar(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TINTABLE_STONE_ACCENTED_PILLAR_CAP = registerBlock("tintable_stone_accented_pillar_cap",
            properties -> new CBAccentedPillarCap(properties
                    .mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2f).requiresCorrectToolForDrops()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function)
    {
        DeferredBlock<T> toReturn = BCBLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block)
    {
        CBItemRegistry.CBITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    public static void register(IEventBus eventBus)
    {
        BCBLOCKS.register(eventBus);
    }
}
