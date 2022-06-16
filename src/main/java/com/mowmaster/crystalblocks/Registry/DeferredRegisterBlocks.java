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

    public static final RegistryObject<Block> CRYSTAL_STONE_PATH = registerBlock("block_crystal_stone_cobble",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PATH_SLAB = registerBlock("block_crystal_stone_cobble_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PATH_STAIR = registerBlock("block_crystal_stone_cobble_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_PATH.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_SAND = registerBlock("block_crystal_stone",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_SAND_SLAB = registerBlock("block_crystal_stone_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_SAND_STAIR = registerBlock("block_crystal_stone_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_SAND.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE = registerBlock("block_crystal_stone_smooth",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_SLAB = registerBlock("block_crystal_stone_smooth_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_STAIR = registerBlock("block_crystal_stone_smooth_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK = registerBlock("block_crystal_stone_brick_1",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK_SLAB = registerBlock("block_crystal_stone_brick_1_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICK_STAIR = registerBlock("block_crystal_stone_brick_1_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICK.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_3 = registerBlock("block_crystal_stone_brick_2",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_3_SLAB = registerBlock("block_crystal_stone_brick_2_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_3_STAIR = registerBlock("block_crystal_stone_brick_2_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICKS_3.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS = registerBlock("block_crystal_stone_brick_3",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_SLAB = registerBlock("block_crystal_stone_brick_3_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_STAIR = registerBlock("block_crystal_stone_brick_3_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_PLANK = registerBlock("block_crystal_stone_brick_4",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PLANK_SLAB = registerBlock("block_crystal_stone_brick_4_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PLANK_STAIR = registerBlock("block_crystal_stone_brick_4_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_PLANK.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_4 = registerBlock("block_crystal_stone_brick_5",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_4_SLAB = registerBlock("block_crystal_stone_brick_5_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_BRICKS_4_STAIR = registerBlock("block_crystal_stone_brick_5_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_BRICKS_4.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_PILLARS = registerBlock("block_crystal_stone_pillar_1",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PILLARS_SLAB = registerBlock("block_crystal_stone_pillar_1_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PILLARS_STAIR = registerBlock("block_crystal_stone_pillar_1_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_PILLARS.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));

    public static final RegistryObject<Block> CRYSTAL_STONE_PILLAR = registerBlock("block_crystal_stone_pillar_2",
            () -> new ColoredStoneBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PILLAR_SLAB = registerBlock("block_crystal_stone_pillar_2_slab",
            () -> new BaseColoredSlabBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> CRYSTAL_STONE_PILLAR_STAIR = registerBlock("block_crystal_stone_pillar_2_stair",
            () -> new BaseColoredStairBlock(CRYSTAL_STONE_PILLAR.get().defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE).strength(1.0F).sound(SoundType.STONE)));



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
