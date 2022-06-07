package com.mowmaster.crystalblocks.Registry;

import com.mojang.serialization.Codec;
import com.mowmaster.crystalblocks.Blocks.BaseColoredSlabBlock;
import com.mowmaster.crystalblocks.Blocks.BaseColoredStairBlock;
import com.mowmaster.crystalblocks.Blocks.ColoredStoneBlock;
import com.mowmaster.mowlib.Tabs.MowLibTab;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.mowmaster.crystalblocks.Util.References.MODID;

public class DeferredRegisterBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,MODID);

    public static final RegistryObject<Block> CRYSTAL_STONE = registerBlock("block_crystal_stone",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_SLAB = registerBlock("block_crystal_stone_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_STAIR = registerBlock("block_crystal_stone_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK = registerBlock("block_crystal_stone_brick",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK_SLAB = registerBlock("block_crystal_stone_brick_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK_STAIR = registerBlock("block_crystal_stone_brick_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICK.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS = registerBlock("block_crystal_stone_bricks",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_SLAB = registerBlock("block_crystal_stone_bricks_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_STAIR = registerBlock("block_crystal_stone_bricks_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        DeferredRegisterItems.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties().tab(MowLibTab.TAB_ITEMS)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    //For Geodes
    private static <P extends PlacementModifier> PlacementModifierType<P> register(String name, Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, name, () -> {
            return codec;
        });
    }
}
