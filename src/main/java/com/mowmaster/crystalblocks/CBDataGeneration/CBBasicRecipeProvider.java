package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

public class CBBasicRecipeProvider extends RecipeProvider
{

    protected CBBasicRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    public static MultiRegistryBootstrap create()
    {
        return RecipeProvider.asBootstrap(CBBasicRecipeProvider::new);
    }

    @Override
    protected void buildRecipes() {

        shaped(RecipeCategory.BUILDING_BLOCKS, CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), 1)
                .pattern("  S")
                .pattern(" S ")
                .pattern("II ")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output, CBReferences.MODID+".base_chisel");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE.get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Blocks.STONE)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID+".crystal_stone_from_amethyst");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE_SLAB.get(), 6)
                .pattern("SSS")
                .define('S', CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_slabs");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE.get(), 1)
                .pattern("S")
                .pattern("S")
                .define('S', CBBlockRegistry.TINTABLE_STONE_SLAB.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE_SLAB.get()), has(CBBlockRegistry.TINTABLE_STONE_SLAB.get()))
                .save(output, CBReferences.MODID+".crystal_stone_from_slabs");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get(), 6)
                .pattern("S")
                .pattern("S")
                .pattern("S")
                .define('S', CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_slabs_vertical");
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE.get(), 1)
                .pattern("SS")
                .define('S', CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get()), has(CBBlockRegistry.TINTABLE_STONE_VERTICAL_SLAB.get()))
                .save(output, CBReferences.MODID+".crystal_stone_from_slabs_vertical");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE_STAIRS.get(), 8)
                .pattern("S  ")
                .pattern("SS ")
                .pattern("SSS")
                .define('S', CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_stairs");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE_WALL.get(), 12)
                .pattern("SSS")
                .pattern("SSS")
                .define('S', CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_wall");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.TINTABLE_STONE_PRESSUREPLATE.get(), 8)
                .pattern("SS")
                .define('S', CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_pressureplate");

        shapeless(RecipeCategory.BUILDING_BLOCKS,CBBlockRegistry.TINTABLE_STONE_BUTTON.get() ,8)
                .requires(CBBlockRegistry.TINTABLE_STONE.get())
                .unlockedBy(getHasName(CBBlockRegistry.TINTABLE_STONE.get()), has(CBBlockRegistry.TINTABLE_STONE.get()))
                .save(output, CBReferences.MODID+".crystal_stone_button");




    }
}
