package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.Blocks.Colorable.*;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Function;

public class CBBlockRegistry
{
    public static final DeferredRegister.Blocks BCBLOCKS = DeferredRegister.createBlocks(CBReferences.MODID);

    // ---- Block sets: one line per texture. The name must match a texture in textures/block/ ----
    public static final CBBlockSet STONE = registerSet("tintable_stone", "Crystal Stone", CBMaterial.STONE);
    public static final CBBlockSet BRICK_LARGE = registerSet("tintable_brick_large", "Crystal Large Brick", CBMaterial.STONE);
    public static final CBBlockSet BRICK_MEDIUM = registerSet("tintable_brick_medium", "Crystal Medium Brick", CBMaterial.STONE);
    public static final CBBlockSet BRICK_SMALL = registerSet("tintable_brick_small", "Crystal Small Brick", CBMaterial.STONE);
    public static final CBBlockSet LOG_ONE = registerSet("tintable_log_one", "Crystal Log", CBMaterial.WOOD);
    public static final CBBlockSet METAL = registerSet("tintable_metal", "Crystal Metal", CBMaterial.METAL);

    // Every set; datagen, tags, recipes, lang and the creative tab all loop over this
    public static final List<CBBlockSet> SETS = List.of(STONE, BRICK_LARGE, BRICK_MEDIUM, BRICK_SMALL, LOG_ONE, METAL);

    // Registers every shape for one texture
    private static CBBlockSet registerSet(String name, String displayName, CBMaterial material)
    {
        DeferredBlock<Block> base = registerBlock(name,
                properties -> new CBTintableBlock(material.apply(properties)));

        return new CBBlockSet(name, displayName, material,
                base,
                registerBlock(name + "_stairs",
                        properties -> new CBTintableStairs(base.get().defaultBlockState(), material.apply(properties))),
                registerBlock(name + "_slab",
                        properties -> new CBTintableSlab(material.apply(properties))),
                registerBlock(name + "_vertical_slab",
                        properties -> new CBTintableVerticalSlab(material.apply(properties))),
                registerBlock(name + "_wall",
                        properties -> new CBTintableWall(material.apply(properties).forceSolidOn())),
                registerBlock(name + "_button",
                        properties -> new CBTintableButton(material.setType(), material.buttonTicks(), properties
                                .sound(material.sound()).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED))),
                registerBlock(name + "_pressureplate",
                        properties -> new CBTintablePressurePlate(material.setType(), properties
                                .sound(material.sound()).strength(0.5f).noCollision().forceSolidOn().pushReaction(PushReaction.POPPED))),
                registerBlock(name + "_column",
                        properties -> new CBTintableColumn(material.apply(properties))),
                registerBlock(name + "_horizontal_column",
                        properties -> new CBTintableHorizontalColumn(material.apply(properties))),
                registerBlock(name + "_accented_pillar",
                        properties -> new CBAccentedPillar(material.apply(properties))),
                registerBlock(name + "_accented_pillar_cap",
                        properties -> new CBAccentedPillarCap(material.apply(properties))),
                registerBlock(name + "_window",
                        properties -> new CBTintableWindow(material.apply(properties).noOcclusion())));
    }

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