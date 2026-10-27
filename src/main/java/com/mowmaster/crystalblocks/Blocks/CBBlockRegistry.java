package com.mowmaster.crystalblocks.Blocks;

import com.mowmaster.crystalblocks.Blocks.Colorable.*;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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
    // ---- Bolted supports: fixed textures (metal + stone, or metal + log), registered once each ----
    public static final DeferredBlock<Block> BOLTED_CORNER_SUPPORT_CAP = registerBlock("bolted_corner_support_cap",
            properties -> new CBBoltedCornerSupportCap(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> BOLTED_I_BEAM = registerBlock("bolted_i_beam",
            properties -> new CBBoltedIBeam(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> BOLTED_I_BEAM_CAP = registerBlock("bolted_i_beam_cap",
            properties -> new CBBoltedIBeamCap(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> BOLTED_SUPPORT_BEAM = registerBlock("bolted_support_beam",
            properties -> new CBBoltedSupportBeam(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> BOLTED_SUPPORT_BEAM_CAP = registerBlock("bolted_support_beam_cap",
            properties -> new CBBoltedSupportBeamCap(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> CHAINED_SUPPORT_BEAM = registerBlock("chained_support_beam",
            properties -> new CBChainedSupportBeam(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> CHAIN_BEAM = registerBlock("chain_beam",
            properties -> new CBChainBeam(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> CHAIN_BEAM_CAP = registerBlock("chain_beam_cap",
            properties -> new CBChainBeamCap(CBMaterial.METAL.apply(properties)));
    public static final DeferredBlock<Block> CHAIN_BEAM_MEDIUM = registerBlock("chain_beam_medium",
            properties -> new CBChainBeam(CBMaterial.METAL.apply(properties)) {
                @Override public CBChainSize size() { return CBChainSize.MEDIUM; }
            });
    public static final DeferredBlock<Block> CHAIN_BEAM_MEDIUM_CAP = registerBlock("chain_beam_medium_cap",
            properties -> new CBChainBeamCap(CBMaterial.METAL.apply(properties)) {
                @Override public CBChainSize size() { return CBChainSize.MEDIUM; }
            });
    public static final DeferredBlock<Block> CHAIN_BEAM_LARGE = registerBlock("chain_beam_large",
            properties -> new CBChainBeam(CBMaterial.METAL.apply(properties)) {
                @Override public CBChainSize size() { return CBChainSize.LARGE; }
            });
    public static final DeferredBlock<Block> CHAIN_BEAM_LARGE_CAP = registerBlock("chain_beam_large_cap",
            properties -> new CBChainBeamCap(CBMaterial.METAL.apply(properties)) {
                @Override public CBChainSize size() { return CBChainSize.LARGE; }
            });
    public static final DeferredBlock<Block> CHAINED_SUPPORT_BEAM_MEDIUM = registerBlock("chained_support_beam_medium",
            properties -> new CBChainedSupportBeam(CBMaterial.METAL.apply(properties)) {
                @Override public CBChainSize chainSize() { return CBChainSize.MEDIUM; }
            });

    public static final DeferredBlock<Block> SCONCE_TORCH = registerBlock("sconce_torch",
            properties -> new CBSconce(CBMaterial.METAL.apply(properties)
                    .sound(SoundType.CHAIN)   // chain sounds for placing, breaking and stepping
                    .strength(1f)
                    .noCollision()            // walk through it, like a torch
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(CBSconce.LIT) ? 15 : 0)));

    public static final DeferredBlock<Block> CHAIN_LANTERN = registerBlock("chain_lantern",
            properties -> new CBChainLantern(lanternProperties(properties)));
    public static final DeferredBlock<Block> CHAIN_LANTERN_CAP = registerBlock("chain_lantern_cap",
            properties -> new CBChainLantern(lanternProperties(properties)) {
                @Override public boolean hasPlate() { return true; }
            });
    public static final DeferredBlock<Block> CHAIN_LANTERN_MEDIUM = registerBlock("chain_lantern_medium",
            properties -> new CBChainLantern(lanternProperties(properties)) {
                @Override public CBChainSize size() { return CBChainSize.MEDIUM; }
            });
    public static final DeferredBlock<Block> CHAIN_LANTERN_MEDIUM_CAP = registerBlock("chain_lantern_medium_cap",
            properties -> new CBChainLantern(lanternProperties(properties)) {
                @Override public CBChainSize size() { return CBChainSize.MEDIUM; }
                @Override public boolean hasPlate() { return true; }
            });

    public static final DeferredBlock<Block> TINTABLE_BLOCK_TECH = registerBlock("tintable_block_tech",
            properties -> new CBTintableTechBlock(CBMaterial.METAL.apply(properties).strength(5f, 6f)));
    public static final DeferredBlock<Block> TINTABLE_BLOCK_TECH_3D = registerBlock("tintable_block_tech_3d",
            properties -> new CBTintableTech3DBlock(CBMaterial.METAL.apply(properties).strength(5f, 6f)));

    public static final DeferredBlock<Block> TINTABLE_BARREL = registerBlock("tintable_barrel",
            properties -> new CBTintableBarrel(CBMaterial.WOOD.apply(properties).strength(2.5f).noOcclusion()));
    public static final DeferredBlock<Block> TINTABLE_CHEST = registerBlock("tintable_chest",
            properties -> new CBTintableChest(CBMaterial.WOOD.apply(properties).strength(2.5f).noOcclusion()));

    public static final List<DeferredBlock<Block>> STORAGE = List.of(TINTABLE_BARREL, TINTABLE_CHEST);
    public static final List<DeferredBlock<Block>> BOLTED = List.of(BOLTED_CORNER_SUPPORT_CAP,
            BOLTED_I_BEAM, BOLTED_I_BEAM_CAP, BOLTED_SUPPORT_BEAM, BOLTED_SUPPORT_BEAM_CAP,
            CHAINED_SUPPORT_BEAM, CHAIN_BEAM, CHAIN_BEAM_CAP,
            CHAIN_BEAM_MEDIUM, CHAIN_BEAM_MEDIUM_CAP, CHAIN_BEAM_LARGE, CHAIN_BEAM_LARGE_CAP, CHAINED_SUPPORT_BEAM_MEDIUM,
            SCONCE_TORCH, CHAIN_LANTERN, CHAIN_LANTERN_CAP, CHAIN_LANTERN_MEDIUM, CHAIN_LANTERN_MEDIUM_CAP, TINTABLE_BLOCK_TECH, TINTABLE_BLOCK_TECH_3D);

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
                registerBlock(name + "_accented_pillar",
                        properties -> new CBAccentedPillar(material.apply(properties))),
                registerBlock(name + "_accented_pillar_cap",
                        properties -> new CBAccentedPillarCap(material.apply(properties))),
                registerBlock(name + "_stepped_pillar",
                        properties -> new CBSteppedPillar(material.apply(properties))),
                registerBlock(name + "_stepped_pillar_cap",
                        properties -> new CBSteppedPillarCap(material.apply(properties))),
                registerBlock(name + "_window",
                        properties -> new CBTintableWindow(material.apply(properties).noOcclusion())));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function)
    {
        DeferredBlock<T> toReturn = BCBLOCKS.registerBlock(name, function);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Vanilla lantern feel: lantern sounds and hardness, broken (not pushed) by pistons, light 15 while lit
    private static BlockBehaviour.Properties lanternProperties(BlockBehaviour.Properties properties)
    {
        return CBMaterial.METAL.apply(properties)
                .sound(SoundType.LANTERN)
                .strength(3.5f)
                .noOcclusion()
                .pushReaction(PushReaction.POPPED)
                .lightLevel(state -> state.getValue(CBChainLantern.LIT) ? 15 : 0);
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