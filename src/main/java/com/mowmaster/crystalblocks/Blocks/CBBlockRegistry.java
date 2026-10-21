package com.mowmaster.crystalblocks.Blocks;

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

    public static final DeferredBlock<Block> CRYSTAL_STONE_RED = registerBlock("crystal_stone_red",
            properties -> new BaseCrystalStone(properties
                    .mapColor(MapColor.COLOR_RED).sound(SoundType.STONE).strength(2f)
                    .pushReaction(PushReaction.PUSH).requiresCorrectToolForDrops()));
    /*public static final DeferredBlock<Block> CRYSTAL_STONE_RED_STAIRS = registerBlock("crystal_stone_red_stairs", properties -> new StairBlock(
            CBBlockRegistry.CRYSTAL_STONE_RED.get().defaultBlockState(), properties.sound(SoundType.STONE).strength(2.0f, 3.0f).sound(SoundType.STONE))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_RED_WALL = registerBlock("crystal_stone_red_wall", properties -> new WallBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f).forceSolidOn())
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_RED_BUTTON = registerBlock("crystal_stone_red_button", properties -> new ButtonBlock(
            BlockSetType.STONE,
            40,
            properties.sound(SoundType.STONE)
                    .strength(0.5f)
                    .noCollision()
                    .forceSolidOn()
                    .pushReaction(PushReaction.POPPED))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_RED_PRESSUREPLATE = registerBlock("crystal_stone_red_pressureplate", properties -> new PressurePlateBlock(
                    BlockSetType.STONE,
                    properties
                            .sound(SoundType.STONE)
                            .strength(0.5f)
                            .noCollision()
                            .forceSolidOn()
                            .pushReaction(PushReaction.POPPED)
            )
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_RED_SLAB = registerBlock("crystal_stone_red_slab", properties -> new SlabBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f))
    );*/



    public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN = registerBlock("crystal_stone_green",
            properties -> new BaseCrystalStone(properties
                    .mapColor(MapColor.COLOR_GREEN).sound(SoundType.STONE).strength(2f)
                    .pushReaction(PushReaction.PUSH).requiresCorrectToolForDrops()));
    /*public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN_STAIRS = registerBlock("crystal_stone_green_stairs", properties -> new StairBlock(
            CBBlockRegistry.CRYSTAL_STONE_RED.get().defaultBlockState(), properties.sound(SoundType.STONE).strength(2.0f, 3.0f).sound(SoundType.STONE))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN_WALL = registerBlock("crystal_stone_green_wall", properties -> new WallBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f).forceSolidOn())
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN_BUTTON = registerBlock("crystal_stone_green_button", properties -> new ButtonBlock(
            BlockSetType.STONE,
            40,
            properties.sound(SoundType.STONE)
                    .strength(0.5f)
                    .noCollision()
                    .forceSolidOn()
                    .pushReaction(PushReaction.POPPED))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN_PRESSUREPLATE = registerBlock("crystal_stone_green_pressureplate", properties -> new PressurePlateBlock(
                    BlockSetType.STONE,
                    properties
                            .sound(SoundType.STONE)
                            .strength(0.5f)
                            .noCollision()
                            .forceSolidOn()
                            .pushReaction(PushReaction.POPPED)
            )
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_GREEN_SLAB = registerBlock("crystal_stone_green_slab", properties -> new SlabBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f))
    );*/



    public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE = registerBlock("crystal_stone_blue",
            properties -> new BaseCrystalStone(properties
                    .mapColor(MapColor.COLOR_BLUE).sound(SoundType.STONE).strength(2f)
                    .pushReaction(PushReaction.PUSH).requiresCorrectToolForDrops()));
    /*public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE_STAIRS = registerBlock("crystal_stone_blue_stairs", properties -> new StairBlock(
            CBBlockRegistry.CRYSTAL_STONE_RED.get().defaultBlockState(), properties.sound(SoundType.STONE).strength(2.0f, 3.0f).sound(SoundType.STONE))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE_WALL = registerBlock("crystal_stone_blue_wall", properties -> new WallBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f).forceSolidOn())
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE_BUTTON = registerBlock("crystal_stone_blue_button", properties -> new ButtonBlock(
            BlockSetType.STONE,
            40,
            properties.sound(SoundType.STONE)
                    .strength(0.5f)
                    .noCollision()
                    .forceSolidOn()
                    .pushReaction(PushReaction.POPPED))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE_PRESSUREPLATE = registerBlock("crystal_stone_blue_pressureplate", properties -> new PressurePlateBlock(
                    BlockSetType.STONE,
                    properties
                            .sound(SoundType.STONE)
                            .strength(0.5f)
                            .noCollision()
                            .forceSolidOn()
                            .pushReaction(PushReaction.POPPED)
            )
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLUE_SLAB = registerBlock("crystal_stone_blue_slab", properties -> new SlabBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f))
    );*/



    public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE = registerBlock("crystal_stone_white",
            properties -> new BaseCrystalStone(properties
                    .mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.STONE).strength(2f)
                    .pushReaction(PushReaction.PUSH).requiresCorrectToolForDrops()));
    /*public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE_STAIRS = registerBlock("crystal_stone_white_stairs", properties -> new StairBlock(
            CBBlockRegistry.CRYSTAL_STONE_RED.get().defaultBlockState(), properties.sound(SoundType.STONE).strength(2.0f, 3.0f).sound(SoundType.STONE))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE_WALL = registerBlock("crystal_stone_white_wall", properties -> new WallBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f).forceSolidOn())
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE_BUTTON = registerBlock("crystal_stone_white_button", properties -> new ButtonBlock(
            BlockSetType.STONE,
            40,
            properties.sound(SoundType.STONE)
                    .strength(0.5f)
                    .noCollision()
                    .forceSolidOn()
                    .pushReaction(PushReaction.POPPED))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE_PRESSUREPLATE = registerBlock("crystal_stone_white_pressureplate", properties -> new PressurePlateBlock(
                    BlockSetType.STONE,
                    properties
                            .sound(SoundType.STONE)
                            .strength(0.5f)
                            .noCollision()
                            .forceSolidOn()
                            .pushReaction(PushReaction.POPPED)
            )
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_WHITE_SLAB = registerBlock("crystal_stone_white_slab", properties -> new SlabBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f))
    );
*/


    public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK = registerBlock("crystal_stone_black",
            properties -> new BaseCrystalStone(properties
                    .mapColor(MapColor.COLOR_BLACK).sound(SoundType.STONE).strength(2f)
                    .pushReaction(PushReaction.PUSH).requiresCorrectToolForDrops()));
    /*public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK_STAIRS = registerBlock("crystal_stone_black_stairs", properties -> new StairBlock(
            CBBlockRegistry.CRYSTAL_STONE_RED.get().defaultBlockState(), properties.sound(SoundType.STONE).strength(2.0f, 3.0f).sound(SoundType.STONE))
    );
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK_WALL = registerBlock("crystal_stone_black_wall", properties -> new WallBlock(
            properties.sound(SoundType.STONE).strength(2.0f, 3.0f).forceSolidOn()));
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK_BUTTON = registerBlock("crystal_stone_black_button", properties -> new ButtonBlock(
            BlockSetType.STONE, 40, properties.sound(SoundType.STONE).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED)));
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK_PRESSUREPLATE = registerBlock("crystal_stone_black_pressureplate", properties -> new PressurePlateBlock(
                    BlockSetType.STONE, properties.sound(SoundType.STONE).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED)));
    public static final DeferredBlock<Block> CRYSTAL_STONE_BLACK_SLAB = registerBlock("crystal_stone_black_slab", properties -> new SlabBlock(properties.sound(SoundType.STONE).strength(2.0f, 3.0f)));
*/

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
