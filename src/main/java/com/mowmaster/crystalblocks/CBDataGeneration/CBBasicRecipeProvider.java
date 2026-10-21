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

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.CRYSTAL_STONE_WHITE.get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Blocks.STONE)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID+".crystal_stone_white_from_amethyst");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBItemRegistry.CRYSTAL_CHISEL_BASE.get(), 1)
                .pattern("  S")
                .pattern(" S ")
                .pattern("II ")
                .define('S', Items.STICK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(output, CBReferences.MODID+".base_chisel");


        /*shapeless(RecipeCategory.BUILDING_BLOCKS,Items.AMETHYST_SHARD ,8)
                .requires(Items.AMETHYST_SHARD)
                .requires(Blocks.STONE)
                .unlockedBy(getHasName(Items.BOWL), has(Items.BOWL))
                .save(output, CBReferences.MODID+".mortar_and_pestle");*/
    }
}
