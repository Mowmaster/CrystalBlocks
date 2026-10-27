package com.mowmaster.crystalblocks.CBDataGeneration;

import com.mowmaster.crystalblocks.Blocks.CBBlockRegistry;
import com.mowmaster.crystalblocks.CBReferences;
import com.mowmaster.crystalblocks.Items.CBItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import com.mowmaster.crystalblocks.Blocks.CBBlockSet;
import net.minecraft.world.level.block.Block;
import com.mowmaster.crystalblocks.Recipes.CBPickedDyeRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

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
                .save(output, CBReferences.MODID + ".base_chisel");

        // How each texture's base block is made. Add one for each new set.
        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.STONE.base().get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', Blocks.STONE)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID + ".crystal_stone_from_amethyst");

        shaped(RecipeCategory.BUILDING_BLOCKS, CBBlockRegistry.LOG_ONE.base().get(), 8)
                .pattern("DDD")
                .pattern("DID")
                .pattern("DDD")
                .define('D', ItemTags.LOGS)
                .define('I', Items.AMETHYST_SHARD)
                .unlockedBy(getHasName(Items.AMETHYST_SHARD), has(Items.AMETHYST_SHARD))
                .save(output, CBReferences.MODID + ".crystal_log_from_amethyst");



        // Colour picker -> custom dye (the picker is returned reset)
        SpecialRecipeBuilder.special(() -> CBPickedDyeRecipe.INSTANCE)
                .save(output, CBReferences.MODID + ".picked_dye");
    }

    private void shapeRecipes(CBBlockSet set) {
        Block base = set.base().get();
        Block slab = set.slab().get();
        Block verticalSlab = set.verticalSlab().get();
        String id = CBReferences.MODID + "." + set.name();

        shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                .pattern("SSS")
                .define('S', base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_slabs");

        shaped(RecipeCategory.BUILDING_BLOCKS, base, 1)
                .pattern("S")
                .pattern("S")
                .define('S', slab)
                .unlockedBy(getHasName(slab), has(slab))
                .save(output, id + "_from_slabs");

        shaped(RecipeCategory.BUILDING_BLOCKS, verticalSlab, 6)
                .pattern("S")
                .pattern("S")
                .pattern("S")
                .define('S', base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_slabs_vertical");

        shaped(RecipeCategory.BUILDING_BLOCKS, base, 1)
                .pattern("SS")
                .define('S', verticalSlab)
                .unlockedBy(getHasName(verticalSlab), has(verticalSlab))
                .save(output, id + "_from_slabs_vertical");

        shaped(RecipeCategory.BUILDING_BLOCKS, set.stairs().get(), 8)
                .pattern("S  ")
                .pattern("SS ")
                .pattern("SSS")
                .define('S', base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_stairs");

        shaped(RecipeCategory.BUILDING_BLOCKS, set.wall().get(), 12)
                .pattern("SSS")
                .pattern("SSS")
                .define('S', base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_wall");

        shaped(RecipeCategory.BUILDING_BLOCKS, set.pressurePlate().get(), 8)
                .pattern("SS")
                .define('S', base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_pressureplate");

        shapeless(RecipeCategory.BUILDING_BLOCKS, set.button().get(), 8)
                .requires(base)
                .unlockedBy(getHasName(base), has(base))
                .save(output, id + "_button");
    }
}
